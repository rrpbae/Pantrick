package com.pantrick.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class RecipeIngredient(
    val raw: String,
    val displayName: String,
    val normalizedName: String,
    val quantity: String? = null,
    val unit: String? = null
)

@Serializable
data class Recipe(
    val id: String,
    val title: String,
    val instructions: String,
    val imageName: String? = null,
    val hasImage: Boolean = false,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val cookingTimeMinutes: Int? = null,
    val servings: Int? = null,
    val category: String? = null
)

@Serializable
data class RecipeListResponse(
    val success: Boolean,
    val message: String,
    val total: Int,
    val limit: Int,
    val offset: Int,
    val data: List<Recipe>
)

@Serializable
data class RecipeDetailResponse(
    val success: Boolean,
    val message: String,
    val data: Recipe? = null
)

@Serializable
data class RecipeIngredientsResponse(
    val success: Boolean,
    val message: String,
    val recipeId: String,
    val total: Int,
    val data: List<RecipeIngredient>
)
