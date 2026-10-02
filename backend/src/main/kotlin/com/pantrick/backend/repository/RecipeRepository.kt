package com.pantrick.backend.repository

import com.pantrick.backend.models.Recipe
import com.pantrick.backend.models.RecipeIngredient

interface RecipeRepository {
    fun getAllRecipes(limit: Int = 50, offset: Int = 0): List<Recipe>
    fun getTotalCount(): Int
    fun getRecipeById(id: String): Recipe?
    fun searchRecipes(query: String, limit: Int = 50, offset: Int = 0): List<Recipe>
    fun searchRecipesCount(query: String): Int
    fun getIngredientsByRecipeId(id: String): List<RecipeIngredient>?
}
