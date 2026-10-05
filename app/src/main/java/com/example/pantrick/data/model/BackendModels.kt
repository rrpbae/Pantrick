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


// ==================== PANTRY API DTOs ====================

/**
 * Response untuk GET /api/pantry/items
 */
@Serializable
data class PantryListResponse(
    val success: Boolean,
    val message: String,
    val total: Int,
    val data: List<PantryItemDto>
)

/**
 * Response untuk POST/PUT pantry item
 */
@Serializable
data class PantryItemResponse(
    val success: Boolean,
    val message: String,
    val data: PantryItemDto
)

/**
 * Pantry item dari backend API
 */
@Serializable
data class PantryItemDto(
    val id: String,
    val userId: Int,
    val name: String,
    val ingredientName: String,
    val normalizedName: String,
    val quantity: Double,
    val unit: String,
    val storageType: String,  // "FRIDGE", "FREEZER", "PANTRY"
    val purchasedAt: String? = null,
    val expiryDate: String? = null,
    val isConsumed: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)

/**
 * Request untuk POST /api/pantry/items
 */
@Serializable
data class AddPantryItemRequest(
    val name: String,
    val quantity: Double,
    val unit: String,
    val storageType: String,
    val purchasedAt: String? = null,
    val expirationDate: String? = null  // Backend expect 'expirationDate' not 'expiryDate'
)

/**
 * Request untuk PUT /api/pantry/items/{id}
 */
@Serializable
data class UpdatePantryItemRequest(
    val name: String? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val storageType: String? = null,
    val purchasedAt: String? = null,
    val expirationDate: String? = null,  // Backend expect 'expirationDate' not 'expiryDate'
    val isConsumed: Boolean? = null
)


// ==================== CONVERSION EXTENSIONS ====================

/**
 * Convert PantryItemDto (from backend) to PantryItem (local model)
 */
fun PantryItemDto.toLocalModel(): PantryItem {
    val storageLocation = when (storageType.uppercase()) {
        "FREEZER" -> StorageLocation.FREEZER
        else -> StorageLocation.KULKAS  // Map FRIDGE and PANTRY to KULKAS
    }
    
    // Parse expiry date to epoch day
    val expiryEpoch = try {
        if (!expiryDate.isNullOrBlank()) {
            java.time.LocalDate.parse(expiryDate).toEpochDay()
        } else {
            // Default: 7 days from now
            java.time.LocalDate.now().plusDays(7).toEpochDay()
        }
    } catch (e: Exception) {
        java.time.LocalDate.now().plusDays(7).toEpochDay()
    }
    
    // Format quantity label
    val qtyLabel = "$quantity $unit"
    
    return PantryItem(
        id = id,
        name = name,
        location = storageLocation,
        quantityLabel = qtyLabel,
        expiryEpochDay = expiryEpoch,
        category = FoodCategory.LAINNYA,  // Default category
        isExpiryEstimated = expiryDate.isNullOrBlank()
    )
}

/**
 * Convert StorageLocation (local) to backend storage type string
 */
fun StorageLocation.toBackendType(): String {
    return when (this) {
        StorageLocation.KULKAS -> "FRIDGE"
        StorageLocation.FREEZER -> "FREEZER"
    }
}

/**
 * Parse quantityLabel back to quantity Double (best effort)
 */
fun String.parseQuantity(): Double {
    return try {
        this.trim().split(" ").firstOrNull()?.toDoubleOrNull() ?: 1.0
    } catch (e: Exception) {
        1.0
    }
}

/**
 * Parse quantityLabel to get unit (best effort)
 */
fun String.parseUnit(): String {
    return try {
        val parts = this.trim().split(" ", limit = 2)
        if (parts.size > 1) parts[1] else "pcs"
    } catch (e: Exception) {
        "pcs"
    }
}
