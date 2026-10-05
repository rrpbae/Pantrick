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

                // Calculate quantity to use based on recipe requirement
                val qtyUsed = if (avail.isQuantityComparable && avail.recipeQtyParsed != null) {
                    // Convert recipe quantity to pantry unit for proper deduction
                    val recipeUnit = avail.recipeUnit ?: "pcs"
                    val recipeInBaseUnit = QuantityComparisonService.toBaseUnit(avail.recipeQtyParsed, recipeUnit)
                    val pantryInBaseUnit = QuantityComparisonService.toBaseUnit(pantryItem.quantity, pantryItem.unit)
                    
                    // Calculate how much to deduct in pantry's unit
                    val unitGroup = QuantityComparisonService.getUnitGroup(pantryItem.unit)
                    if (unitGroup != null && unitGroup == QuantityComparisonService.getUnitGroup(recipeUnit)) {
                        // Units compatible - calculate proportional deduction
                        val ratio = recipeInBaseUnit / pantryInBaseUnit
                        pantryItem.quantity * ratio
                    } else {
                        // Units not compatible - use recipe quantity directly
                        avail.recipeQtyParsed
                    }
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

        // DEBUG LOG
        println("DEBUG [buildAvailability] Recipe ingredient: raw='${ing.raw}', normalized='$ingNorm', qty='${ing.quantity}', unit='${ing.unit}'")
        println("DEBUG [buildAvailability] Checking against ${pantryItems.size} pantry items")

        // Use shared IngredientMatchingService for consistency with RecommendationService
        val matched = pantryItems
            .filter { !it.isConsumed && it.quantity > 0 }
            .firstOrNull { pantry ->
                val pNorm = pantry.normalizedName.ifBlank { 
                    IngredientParser.normalizeIngredientName(pantry.name) 
                }
                println("DEBUG [buildAvailability]   Pantry: name='${pantry.name}', normalized='$pNorm', qty=${pantry.quantity}, unit='${pantry.unit}'")
                val isMatch = IngredientMatchingService.isIngredientMatch(ingNorm, pNorm)
                println("DEBUG [buildAvailability]   Match result: $isMatch")
                isMatch
            }

        if (matched == null) {
            println("DEBUG [buildAvailability] NO MATCH FOUND for '${ing.raw}'")
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

        println("DEBUG [buildAvailability] MATCHED with pantry item: id='${matched.id}', name='${matched.name}'")

        // Use shared QuantityComparisonService for quantity comparison
        val qtyComparison = QuantityComparisonService.compareQuantities(
            recipeQty = ing.quantity,
            recipeUnit = ing.unit,
            pantryQty = matched.quantity,
            pantryUnit = matched.unit
        )

        println("DEBUG [buildAvailability] Quantity comparison: comparable=${qtyComparison.isComparable}, sufficient=${qtyComparison.isSufficient}")

        return IngredientAvailability(
            ingredientRaw = ing.raw,
            pantryItemId = matched.id,
            pantryItemName = matched.name,
            pantryQty = matched.quantity,
            pantryUnit = matched.unit,
            recipeQtyStr = ing.quantity,
            recipeUnit = ing.unit,
            recipeQtyParsed = qtyComparison.recipeQtyParsed,
            isSufficientQty = qtyComparison.isSufficient,
            isNameMatched = true,
            isQuantityComparable = qtyComparison.isComparable
        )
    }
}
