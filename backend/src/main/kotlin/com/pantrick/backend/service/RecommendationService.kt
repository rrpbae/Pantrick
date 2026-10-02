package com.pantrick.backend.service

import com.pantrick.backend.models.ExpiringPantryItem
import com.pantrick.backend.models.PantryItem
import com.pantrick.backend.models.Recipe
import com.pantrick.backend.models.RecipeRecommendation
import com.pantrick.backend.repository.PantryRepository
import com.pantrick.backend.repository.RecipeRepository
import kotlin.math.roundToInt

/**
 * RecommendationService — pusat logic rekomendasi resep berbasis Pantry user.
 *
 * Algoritma:
 * 1. Ambil semua item pantry user berdasarkan userId (JWT).
 * 2. Normalisasi nama bahan pantry → lowercase, trimmed, no punctuation.
 * 3. Untuk setiap Recipe di RecipeRepository:
 *    - Hitung matchedIngredients: ingredient recipe yang ada di pantry (flexible substring match).
 *    - Hitung missingIngredients: sisanya.
 *    - Hitung matchPercentage = matchedCount / totalIngredients * 100.
 *    - Hitung score = matchPercentage + (matchedCount * 5.0) + 20.0 jika bahan mau kadaluarsa.
 * 4. Filter: minimal 1 ingredient cocok.
 * 5. Urutkan: score DESC → matchedCount DESC → title ASC (deterministik).
 * 6. Ambil top-N sesuai limit.
 *
 * Semua Recipe WAJIB berasal dari RecipeRepository (dataset asli).
 * TIDAK ada recipe dummy atau hardcode.
 */
class RecommendationService(
    private val recipeRepository: RecipeRepository,
    private val pantryRepository: PantryRepository
) {

    /**
     * Menghasilkan daftar rekomendasi resep berdasarkan pantry user yang sedang login.
     *
     * @param userId      ID user dari JWT
     * @param limit       Jumlah maksimal rekomendasi (1–100, default 10)
     * @param expiringItems Daftar item yang hampir kadaluarsa (opsional, dari HomeService/PantryService)
     * @return Pair<List<RecipeRecommendation>, pantryItemCount>
     */
    fun getRecommendations(
        userId: Int,
        limit: Int = 10,
        expiringItems: List<ExpiringPantryItem> = emptyList()
    ): Pair<List<RecipeRecommendation>, Int> {
        val safeLimit = limit.coerceIn(1, 100)

        val userPantryItems = pantryRepository.getItemsByUserId(userId)
        if (userPantryItems.isEmpty()) {
            return Pair(emptyList(), 0)
        }

        // Normalisasi nama bahan pantry user → set untuk lookup cepat
        val pantryNormNames: Set<String> = userPantryItems
            .map { it.normalizedName.ifBlank { IngredientParser.normalizeIngredientName(it.name) } }
            .filter { it.isNotBlank() }
            .toSet()

        if (pantryNormNames.isEmpty()) {
            return Pair(emptyList(), userPantryItems.size)
        }

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

            val result = matchRecipeAgainstPantry(recipe, pantryNormNames, expiringNormNames)
            if (result.matchedCount > 0) {
                recommendations.add(result)
            }
        }

        // Urutkan: score DESC → matchedCount DESC → title ASC (deterministik tie-breaker)
        val sorted = recommendations.sortedWith(
            compareByDescending<RecipeRecommendation> { it.score }
                .thenByDescending { it.matchedCount }
                .thenBy { it.recipe.title }
        )

        return Pair(sorted.take(safeLimit), userPantryItems.size)
    }

    /**
     * Mencocokkan satu recipe terhadap pantry user.
     * Menggunakan flexible substring match: ingredient cocok jika:
     *   - normalizedIngredient mengandung pantryName ATAU
     *   - pantryName mengandung normalizedIngredient
     * Ini memungkinkan matching antara "butter" (pantry) dan "unsalted butter" (recipe), dll.
     */
    internal fun matchRecipeAgainstPantry(
        recipe: Recipe,
        pantryNormNames: Set<String>,
        expiringNormNames: Set<String> = emptySet()
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
                // Cek apakah ingredient ini termasuk yang hampir kadaluarsa
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

        // Scoring:
        // Base = matchPercentage (0–100)
        // Bonus per matched ingredient = +5.0 (mendorong resep dengan lebih banyak bahan cocok)
        // Bonus expiring items = +20.0 (prioritaskan bahan yang segera kadaluarsa)
        var score = matchPercentage + (matchedCount * 5.0)
        if (usesExpiring) score += 20.0
        score = score.roundTo(1)

        return RecipeRecommendation(
            recipe = recipe,
            matchedIngredients = matchedIngredients,
            missingIngredients = missingIngredients,
            matchPercentage = matchPercentage,
            matchedCount = matchedCount,
            totalIngredients = totalIngredients,
            usesExpiringItems = usesExpiring,
            score = score
        )
    }

    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (this * multiplier).roundToInt() / multiplier
    }
}
