// [Materi: ViewModel & StateFlow] Pengelolaan state recommendation dari backend untuk HomeScreen
package com.example.pantrick.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pantrick.data.model.BackendRecommendation
import com.example.pantrick.data.repository.RecommendationApiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "HomeViewModel"

// [Materi: Sealed Interface State] Pemodelan status UI untuk recommendation
sealed interface RecommendationUiState {
    data object Idle : RecommendationUiState
    data object Loading : RecommendationUiState
    data class Success(val recommendations: List<BackendRecommendation>) : RecommendationUiState
    data class Error(val message: String) : RecommendationUiState
}

/**
 * ViewModel untuk HomeScreen yang mengambil recommendation dari backend API.
 * Recommendation diambil dari GET /api/recipes/recommendations dengan JWT authentication.
 * Backend akan mengambil pantry user dan melakukan matching dengan:
 * - IngredientMatchingService (alias matching, word boundary, fuzzy)
 * - QuantityComparisonService (quantity comparison dengan unit conversion)
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val recommendationRepository = RecommendationApiRepository()

    // [Materi: StateFlow UiState] Aliran status UI lengkap untuk recommendation
    private val _recommendationState = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Idle)
    val recommendationState: StateFlow<RecommendationUiState> = _recommendationState.asStateFlow()

    /**
     * Memuat recommendation dari backend berdasarkan pantry user.
     * Backend akan:
     * 1. Mengambil userId dari JWT token
     * 2. Fetch pantry user dari pantryRepository.getItemsByUserId(userId)
     * 3. Match dengan dataset 13,496 resep menggunakan IngredientMatchingService + QuantityComparisonService
     * 4. Return recommendations dengan matchedCount, missingIngredients, status, dll
     * 
     * @param jwtToken JWT token untuk authentication
     * @param limit Jumlah maksimal recommendations (default 10)
     * @param filter Filter mode: "all", "ready", "quick" (default "all")
     * @param sort Sort mode: "match", "time", "missing" (default "match")
     */
    fun loadRecommendations(
        jwtToken: String,
        limit: Int = 10,
        filter: String = "all",
        sort: String = "match"
    ) {
        if (jwtToken.isBlank()) {
            Log.w(TAG, "loadRecommendations blocked: JWT token kosong")
            _recommendationState.value = RecommendationUiState.Error("Autentikasi diperlukan")
            return
        }

        viewModelScope.launch {
            _recommendationState.value = RecommendationUiState.Loading
            Log.d(TAG, "Memuat recommendations dari backend (limit=$limit, filter=$filter, sort=$sort)")

            val result = recommendationRepository.getRecommendations(
                jwtToken = jwtToken,
                limit = limit,
                filter = filter,
                sort = sort
            )

            result.onSuccess { recommendations ->
                _recommendationState.value = RecommendationUiState.Success(recommendations)
                Log.d(TAG, "Berhasil memuat ${recommendations.size} recommendations dari backend")
            }.onFailure { exception ->
                _recommendationState.value = RecommendationUiState.Error(
                    exception.message ?: "Gagal memuat rekomendasi"
                )
                Log.e(TAG, "Gagal memuat recommendations", exception)
            }
        }
    }

    /**
     * Retry memuat recommendations setelah error.
     * Menggunakan parameter yang sama dengan pemanggilan terakhir.
     */
    fun retryLoad(jwtToken: String, limit: Int = 10, filter: String = "all", sort: String = "match") {
        loadRecommendations(jwtToken, limit, filter, sort)
    }

    /**
     * Reset state ke Idle.
     * Berguna saat user logout atau berpindah screen.
     */
    fun resetState() {
        _recommendationState.value = RecommendationUiState.Idle
        Log.d(TAG, "Recommendation state direset ke Idle")
    }
}
