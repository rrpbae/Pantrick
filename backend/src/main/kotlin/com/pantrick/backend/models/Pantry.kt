package com.pantrick.backend.models

import kotlinx.serialization.Serializable

@Serializable
enum class StorageType {
    FRIDGE,
    FREEZER,
    PANTRY;

    companion object {
        fun fromString(value: String?): StorageType {
            if (value.isNullOrBlank()) return PANTRY
            val trimmed = value.trim().uppercase()
            return when (trimmed) {
                "FRIDGE", "REFRIGERATOR" -> FRIDGE
                "FREEZER" -> FREEZER
                "PANTRY", "ROOM", "STORAGE" -> PANTRY
                else -> try {
                    valueOf(trimmed)
                } catch (e: Exception) {
                    PANTRY
                }
            }
        }
    }
}

@Serializable
enum class ExpirationStatus {
    EXPIRED,
    EXPIRING_SOON,
    FRESH,
    GOOD // retained for backward compatibility with existing tests
}

@Serializable
enum class ExpirationPriority {
    HIGH,
    MEDIUM,
    LOW
}

@Serializable
data class PantryItem(
    val id: String,
    val userId: Int,
    val name: String,
    val ingredientName: String = name, // Alias for backward compatibility with Home
    val normalizedName: String,
    val quantity: Double,
    val unit: String,
    val category: String = "Pantry", // e.g. Produce, Dairy & Eggs, Meat, Pantry, Beverages
    val storageType: StorageType = StorageType.PANTRY,
    val expirationDate: String? = null, // YYYY-MM-DD
    val imageUrl: String? = null,
    val isConsumed: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class PantryItemResponseDto(
    val id: String,
    val userId: Int,
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: String,
    val storageType: StorageType,
    val expirationDate: String? = null,
    val expirationStatus: ExpirationStatus? = null,
    val daysRemaining: Long? = null,
    val imageUrl: String? = null,
    val isConsumed: Boolean = false,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

@Serializable
data class CreatePantryItemRequest(
    val name: String? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val category: String? = null,
    val storageType: String? = null,
    val expirationDate: String? = null,
    val imageUrl: String? = null
)

@Serializable
data class UpdatePantryItemRequest(
    val name: String? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    val category: String? = null,
    val storageType: String? = null,
    val expirationDate: String? = null,
    val imageUrl: String? = null,
    val isConsumed: Boolean? = null
)

@Serializable
data class PantryItemSingleResponse(
    val success: Boolean,
    val message: String,
    val data: PantryItemResponseDto? = null
)

@Serializable
data class PantryDeleteResponse(
    val success: Boolean,
    val message: String
)

@Serializable
data class PantryItemListResponse(
    val success: Boolean,
    val message: String,
    val total: Int = 0,
    val data: List<PantryItemResponseDto> = emptyList()
)

@Serializable
data class ExpiringPantryItem(
    val id: String,
    val ingredientName: String,
    val quantity: Double,
    val unit: String,
    val expirationDate: String,
    val daysRemaining: Long,
    val status: ExpirationStatus,
    val priority: ExpirationPriority
)

@Serializable
data class StorageBreakdown(
    val fridge: Int = 0,
    val freezer: Int = 0,
    val room: Int = 0,
    val pantry: Int = room
)

@Serializable
data class PantrySummaryData(
    val fridge: Int = 0,
    val freezer: Int = 0,
    val pantry: Int = 0,
    val totalItems: Int = 0,
    val expiringSoonItems: Int = 0,
    val expiredItems: Int = 0
)

@Serializable
data class PantrySummaryResponse(
    val success: Boolean,
    val message: String,
    val data: PantrySummaryData
)

@Serializable
data class PantryCategoriesResponse(
    val success: Boolean,
    val message: String,
    val data: List<String> = emptyList()
)

@Serializable
data class PantrySummary(
    val totalItems: Int,
    val expiringSoonItems: Int,
    val storageBreakdown: StorageBreakdown,
    val categoryBreakdown: Map<String, Int> = emptyMap()
)

@Serializable
data class RecipePairing(
    val recipeId: String,
    val title: String,
    val imageName: String? = null,
    val hasImage: Boolean = false,
    val matchedIngredientsCount: Int,
    val totalIngredientsCount: Int,
    val matchPercentage: Double,
    val missingIngredients: List<String> = emptyList(),
    val matchedPantryItems: List<String> = emptyList(),
    val usesExpiringItems: Boolean = false,
    val score: Double = 0.0
)

@Serializable
data class HomeData(
    val user: UserData,
    val pantrySummary: PantrySummary,
    val categories: List<String>,
    val needsAttention: List<ExpiringPantryItem>,
    val recipePairings: List<RecipePairing>
)

@Serializable
data class HomeResponse(
    val success: Boolean,
    val message: String,
    val data: HomeData
)
