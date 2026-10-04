package com.pantrick.backend.service

import com.pantrick.backend.models.CookingReadinessResponse
import com.pantrick.backend.models.CookingSession
import com.pantrick.backend.models.CookingSessionStatus
import com.pantrick.backend.models.IngredientAvailability
import com.pantrick.backend.models.PantrySnapshot
import com.pantrick.backend.models.UpdatePantryItemRequest
import com.pantrick.backend.repository.CookingSessionRepository
import com.pantrick.backend.repository.PantryRepository
import com.pantrick.backend.repository.RecipeRepository
import java.time.Instant

/**
 * CookingSessionService — mengelola sesi memasak:
 * 1. Validasi bahan tersedia (nama + quantity)
 * 2. Snapshot pantry sebelum memasak
 * 3. Kurangi pantry secara atomik
 * 4. State machine: STARTED → COMPLETED | CANCELLED
 * 5. Rollback pantry saat CANCELLED
 *
 * Thread-safety: startCooking() di-synchronized per user+recipe untuk cegah double-tap.
 */
class CookingSessionService(
    private val recipeRepository: RecipeRepository,
    private val pantryRepository: PantryRepository,
    private val sessionRepository: CookingSessionRepository
) {

    // Lock per (userId+recipeId) untuk mencegah concurrent startCooking
    private val startLocks = java.util.concurrent.ConcurrentHashMap<String, Any>()

    // ======================================================================
    // CHECK READINESS (untuk UI tombol Memasak)
    // ======================================================================

    fun checkReadiness(userId: Int, recipeId: String): CookingReadinessResponse {
        val recipe = recipeRepository.getRecipeById(recipeId)
            ?: return CookingReadinessResponse(
                success = false,
                message = "Resep tidak ditemukan",
                recipeId = recipeId,
                recipeTitle = "",
                canCook = false
            )

        val pantryItems = pantryRepository.getItemsByUserId(userId)

        val ingAvailabilities = recipe.ingredients.map { ing ->
            buildAvailability(ing, pantryItems)
        }

        val canCook = ingAvailabilities.all { it.isNameMatched && it.isSufficientQty }

        return CookingReadinessResponse(
            success = true,
            message = if (canCook) "Semua bahan tersedia" else "Beberapa bahan belum cukup atau tidak tersedia",
            recipeId = recipeId,
            recipeTitle = recipe.title,
            canCook = canCook,
            ingredients = ingAvailabilities
        )
    }

    // ======================================================================
    // START COOKING
    // ======================================================================

    /**
     * Mulai memasak:
     * 1. Validasi recipe ada
     * 2. Cek apakah sudah ada sesi STARTED untuk recipe ini (idempotency)
     * 3. Cek semua bahan tersedia
     * 4. Snapshot pantry → kurangi quantity → buat session STARTED
     *
     * @throws IllegalStateException jika sesi sudah ada / bahan tidak cukup
     */
    fun startCooking(userId: Int, recipeId: String): CookingSession {
        val lockKey = "${userId}-${recipeId}"
        val lock = startLocks.computeIfAbsent(lockKey) { Any() }

        synchronized(lock) {
            // Idempotency: tolak jika sudah ada sesi STARTED
            val existing = sessionRepository.findActiveSession(userId, recipeId)
            if (existing != null) {
                throw IllegalStateException("Sesi memasak sudah aktif (${existing.id}). Selesaikan atau batalkan dulu.")
            }

            val recipe = recipeRepository.getRecipeById(recipeId)
                ?: throw IllegalArgumentException("Resep tidak ditemukan: $recipeId")

            val pantryItems = pantryRepository.getItemsByUserId(userId)

            // Validasi: semua bahan harus ada
            val ingAvailabilities = recipe.ingredients.map { buildAvailability(it, pantryItems) }
            val missing = ingAvailabilities.filter { !it.isNameMatched || !it.isSufficientQty }
            if (missing.isNotEmpty()) {
                val names = missing.joinToString(", ") { it.ingredientRaw.take(30) }
                throw IllegalStateException("Bahan tidak cukup: $names")
            }

            // Snapshot + deduct pantry
            val now = Instant.now().toString()
            val snapshots = mutableListOf<PantrySnapshot>()

            for (avail in ingAvailabilities) {
                val pantryId = avail.pantryItemId ?: continue
                val pantryItem = pantryRepository.getItemById(pantryId) ?: continue

                val qtyUsed = if (avail.isQuantityComparable && avail.recipeQtyParsed != null) {
                    avail.recipeQtyParsed
                } else {
                    // Unit tidak bisa dibandingkan: anggap 1 unit digunakan
                    1.0
                }

                val qtyBefore = pantryItem.quantity
                val qtyAfter = maxOf(0.0, qtyBefore - qtyUsed)

                snapshots.add(
                    PantrySnapshot(
                        pantryItemId = pantryId,
                        pantryItemName = pantryItem.name,
                        unit = pantryItem.unit,
                        quantityBefore = qtyBefore,
                        quantityUsed = qtyUsed,
                        quantityAfter = qtyAfter
                    )
                )

                // Kurangi pantry
                val updated = pantryItem.copy(
                    quantity = qtyAfter,
                    isConsumed = qtyAfter == 0.0,
                    updatedAt = now
                )
                pantryRepository.updateItem(updated)
            }

            // Buat session
            val session = CookingSession(
                id = "",
                userId = userId,
                recipeId = recipeId,
                recipeTitle = recipe.title,
                status = CookingSessionStatus.STARTED,
                snapshots = snapshots,
                startedAt = now
            )
            return sessionRepository.createSession(session)
        }
    }

    // ======================================================================
    // CANCEL COOKING — rollback pantry
    // ======================================================================

    fun cancelCooking(userId: Int, sessionId: String): CookingSession {
        val session = sessionRepository.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Sesi memasak tidak ditemukan: $sessionId")

        if (session.userId != userId) throw SecurityException("Akses ditolak")

        if (session.status != CookingSessionStatus.STARTED) {
            throw IllegalStateException(
                "Tidak dapat membatalkan sesi berstatus ${session.status}. Hanya STARTED yang bisa dibatalkan."
            )
        }

        val now = Instant.now().toString()

        // Rollback pantry ke snapshot
        for (snapshot in session.snapshots) {
            val pantryItem = pantryRepository.getItemById(snapshot.pantryItemId) ?: continue
            if (pantryItem.userId != userId) continue

            val restored = pantryItem.copy(
                quantity = snapshot.quantityBefore,
                isConsumed = false,
                updatedAt = now
            )
            pantryRepository.updateItem(restored)
        }

        val updated = session.copy(
            status = CookingSessionStatus.CANCELLED,
            endedAt = now
        )
        return sessionRepository.updateSession(updated)
            ?: throw IllegalStateException("Gagal memperbarui sesi")
    }

    // ======================================================================
    // COMPLETE COOKING — no rollback
    // ======================================================================

    fun completeCooking(userId: Int, sessionId: String): CookingSession {
        val session = sessionRepository.getSessionById(sessionId)
            ?: throw IllegalArgumentException("Sesi memasak tidak ditemukan: $sessionId")

        if (session.userId != userId) throw SecurityException("Akses ditolak")

        if (session.status != CookingSessionStatus.STARTED) {
            throw IllegalStateException(
                "Tidak dapat menyelesaikan sesi berstatus ${session.status}. Hanya STARTED yang bisa diselesaikan."
            )
        }

        val updated = session.copy(
            status = CookingSessionStatus.COMPLETED,
            endedAt = Instant.now().toString()
        )
        return sessionRepository.updateSession(updated)
            ?: throw IllegalStateException("Gagal memperbarui sesi")
    }

    // ======================================================================
    // GET SESSION
    // ======================================================================

    fun getSession(userId: Int, sessionId: String): CookingSession? {
        val session = sessionRepository.getSessionById(sessionId) ?: return null
        if (session.userId != userId) return null
        return session
    }

    fun getActiveSession(userId: Int, recipeId: String): CookingSession? =
        sessionRepository.findActiveSession(userId, recipeId)

    // ======================================================================
    // INTERNAL: Build ingredient availability
    // ======================================================================

    private fun buildAvailability(
        ing: com.pantrick.backend.models.RecipeIngredient,
        pantryItems: List<com.pantrick.backend.models.PantryItem>
    ): IngredientAvailability {
        val ingNorm = ing.normalizedName

        // Gunakan matching logic yang sama dengan RecommendationService
        // untuk konsistensi hasil antara recommendation dan cooking readiness
        val matched = pantryItems
            .filter { !it.isConsumed && it.quantity > 0 }
            .firstOrNull { pantry ->
                val pNorm = pantry.normalizedName
                isIngredientMatch(ingNorm, pNorm)
            }

        if (matched == null) {
            return IngredientAvailability(
                ingredientRaw = ing.raw,
                pantryItemId = null,
                pantryItemName = null,
                pantryQty = null,
                pantryUnit = null,
                recipeQtyStr = ing.quantity,
                recipeUnit = ing.unit,
                recipeQtyParsed = null,
                isSufficientQty = false,
                isNameMatched = false,
                isQuantityComparable = false
            )
        }

        // Parse recipe quantity
        val recipeQtyParsed = parseFraction(ing.quantity)

        // Compare quantity if both have compatible units
        val (comparable, sufficient) = if (recipeQtyParsed == null || ing.unit == null) {
            // Tidak ada quantity/unit di recipe → name match saja sudah cukup
            Pair(false, true)
        } else {
            val unitGroup = getUnitGroup(ing.unit)
            val pantryUnitGroup = getUnitGroup(matched.unit)
            if (unitGroup == null || pantryUnitGroup == null || unitGroup != pantryUnitGroup) {
                // Unit tidak kompatibel → tidak bisa dibandingkan, anggap sufficient
                Pair(false, true)
            } else {
                // Convert ke unit dasar lalu bandingkan
                val recipeBaseQty = toBaseUnit(recipeQtyParsed, ing.unit)
                val pantryBaseQty = toBaseUnit(matched.quantity, matched.unit)
                Pair(true, pantryBaseQty >= recipeBaseQty)
            }
        }

        return IngredientAvailability(
            ingredientRaw = ing.raw,
            pantryItemId = matched.id,
            pantryItemName = matched.name,
            pantryQty = matched.quantity,
            pantryUnit = matched.unit,
            recipeQtyStr = ing.quantity,
            recipeUnit = ing.unit,
            recipeQtyParsed = recipeQtyParsed,
            isSufficientQty = sufficient,
            isNameMatched = true,
            isQuantityComparable = comparable
        )
    }

    // ======================================================================
    // UNIT CONVERSION HELPERS
    // ======================================================================

    private enum class UnitGroup { WEIGHT, VOLUME, COUNT }

    private fun getUnitGroup(unit: String?): UnitGroup? {
        if (unit == null) return null
        return when (unit.trim().lowercase()) {
            "g", "gram", "grams", "kg", "kilogram", "kilograms",
            "oz", "ounce", "ounces", "lb", "lbs", "pound", "pounds" -> UnitGroup.WEIGHT

            "ml", "milliliter", "milliliters", "millilitre",
            "l", "liter", "liters", "litre",
            "cup", "cups", "tsp", "teaspoon", "teaspoons",
            "tbsp", "tablespoon", "tablespoons" -> UnitGroup.VOLUME

            "pcs", "piece", "pieces", "unit", "units", "whole",
            "clove", "cloves", "slice", "slices", "head", "heads",
            "bunch", "bunches", "stalk", "stalks", "sprig", "sprigs",
            "can", "cans", "bottle", "bottles", "package", "packages",
            "stick", "sticks" -> UnitGroup.COUNT

            else -> null
        }
    }

    /** Konversi ke unit dasar: gram (weight), ml (volume), pcs (count) */
    private fun toBaseUnit(qty: Double, unit: String): Double {
        return when (unit.trim().lowercase()) {
            "kg", "kilogram", "kilograms" -> qty * 1000
            "oz", "ounce", "ounces" -> qty * 28.35
            "lb", "lbs", "pound", "pounds" -> qty * 453.59
            "l", "liter", "liters", "litre" -> qty * 1000
            "cup", "cups" -> qty * 240
            "tbsp", "tablespoon", "tablespoons" -> qty * 15
            "tsp", "teaspoon", "teaspoons" -> qty * 5
            else -> qty // g, ml, pcs — sudah base unit
        }
    }

    /**
     * Parse fraction string ke Double.
     * Mendukung: "1", "1.5", "1/2", "1 1/2", "¼", "½", "¾", "⅓", "⅔"
     */
    private fun parseFraction(input: String?): Double? {
        if (input.isNullOrBlank()) return null

        // Replace unicode fractions
        val normalized = input.trim()
            .replace("¼", "1/4").replace("½", "1/2").replace("¾", "3/4")
            .replace("⅓", "1/3").replace("⅔", "2/3")
            .replace("⅛", "1/8").replace("⅜", "3/8").replace("⅝", "5/8").replace("⅞", "7/8")
            // Remove garbage unicode chars (like "A½" → just take numeric part)
            .replace(Regex("[^0-9/. ]"), "").trim()

        if (normalized.isBlank()) return null

        // Try simple double parse first
        normalized.toDoubleOrNull()?.let { return it }

        val parts = normalized.split(" ").filter { it.isNotBlank() }
        return when {
            parts.size == 2 -> {
                // "1 1/2" → 1 + 0.5
                val whole = parts[0].toDoubleOrNull() ?: return null
                val frac = parseSingleFraction(parts[1]) ?: return null
                whole + frac
            }
            parts.size == 1 -> parseSingleFraction(parts[0])
            else -> null
        }
    }

    private fun parseSingleFraction(s: String): Double? {
        if ('/' !in s) return s.toDoubleOrNull()
        val slashIdx = s.indexOf('/')
        val num = s.substring(0, slashIdx).toDoubleOrNull() ?: return null
        val den = s.substring(slashIdx + 1).toDoubleOrNull() ?: return null
        if (den == 0.0) return null
        return num / den
    }
    
    // ======================================================================
    // INGREDIENT MATCHING HELPERS (shared logic with RecommendationService)
    // ======================================================================
    
    /**
     * Check if ingredient matches pantry item name.
     * Uses same logic as RecommendationService for consistency.
     */
    private fun isIngredientMatch(ingNorm: String, pantryNorm: String): Boolean {
        // Exact match
        if (ingNorm == pantryNorm) return true
        
        // Word boundary match: avoid "rice" matching "licorice"
        val ingWords = ingNorm.split(Regex("\\s+"))
        val pantryWords = pantryNorm.split(Regex("\\s+"))
        
        // Check if pantryName is a word in ingredient (e.g., "chicken" in "chicken breast")
        if (ingWords.any { it == pantryNorm }) return true
        
        // Check if ingredient is a word in pantryName (e.g., "breast" in "chicken breast")
        if (pantryWords.any { it == ingNorm }) return true
        
        // Fuzzy match for close variations (e.g., "tomato" vs "tomatoes")
        return isFuzzyMatch(ingNorm, pantryNorm)
    }
    
    /**
     * Fuzzy match untuk menangani plural/singular dan typo kecil.
     */
    private fun isFuzzyMatch(a: String, b: String): Boolean {
        // Handle plural: remove trailing 's', 'es'
        val aStem = a.removeSuffix("es").removeSuffix("s")
        val bStem = b.removeSuffix("es").removeSuffix("s")
        
        if (aStem == bStem) return true
        
        // Levenshtein distance for close matches
        val distance = levenshteinDistance(a, b)
        val maxLen = maxOf(a.length, b.length)
        if (maxLen == 0) return false
        
        val similarity = 1.0 - (distance.toDouble() / maxLen)
        return similarity >= 0.85 // 85% similarity threshold
    }
    
    /**
     * Calculate Levenshtein distance between two strings.
     */
    private fun levenshteinDistance(a: String, b: String): Int {
        val costs = IntArray(b.length + 1) { it }
        
        for (i in 1..a.length) {
            var lastValue = i
            for (j in 1..b.length) {
                val newValue = costs[j]
                costs[j] = if (a[i - 1] == b[j - 1]) {
                    lastValue
                } else {
                    1 + minOf(lastValue, newValue, costs[j - 1])
                }
                lastValue = newValue
            }
        }
        
        return costs[b.length]
    }
}
