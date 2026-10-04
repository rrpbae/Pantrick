// [Materi: DTO Cooking Session] Data Transfer Objects untuk fitur Memasak
package com.example.pantrick.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class CookingSessionStatus {
    STARTED,
    COMPLETED,
    CANCELLED
}

@Serializable
data class PantrySnapshotDto(
    val pantryItemId: String,
    val pantryItemName: String,
    val unit: String,
    val quantityBefore: Double,
    val quantityUsed: Double,
    val quantityAfter: Double
)

@Serializable
data class CookingSessionDto(
    val id: String,
    val userId: Int,
    val recipeId: String,
    val recipeTitle: String,
    val status: CookingSessionStatus,
    val snapshots: List<PantrySnapshotDto> = emptyList(),
    val startedAt: String,
    val endedAt: String? = null
)

@Serializable
data class CookingSessionResponse(
    val success: Boolean = false,
    val message: String = "",
    val data: CookingSessionDto? = null
)

@Serializable
data class IngredientAvailabilityDto(
    val ingredientRaw: String,
    val pantryItemId: String? = null,
    val pantryItemName: String? = null,
    val pantryQty: Double? = null,
    val pantryUnit: String? = null,
    val recipeQtyStr: String? = null,
    val recipeUnit: String? = null,
    val recipeQtyParsed: Double? = null,
    val isSufficientQty: Boolean = false,
    val isNameMatched: Boolean = false,
    val isQuantityComparable: Boolean = false
)

@Serializable
data class CookingReadinessDto(
    val success: Boolean = false,
    val message: String = "",
    val recipeId: String = "",
    val recipeTitle: String = "",
    val canCook: Boolean = false,
    val ingredients: List<IngredientAvailabilityDto> = emptyList()
)
