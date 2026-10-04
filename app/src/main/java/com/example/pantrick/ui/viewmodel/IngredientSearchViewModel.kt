// [Materi: ViewModel & StateFlow] ViewModel untuk mengelola pencarian rekomendasi resep dari backend
package com.example.pantrick.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pantrick.data.model.BackendRecommendation
import com.example.pantrick.data.model.BackendRecipe
import com.example.pantrick.data.repository.RecommendationApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "IngredientSearchVM"

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data class Success(val recommendations: List<BackendRecommendation>) : SearchUiState
    data class Error(val message: String) : SearchUiState
}

sealed interface RecipeDetailUiState {
    data object Idle : RecipeDetailUiState
    data object Loading : RecipeDetailUiState
    data class Success(val recipe: BackendRecipe) : RecipeDetailUiState
    data class Error(val message: String) : RecipeDetailUiState
    data object NotFound : RecipeDetailUiState
}

/**
 * ViewModel untuk layar AddIngredientScreen (pencarian resep berdasarkan bahan pilihan user)
 * dan RecipeDetailScreen (detail resep dari dataset backend).
 */
class IngredientSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RecommendationApiRepository()

    // State untuk hasil pencarian rekomendasi
    private val _searchState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val searchState: StateFlow<SearchUiState> = _searchState.asStateFlow()

    // State untuk detail resep
    private val _detailState = MutableStateFlow<RecipeDetailUiState>(RecipeDetailUiState.Idle)
    val detailState: StateFlow<RecipeDetailUiState> = _detailState.asStateFlow()

    /**
     * Mencari rekomendasi resep dari backend berdasarkan bahan yang dipilih user.
     * Bahan dalam Bahasa Inggris sesuai dataset.
     */
    fun searchRecommendations(ingredients: Set<String>, limit: Int = 10) {
        if (ingredients.isEmpty()) return

        viewModelScope.launch {
            _searchState.value = SearchUiState.Loading
            Log.d(TAG, "Mencari rekomendasi untuk bahan: $ingredients")

            val result = repository.searchByIngredients(
                ingredients = ingredients.toList(),
                limit = limit
            )

            _searchState.value = result.fold(
                onSuccess = { recommendations ->
                    Log.d(TAG, "Hasil: ${recommendations.size} rekomendasi")
                    SearchUiState.Success(recommendations)
                },
                onFailure = { error ->
                    Log.e(TAG, "Gagal mengambil rekomendasi", error)
                    SearchUiState.Error(
                        "Gagal terhubung ke server. Pastikan backend berjalan dan periksa koneksi."
                    )
                }
            )
        }
    }

    /**
     * Mengambil detail resep dari backend berdasarkan ID.
     * Dipanggil saat user menekan "Masak Sekarang" atau membuka detail resep hasil recommendation.
     */
    fun loadRecipeDetail(recipeId: String) {
        viewModelScope.launch {
            _detailState.value = RecipeDetailUiState.Loading
            Log.d(TAG, "Memuat detail resep: $recipeId")

            val result = repository.getRecipeById(recipeId)

            _detailState.value = result.fold(
                onSuccess = { recipe ->
                    if (recipe != null) {
                        Log.d(TAG, "Detail resep berhasil dimuat: ${recipe.title}")
                        RecipeDetailUiState.Success(recipe)
                    } else {
                        Log.w(TAG, "Resep dengan ID '$recipeId' tidak ditemukan")
                        RecipeDetailUiState.NotFound
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Gagal memuat detail resep $recipeId", error)
                    RecipeDetailUiState.Error(
                        "Gagal memuat detail resep. Pastikan backend berjalan."
                    )
                }
            )
        }
    }

    /** Reset state pencarian ke kondisi awal (Idle) */
    fun resetSearch() {
        _searchState.value = SearchUiState.Idle
    }

    /** Reset state detail ke kondisi awal (Idle) */
    fun resetDetail() {
        _detailState.value = RecipeDetailUiState.Idle
    }
}
