package com.pantrick.backend.repository

import com.pantrick.backend.models.Recipe
import com.pantrick.backend.models.RecipeIngredient
import java.util.concurrent.ConcurrentHashMap

class InMemoryRecipeRepository(initialRecipes: List<Recipe> = emptyList()) : RecipeRepository {

    private val recipeMap = ConcurrentHashMap<String, Recipe>()
    private val recipesList = mutableListOf<Recipe>()

    init {
        initialRecipes.forEach { recipe ->
            recipeMap[recipe.id] = recipe
            recipesList.add(recipe)
        }
    }

    override fun getAllRecipes(limit: Int, offset: Int): List<Recipe> {
        if (offset >= recipesList.size) return emptyList()
        val toIndex = (offset + limit).coerceAtMost(recipesList.size)
        return recipesList.subList(offset, toIndex)
    }

    override fun getTotalCount(): Int {
        return recipesList.size
    }

    override fun getRecipeById(id: String): Recipe? {
        return recipeMap[id]
    }

    override fun searchRecipes(query: String, limit: Int, offset: Int): List<Recipe> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return getAllRecipes(limit, offset)

        val matches = recipesList.filter { recipe ->
            recipe.title.lowercase().contains(q) ||
            recipe.ingredients.any { ing ->
                ing.displayName.lowercase().contains(q) ||
                ing.normalizedName.contains(q)
            }
        }

        if (offset >= matches.size) return emptyList()
        val toIndex = (offset + limit).coerceAtMost(matches.size)
        return matches.subList(offset, toIndex)
    }

    override fun searchRecipesCount(query: String): Int {
        val q = query.trim().lowercase()
        if (q.isBlank()) return recipesList.size

        return recipesList.count { recipe ->
            recipe.title.lowercase().contains(q) ||
            recipe.ingredients.any { ing ->
                ing.displayName.lowercase().contains(q) ||
                ing.normalizedName.contains(q)
            }
        }
    }

    override fun getIngredientsByRecipeId(id: String): List<RecipeIngredient>? {
        return recipeMap[id]?.ingredients
    }
}
