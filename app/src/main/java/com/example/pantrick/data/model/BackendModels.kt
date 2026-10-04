// [Materi: Kotlinx Serialization DTO] Model data respon recommendation dari backend Pantrick
package com.example.pantrick.data.model

import kotlinx.serialization.Serializable

/**
 * Satu bahan resep dari dataset backend.
 * raw = string asli dari CSV, displayName = label tampilan, normalizedName = lowercase tanpa tanda baca.
 */
@Serializable
data class BackendRecipeIngredient(
    val raw: String = "",
    val displayName: String = "",
    val normalizedName: String = "",
    val quantity: String? = null,
    val unit: String? = null
)

/**
 * Model resep dari dataset backend.
 * Berbeda dari Recipe lokal — tidak menggunakan imageRes (Int) karena gambar dimuat dari URL.
 */
@Serializable
data class BackendRecipe(
    val id: String,
    val title: String,
    val instructions: String = "",
    val imageName: String? = null,
    val hasImage: Boolean = false,
    val ingredients: List<BackendRecipeIngredient> = emptyList(),
    val cookingTimeMinutes: Int? = null,
    val servings: Int? = null,
    val category: String? = null
)

/**
 * Satu entri rekomendasi dari endpoint search-by-ingredients.
 */
@Serializable
data class BackendRecommendation(
    val recipe: BackendRecipe,
    val matchedIngredients: List<String> = emptyList(),
    val missingIngredients: List<String> = emptyList(),
    val matchPercentage: Double = 0.0,
    val matchedCount: Int = 0,
    val totalIngredients: Int = 0,
    val missingIngredientCount: Int = 0,
    val status: String = "NOT_READY",
    val isSaved: Boolean = false,
    val usesExpiringItems: Boolean = false,
    val score: Double = 0.0
)

/**
 * Response dari POST /api/recipes/search-by-ingredients.
 */
@Serializable
data class IngredientSearchApiResponse(
    val success: Boolean = false,
    val message: String = "",
    val total: Int = 0,
    val limit: Int = 10,
    val offset: Int = 0,
    val recommendations: List<BackendRecommendation> = emptyList()
)

/**
 * Request body untuk POST /api/recipes/search-by-ingredients.
 */
@Serializable
data class IngredientSearchApiRequest(
    val ingredients: List<String>
)

/**
 * Response dari GET /api/recipes/{id}.
 */
@Serializable
data class RecipeDetailApiResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: BackendRecipe? = null
)
