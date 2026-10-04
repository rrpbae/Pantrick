package com.pantrick.backend.models

import kotlinx.serialization.Serializable

/**
 * DTO untuk satu rekomendasi resep, berisi Recipe asli dari repository
 * ditambah metadata matching terhadap pantry user, status kesiapan, dan status tersimpan.
 */
@Serializable
data class RecipeRecommendation(
    val recipe: Recipe,
    val matchedIngredients: List<String> = emptyList(),
    val missingIngredients: List<String> = emptyList(),
    val matchPercentage: Double,
    val matchedCount: Int,
    val totalIngredients: Int,
    val missingIngredientCount: Int = missingIngredients.size,
    val status: RecipeReadinessStatus = if (missingIngredients.isEmpty()) RecipeReadinessStatus.READY else if (matchedCount > 0) RecipeReadinessStatus.PARTIAL else RecipeReadinessStatus.NOT_READY,
    val isSaved: Boolean = false,
    val usesExpiringItems: Boolean = false,
    val score: Double
)

/**
 * Response untuk endpoint GET /api/recipes/recommendations & /api/recipes/recommended
 */
@Serializable
data class RecommendationResponse(
    val success: Boolean,
    val message: String,
    val total: Int,
    val limit: Int = 10,
    val offset: Int = 0,
    val pantryItemCount: Int,
    val recommendations: List<RecipeRecommendation> = emptyList(),
    val data: List<RecipeRecommendation> = recommendations
)

/**
 * Request body untuk POST /api/recipes/search-by-ingredients (tanpa autentikasi).
 */
@Serializable
data class IngredientSearchRequest(
    val ingredients: List<String> = emptyList()
)

/**
 * Response untuk POST /api/recipes/search-by-ingredients.
 */
@Serializable
data class IngredientSearchResponse(
    val success: Boolean,
    val message: String,
    val total: Int,
    val limit: Int = 10,
    val offset: Int = 0,
    val recommendations: List<RecipeRecommendation> = emptyList()
)
