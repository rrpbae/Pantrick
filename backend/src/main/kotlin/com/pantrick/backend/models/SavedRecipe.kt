package com.pantrick.backend.models

import kotlinx.serialization.Serializable

/**
 * Status kesiapan resep berdasarkan ketersediaan bahan di Pantry.
 */
enum class RecipeReadinessStatus {
    READY,      // 100% bahan tersedia (missingIngredientCount == 0)
    PARTIAL,    // Sebagian bahan tersedia (matchedCount > 0)
    NOT_READY   // Tidak ada bahan yang cocok (matchedCount == 0)
}

/**
 * Model data untuk resep yang disimpan (bookmarked) oleh user.
 */
@Serializable
data class SavedRecipe(
    val id: String,
    val userId: Int,
    val recipeId: String,
    val createdAt: String
)

/**
 * Model data untuk koleksi/folder resep milik user.
 */
@Serializable
data class RecipeCollection(
    val id: String,
    val userId: Int,
    val name: String,
    val recipeIds: List<String> = emptyList(),
    val createdAt: String,
    val updatedAt: String
)

/**
 * Model data untuk histori memasak user.
 */
@Serializable
data class CookingHistory(
    val id: String,
    val userId: Int,
    val recipeId: String,
    val cookedAt: String
)

/**
 * Model data untuk item dalam keranjang belanja / shopping list.
 */
@Serializable
data class ShoppingListItem(
    val id: String,
    val userId: Int,
    val ingredientName: String,
    val quantity: String? = null,
    val unit: String? = null,
    val recipeId: String? = null,
    val createdAt: String
)

/**
 * DTO request untuk membuat/mengubah koleksi.
 */
@Serializable
data class CreateCollectionRequest(
    val name: String
)

/**
 * DTO request untuk menambahkan item ke shopping list.
 */
@Serializable
data class AddShoppingListItemRequest(
    val ingredientName: String,
    val quantity: String? = null,
    val unit: String? = null,
    val recipeId: String? = null
)

/**
 * DTO request massal untuk menambahkan bahan kurang dari resep ke keranjang.
 */
@Serializable
data class AddMissingIngredientsRequest(
    val recipeId: String
)

/**
 * DTO Response untuk Smart Menu card.
 */
@Serializable
data class SmartMenuResponse(
    val success: Boolean = true,
    val availableRecipeCount: Int,
    val missingIngredientCount: Int,
    val readyRecipeCount: Int,
    val message: String
)

/**
 * DTO Response untuk Instant Dinner.
 */
@Serializable
data class InstantDinnerResponse(
    val success: Boolean = true,
    val message: String = "Berhasil mendapatkan rekomendasi Instant Dinner",
    val data: RecipeRecommendation? = null
)

/**
 * DTO Response untuk Filter Options.
 */
@Serializable
data class FilterOptionsResponse(
    val success: Boolean = true,
    val filters: List<String> = listOf("all", "ready", "quick"),
    val sortOptions: List<String> = listOf("match", "time", "missing")
)

/**
 * DTO Response untuk list Saved Recipes.
 */
@Serializable
data class SavedRecipeListResponse(
    val success: Boolean,
    val message: String,
    val total: Int,
    val data: List<Recipe>
)

/**
 * DTO Response untuk status save single recipe.
 */
@Serializable
data class RecipeSavedStatusResponse(
    val success: Boolean,
    val message: String,
    val recipeId: String,
    val isSaved: Boolean
)

/**
 * DTO Response untuk RecipeCollection.
 */
@Serializable
data class RecipeCollectionResponse(
    val success: Boolean,
    val message: String,
    val data: RecipeCollection? = null
)

@Serializable
data class RecipeCollectionListResponse(
    val success: Boolean,
    val message: String,
    val total: Int,
    val data: List<RecipeCollection>
)

/**
 * DTO Response untuk ShoppingList.
 */
@Serializable
data class ShoppingListResponse(
    val success: Boolean,
    val message: String,
    val total: Int,
    val data: List<ShoppingListItem>
)

/**
 * DTO Response untuk CookingHistory.
 */
@Serializable
data class CookingHistoryResponse(
    val success: Boolean,
    val message: String,
    val data: CookingHistory? = null
)
