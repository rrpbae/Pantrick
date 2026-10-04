package com.pantrick.backend.service

import com.pantrick.backend.models.ExpiringPantryItem
import com.pantrick.backend.models.Recipe
import com.pantrick.backend.models.RecipeReadinessStatus
import com.pantrick.backend.models.RecipeRecommendation
import com.pantrick.backend.models.SmartMenuResponse
import com.pantrick.backend.repository.PantryRepository
import com.pantrick.backend.repository.RecipeRepository
import com.pantrick.backend.repository.SavedRecipeRepository
import kotlin.math.roundToInt

/**
 * RecommendationService — pusat logic rekomendasi resep berbasis Pantry user & Recipe Dataset.
 */
class RecommendationService(
    private val recipeRepository: RecipeRepository,
    private val pantryRepository: PantryRepository,
    private val savedRecipeRepository: SavedRecipeRepository? = null
) {

    /**
     * Menghasilkan daftar rekomendasi resep berbasis pantry user dengan dukungan
     * search, filter, sorting, maxTime, limit, dan offset.
     */
    fun getRecommendations(
        userId: Int,
        search: String? = null,
        filter: String = "all",
        sort: String = "match",
        maxTime: Int? = null,
        limit: Int = 10,
        offset: Int = 0,
        expiringItems: List<ExpiringPantryItem> = emptyList()
    ): Pair<List<RecipeRecommendation>, Int> {
        val safeLimit = limit.coerceIn(1, 100)
        val safeOffset = offset.coerceAtLeast(0)

        val userPantryItems = pantryRepository.getItemsByUserId(userId)
        val normSearchQuery = search?.trim()?.let { IngredientParser.normalizeIngredientName(it) }

        if (userPantryItems.isEmpty() && normSearchQuery.isNullOrBlank()) {
            return Pair(emptyList(), 0)
        }

        // Normalisasi nama bahan pantry user
        val pantryNormNames: Set<String> = userPantryItems
            .map { it.normalizedName.ifBlank { IngredientParser.normalizeIngredientName(it.name) } }
            .filter { it.isNotBlank() }
            .toSet()

        // Normalisasi nama bahan yang hampir kadaluarsa
        val expiringNormNames: Set<String> = expiringItems
            .map { IngredientParser.normalizeIngredientName(it.ingredientName) }
            .filter { it.isNotBlank() }
            .toSet()

        // Ambil semua resep dari repository (dataset asli)
        val allRecipes = recipeRepository.getAllRecipes(limit = Int.MAX_VALUE, offset = 0)

        val recommendations = mutableListOf<RecipeRecommendation>()

        for (recipe in allRecipes) {
            if (recipe.ingredients.isEmpty()) continue

            // 1. Search filter berdasarkan bahan/title jika search diset
            if (!normSearchQuery.isNullOrBlank()) {
                val matchesIngredient = recipe.ingredients.any { ing ->
                    val ingNorm = ing.normalizedName
                    ingNorm.contains(normSearchQuery) || normSearchQuery.contains(ingNorm)
                }
                val matchesTitle = IngredientParser.normalizeIngredientName(recipe.title).contains(normSearchQuery)
                if (!matchesIngredient && !matchesTitle) {
                    continue
                }
            }

            // 2. Filter maxTime jika diset
            if (maxTime != null && recipe.cookingTimeMinutes != null && recipe.cookingTimeMinutes > maxTime) {
                continue
            }

            // 3. Match terhadap pantry user
            val result = matchRecipeAgainstPantry(recipe, pantryNormNames, expiringNormNames, userId)

            // 4. Mode filter: "ready", "quick", "all"
            when (filter.lowercase()) {
                "ready" -> {
                    if (result.status != RecipeReadinessStatus.READY) continue
                }
                "quick" -> {
                    if (recipe.cookingTimeMinutes == null || recipe.cookingTimeMinutes >= 20) continue
                }
            }

            // Jika pantry tidak kosong dan tidak ada query search, hanya ambil yang minimal 1 match
            if (pantryNormNames.isNotEmpty() && result.matchedCount == 0 && normSearchQuery.isNullOrBlank()) {
                continue
            }

            recommendations.add(result)
        }

        // 5. Sorting logic
        val sorted = when (sort.lowercase()) {
            "time" -> recommendations.sortedWith(
                compareBy<RecipeRecommendation> { it.recipe.cookingTimeMinutes ?: Int.MAX_VALUE }
                    .thenByDescending { it.matchPercentage }
                    .thenBy { it.recipe.title }
            )
            "missing" -> recommendations.sortedWith(
                compareBy<RecipeRecommendation> { it.missingIngredientCount }
                    .thenByDescending { it.matchPercentage }
                    .thenBy { it.recipe.title }
            )
            else -> recommendations.sortedWith( // "match" / default
                compareByDescending<RecipeRecommendation> { it.score }
                    .thenByDescending { it.matchedCount }
                    .thenBy { it.recipe.title }
            )
        }

        val paged = sorted.drop(safeOffset).take(safeLimit)
        return Pair(paged, userPantryItems.size)
    }

    /**
     * Menghitung data untuk Smart Menu / Pembuat Menu Pintar Card.
     */
    fun getSmartMenu(userId: Int): SmartMenuResponse {
        val (allRecs, _) = getRecommendations(
            userId = userId,
            filter = "all",
            limit = 500
        )

        val readyCount = allRecs.count { it.status == RecipeReadinessStatus.READY }
        val missingIngredientCount = if (readyCount > 0) 0 else {
            allRecs.firstOrNull()?.missingIngredientCount ?: 0
        }

        val message = if (readyCount > 0) {
            "Kamu bisa memasak $readyCount resep dengan 0 bahan kurang hari ini!"
        } else if (allRecs.isNotEmpty()) {
            "Kamu dapat memasak ${allRecs.size} resep dengan sedikit bahan tambahan!"
        } else {
            "Tambahkan bahan ke Pantry kamu untuk melihat menu pintar hari ini!"
        }

        return SmartMenuResponse(
            success = true,
            availableRecipeCount = readyCount.ifZero(allRecs.size),
            missingIngredientCount = missingIngredientCount,
            readyRecipeCount = readyCount,
            message = message
        )
    }

    /**
     * Menghasilkan rekomendasi resep untuk Instant Dinner.
     */
    fun getInstantDinner(userId: Int): RecipeRecommendation? {
        val (recommendations, _) = getRecommendations(
            userId = userId,
            filter = "all",
            sort = "match",
            limit = 50
        )

        val readyInstant = recommendations.find { it.status == RecipeReadinessStatus.READY }
        if (readyInstant != null) return readyInstant

        return recommendations.firstOrNull()
    }

    /**
     * Mencocokkan satu recipe terhadap pantry user dan mengembalikan DTO RecipeRecommendation.
     */
    internal fun matchRecipeAgainstPantry(
        recipe: Recipe,
        pantryNormNames: Set<String>,
        expiringNormNames: Set<String> = emptySet(),
        userId: Int = 0
    ): RecipeRecommendation {
        val matchedIngredients = mutableListOf<String>()
        val missingIngredients = mutableListOf<String>()
        var usesExpiring = false

        for (ing in recipe.ingredients) {
            val ingNorm = ing.normalizedName

            val isMatched = pantryNormNames.any { pantryName ->
                ingNorm.contains(pantryName) || pantryName.contains(ingNorm)
            }

            if (isMatched) {
                matchedIngredients.add(ing.displayName)
                if (expiringNormNames.any { expName ->
                        ingNorm.contains(expName) || expName.contains(ingNorm)
                    }) {
                    usesExpiring = true
                }
            } else {
                missingIngredients.add(ing.displayName)
            }
        }

        val totalIngredients = recipe.ingredients.size
        val matchedCount = matchedIngredients.size
        val matchPercentage = if (totalIngredients > 0) {
            ((matchedCount.toDouble() / totalIngredients.toDouble()) * 100.0).roundTo(1)
        } else 0.0

        var score = matchPercentage + (matchedCount * 5.0)
        if (usesExpiring) score += 20.0
        score = score.roundTo(1)

        val status = when {
            missingIngredients.isEmpty() && totalIngredients > 0 -> RecipeReadinessStatus.READY
            matchedCount > 0 -> RecipeReadinessStatus.PARTIAL
            else -> RecipeReadinessStatus.NOT_READY
        }

        val isSaved = savedRecipeRepository?.isRecipeSaved(userId, recipe.id) ?: false

        return RecipeRecommendation(
            recipe = recipe,
            matchedIngredients = matchedIngredients,
            missingIngredients = missingIngredients,
            matchPercentage = matchPercentage,
            matchedCount = matchedCount,
            totalIngredients = totalIngredients,
            missingIngredientCount = missingIngredients.size,
            status = status,
            isSaved = isSaved,
            usesExpiringItems = usesExpiring,
            score = score
        )
    }

    /**
     * Menghasilkan rekomendasi berdasarkan daftar nama bahan yang dikirim langsung (tidak memerlukan pantry user).
     * Digunakan oleh endpoint search-by-ingredients tanpa autentikasi.
     */
    fun getRecommendationsByIngredientNames(
        ingredientNames: List<String>,
        limit: Int = 10,
        offset: Int = 0
    ): List<RecipeRecommendation> {
        if (ingredientNames.isEmpty()) return emptyList()

        val safeLimit = limit.coerceIn(1, 100)
        val safeOffset = offset.coerceAtLeast(0)

        val normNames: Set<String> = ingredientNames
            .map { IngredientParser.normalizeIngredientName(it) }
            .filter { it.isNotBlank() }
            .toSet()

        if (normNames.isEmpty()) return emptyList()

        val allRecipes = recipeRepository.getAllRecipes(limit = Int.MAX_VALUE, offset = 0)
        val results = mutableListOf<RecipeRecommendation>()

        for (recipe in allRecipes) {
            if (recipe.ingredients.isEmpty()) continue
            val rec = matchRecipeAgainstPantry(recipe, normNames)
            if (rec.matchedCount > 0) {
                results.add(rec)
            }
        }

        return results
            .sortedWith(
                compareByDescending<RecipeRecommendation> { it.score }
                    .thenByDescending { it.matchedCount }
                    .thenBy { it.recipe.title }
            )
            .drop(safeOffset)
            .take(safeLimit)
    }

    private fun Int.ifZero(default: Int): Int = if (this == 0) default else this

    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (this * multiplier).roundToInt() / multiplier
    }
}
