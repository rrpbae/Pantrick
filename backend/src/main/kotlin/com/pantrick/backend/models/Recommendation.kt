package com.pantrick.backend.models

import kotlinx.serialization.Serializable

/**
 * DTO untuk satu rekomendasi resep, berisi Recipe asli dari repository
 * ditambah metadata matching terhadap pantry user.
 */
@Serializable
data class RecipeRecommendation(
    val recipe: Recipe,
    val matchedIngredients: List<String> = emptyList(),
    val missingIngredients: List<String> = emptyList(),
    val matchPercentage: Double,
    val matchedCount: Int,
    val totalIngredients: Int,
    val usesExpiringItems: Boolean = false,
    val score: Double
)

/**
 * Response untuk endpoint GET /api/recipes/recommendations
 */
@Serializable
data class RecommendationResponse(
    val success: Boolean,
    val message: String,
    val total: Int,
    val limit: Int,
    val pantryItemCount: Int,
    val recommendations: List<RecipeRecommendation> = emptyList()
)
