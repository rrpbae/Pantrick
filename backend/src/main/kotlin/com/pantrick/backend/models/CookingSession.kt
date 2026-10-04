package com.pantrick.backend.models

import kotlinx.serialization.Serializable

// ======================================================================
// COOKING SESSION — State Machine: STARTED → COMPLETED | CANCELLED
// ======================================================================

@Serializable
enum class CookingSessionStatus {
    STARTED,
    COMPLETED,
    CANCELLED
}

/**
 * Snapshot satu item pantry sebelum/sesudah digunakan untuk memasak.
 */
@Serializable
data class PantrySnapshot(
    val pantryItemId: String,
    val pantryItemName: String,
    val unit: String,
    val quantityBefore: Double,
    val quantityUsed: Double,
    val quantityAfter: Double
)

/**
 * Cooking session — satu sesi memasak oleh user untuk satu recipe.
 */
@Serializable
data class CookingSession(
    val id: String,
    val userId: Int,
    val recipeId: String,
    val recipeTitle: String,
    val status: CookingSessionStatus,
    val snapshots: List<PantrySnapshot> = emptyList(),
    val startedAt: String,
    val endedAt: String? = null
)

// ======================================================================
// REQUEST / RESPONSE DTOs
// ======================================================================

/**
 * Response satu cooking session.
 */
@Serializable
data class CookingSessionResponse(
    val success: Boolean,
    val message: String,
    val data: CookingSession? = null
)

/**
 * Ingredient detail yang tidak bisa dicocokkan quantity-nya.
 */
@Serializable
data class IngredientAvailability(
    val ingredientRaw: String,
    val pantryItemId: String?,
    val pantryItemName: String?,
    val pantryQty: Double?,
    val pantryUnit: String?,
    val recipeQtyStr: String?,
    val recipeUnit: String?,
    val recipeQtyParsed: Double?,
    val isSufficientQty: Boolean,      // qty cukup atau unit tidak bisa dibandingkan
    val isNameMatched: Boolean,        // nama bahan cocok di pantry
    val isQuantityComparable: Boolean  // unit bisa dibandingkan
)

/**
 * Response validasi ketersediaan bahan sebelum memasak.
 */
@Serializable
data class CookingReadinessResponse(
    val success: Boolean,
    val message: String,
    val recipeId: String,
    val recipeTitle: String,
    val canCook: Boolean,
    val ingredients: List<IngredientAvailability> = emptyList()
)
