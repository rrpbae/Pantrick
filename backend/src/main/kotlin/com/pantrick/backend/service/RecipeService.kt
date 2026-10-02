package com.pantrick.backend.service

import com.pantrick.backend.models.Recipe
import com.pantrick.backend.models.RecipeIngredient
import com.pantrick.backend.repository.RecipeRepository

class RecipeService(private val repository: RecipeRepository) {

    fun getRecipes(limit: Int = 50, offset: Int = 0): Pair<List<Recipe>, Int> {
        val safeLimit = limit.coerceIn(1, 200)
        val safeOffset = offset.coerceAtLeast(0)
        val list = repository.getAllRecipes(safeLimit, safeOffset)
        val total = repository.getTotalCount()
        return Pair(list, total)
    }

    fun getRecipeById(id: String): Recipe? {
        return repository.getRecipeById(id)
    }

    fun searchRecipes(query: String, limit: Int = 50, offset: Int = 0): Pair<List<Recipe>, Int> {
        val safeLimit = limit.coerceIn(1, 200)
        val safeOffset = offset.coerceAtLeast(0)
        val list = repository.searchRecipes(query, safeLimit, safeOffset)
        val total = repository.searchRecipesCount(query)
        return Pair(list, total)
    }

    fun getIngredientsByRecipeId(id: String): List<RecipeIngredient>? {
        return repository.getIngredientsByRecipeId(id)
    }
}
