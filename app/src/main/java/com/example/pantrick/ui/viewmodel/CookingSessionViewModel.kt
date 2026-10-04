// [Materi: ViewModel StateFlow] ViewModel untuk mengelola sesi memasak
package com.example.pantrick.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pantrick.data.model.CookingReadinessDto
import com.example.pantrick.data.model.CookingSessionDto
import com.example.pantrick.data.repository.CookingSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "CookingSessionVM"

sealed interface CookingUiState {
    data object Idle : CookingUiState
    data object CheckingReadiness : CookingUiState
    data class ReadinessResult(val readiness: CookingReadinessDto) : CookingUiState
    data object Starting : CookingUiState
    data class Active(val session: CookingSessionDto) : CookingUiState
    data object Cancelling : CookingUiState
    data object Cancelled : CookingUiState
    data object Completing : CookingUiState
    data class Completed(val session: CookingSessionDto) : CookingUiState
    data class Error(val message: String) : CookingUiState
}

class CookingSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CookingSessionRepository()

    private val _cookingState = MutableStateFlow<CookingUiState>(CookingUiState.Idle)
    val cookingState: StateFlow<CookingUiState> = _cookingState.asStateFlow()

    // ======================================================================
    // CHECK READINESS
    // ======================================================================

    fun checkReadiness(recipeId: String, token: String) {
        viewModelScope.launch {
            _cookingState.value = CookingUiState.CheckingReadiness
            Log.d(TAG, "checkReadiness($recipeId)")

            val result = repository.checkReadiness(recipeId, token)
            _cookingState.value = result.fold(
                onSuccess = { readiness ->
                    Log.d(TAG, "canCook=${readiness.canCook} ingredients=${readiness.ingredients.size}")
                    CookingUiState.ReadinessResult(readiness)
                },
                onFailure = { e ->
                    Log.e(TAG, "checkReadiness failed", e)
                    CookingUiState.Error("Gagal memeriksa ketersediaan bahan")
                }
            )
        }
    }

    // ======================================================================
    // START COOKING
    // ======================================================================

    fun startCooking(recipeId: String, token: String) {
        val current = _cookingState.value
        // Cegah double-tap: jangan mulai jika sudah Starting atau Active
        if (current is CookingUiState.Starting || current is CookingUiState.Active) {
            Log.w(TAG, "startCooking ignored — already in state $current")
            return
        }

        viewModelScope.launch {
            _cookingState.value = CookingUiState.Starting
            Log.d(TAG, "startCooking($recipeId)")

            val result = repository.startCooking(recipeId, token)
            _cookingState.value = result.fold(
                onSuccess = { resp ->
                    val session = resp.data
                    if (resp.success && session != null) {
                        Log.d(TAG, "Session started: ${session.id}")
                        CookingUiState.Active(session)
                    } else {
                        CookingUiState.Error(resp.message.ifBlank { "Gagal memulai sesi memasak" })
                    }
                },
                onFailure = { e ->
                    Log.e(TAG, "startCooking failed", e)
                    CookingUiState.Error("Gagal memulai memasak")
                }
            )
        }
    }

    // ======================================================================
    // CANCEL COOKING
    // ======================================================================

    fun cancelCooking(token: String, onPantryRefresh: () -> Unit = {}) {
        val current = _cookingState.value
        if (current !is CookingUiState.Active) {
            Log.w(TAG, "cancelCooking ignored — not in Active state")
            return
        }

        viewModelScope.launch {
            _cookingState.value = CookingUiState.Cancelling
            Log.d(TAG, "cancelCooking(${current.session.id})")

            val result = repository.cancelCooking(current.session.id, token)
            _cookingState.value = result.fold(
                onSuccess = { resp ->
                    if (resp.success) {
                        Log.d(TAG, "Session cancelled: ${resp.data?.id}")
                        onPantryRefresh()
                        CookingUiState.Cancelled
                    } else {
                        CookingUiState.Error(resp.message.ifBlank { "Gagal membatalkan sesi" })
                    }
                },
                onFailure = { e ->
                    Log.e(TAG, "cancelCooking failed", e)
                    CookingUiState.Error("Gagal membatalkan sesi memasak")
                }
            )
        }
    }

    // ======================================================================
    // COMPLETE COOKING
    // ======================================================================

    fun completeCooking(token: String, onPantryRefresh: () -> Unit = {}) {
        val current = _cookingState.value
        if (current !is CookingUiState.Active) {
            Log.w(TAG, "completeCooking ignored — not in Active state")
            return
        }

        viewModelScope.launch {
            _cookingState.value = CookingUiState.Completing
            Log.d(TAG, "completeCooking(${current.session.id})")

            val result = repository.completeCooking(current.session.id, token)
            _cookingState.value = result.fold(
                onSuccess = { resp ->
                    val session = resp.data
                    if (resp.success && session != null) {
                        Log.d(TAG, "Session completed: ${session.id}")
                        onPantryRefresh()
                        CookingUiState.Completed(session)
                    } else {
                        CookingUiState.Error(resp.message.ifBlank { "Gagal menyelesaikan sesi" })
                    }
                },
                onFailure = { e ->
                    Log.e(TAG, "completeCooking failed", e)
                    CookingUiState.Error("Gagal menyelesaikan sesi memasak")
                }
            )
        }
    }

    // ======================================================================
    // RESET
    // ======================================================================

    fun reset() {
        _cookingState.value = CookingUiState.Idle
    }

    /** Cek jika ada sesi aktif yang tersimpan (saat screen dibuka kembali) */
    fun resumeActiveSession(recipeId: String, token: String) {
        viewModelScope.launch {
            val result = repository.getActiveSession(recipeId, token)
            result.onSuccess { resp ->
                val session = resp.data
                if (resp.success && session != null) {
                    Log.d(TAG, "Resuming active session: ${session.id}")
                    _cookingState.value = CookingUiState.Active(session)
                }
            }
        }
    }
}
