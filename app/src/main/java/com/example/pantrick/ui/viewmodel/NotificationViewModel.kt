// [Materi: ViewModel & StateFlow] ViewModel untuk managing notifikasi ekspirasi
package com.example.pantrick.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pantrick.data.model.NotificationItemDto
import com.example.pantrick.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val TAG = "NotificationVM"

sealed interface NotificationUiState {
    data object Idle : NotificationUiState
    data object Loading : NotificationUiState
    data class Success(
        val notifications: List<NotificationItemDto>,
        val unreadCount: Int
    ) : NotificationUiState
    data class Error(val message: String) : NotificationUiState
}

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NotificationRepository()

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Idle)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    fun loadNotifications(token: String) {
        viewModelScope.launch {
            _uiState.value = NotificationUiState.Loading
            Log.d(TAG, "Loading notifications...")

            val result = repository.getNotifications(token)
            _uiState.value = result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        Log.d(TAG, "Loaded ${response.notifications.size} notifications, unread=${response.unreadCount}")
                        NotificationUiState.Success(
                            notifications = response.notifications,
                            unreadCount = response.unreadCount
                        )
                    } else {
                        NotificationUiState.Error(response.message.ifBlank { "Gagal memuat notifikasi" })
                    }
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to load notifications", error)
                    NotificationUiState.Error("Gagal memuat notifikasi")
                }
            )
        }
    }

    fun markAsRead(notificationId: String, token: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            Log.d(TAG, "Marking notification as read: $notificationId")
            val result = repository.markAsRead(notificationId, token)
            result.onSuccess {
                if (it.success) {
                    Log.d(TAG, "Successfully marked as read")
                    onSuccess()
                    // Reload notifications to update state
                    loadNotifications(token)
                }
            }.onFailure { error ->
                Log.e(TAG, "Failed to mark as read", error)
            }
        }
    }

    fun reset() {
        _uiState.value = NotificationUiState.Idle
    }
}
