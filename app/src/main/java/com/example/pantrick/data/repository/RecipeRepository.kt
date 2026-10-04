// [Materi: Repository Pattern] Abstraksi repositori untuk pengelolaan resep tersimpan
package com.example.pantrick.data.repository

import com.example.pantrick.data.local.PantrickPreferences
import com.example.pantrick.data.model.Recipe

class RecipeRepository(private val preferences: PantrickPreferences) {

    fun getSavedRecipes(email: String?): List<Recipe> {
        return preferences.getSavedRecipes(email)
    }

    fun saveRecipe(email: String?, recipe: Recipe) {
        preferences.saveRecipe(email, recipe)
    }

    fun removeSavedRecipe(email: String?, recipeId: String) {
        preferences.removeSavedRecipe(email, recipeId)
    }

    fun isRecipeSaved(email: String?, recipeId: String): Boolean {
        return preferences.isRecipeSaved(email, recipeId)
    }
}
