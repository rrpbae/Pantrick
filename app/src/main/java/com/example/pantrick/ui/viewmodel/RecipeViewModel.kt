// [Materi: ViewModel & StateFlow] ViewModel untuk mengelola state resep tersimpan per-user
package com.example.pantrick.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.pantrick.data.local.PantrickPreferences
import com.example.pantrick.data.model.Recipe
import com.example.pantrick.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "RecipeViewModel"

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = PantrickPreferences(application)
    private val recipeRepository = RecipeRepository(preferences)

    private val _savedRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val savedRecipes: StateFlow<List<Recipe>> = _savedRecipes.asStateFlow()

    private var activeEmail: String? = null

    fun loadSavedRecipesForUser(email: String?) {
        activeEmail = email
        if (email.isNullOrBlank()) {
            _savedRecipes.value = emptyList()
            return
        }
        try {
            val list = recipeRepository.getSavedRecipes(email)
            _savedRecipes.value = list
            Log.d(TAG, "Berhasil memuat ${list.size} resep tersimpan untuk $email")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memuat resep tersimpan user: $email", e)
            _savedRecipes.value = emptyList()
        }
    }

    fun toggleSaveRecipe(recipe: Recipe) {
        val email = activeEmail ?: return
        val isAlreadySaved = _savedRecipes.value.any { it.id == recipe.id }
        if (isAlreadySaved) {
            recipeRepository.removeSavedRecipe(email, recipe.id)
            Log.d(TAG, "Menghapus resep dari tersimpan: ${recipe.title}")
        } else {
            recipeRepository.saveRecipe(email, recipe)
            Log.d(TAG, "Menyimpan resep: ${recipe.title}")
        }
        _savedRecipes.value = recipeRepository.getSavedRecipes(email)
    }

    fun isRecipeSaved(recipeId: String): Boolean {
        return _savedRecipes.value.any { it.id == recipeId }
    }
}
