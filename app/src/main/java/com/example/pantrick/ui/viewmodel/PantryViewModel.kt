// [Materi: ViewModel & StateFlow] Pengelolaan state daftar bahan pantry per-user dengan penanganan error
package com.example.pantrick.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pantrick.data.local.PantrickPreferences
import com.example.pantrick.data.model.AddPantryItemRequest
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.data.model.UpdatePantryItemRequest
import com.example.pantrick.data.repository.PantryApiRepository
import com.example.pantrick.data.repository.PantryRepository
import com.example.pantrick.util.ExpirationNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val TAG = "PantryViewModel"

// [Materi: Sealed Interface State] Pemodelan status UI layar Pantry (Loading, Success, Error)
sealed interface PantryUiState {
    data object Loading : PantryUiState
    data class Success(val items: List<PantryItem>) : PantryUiState
    data class Error(val message: String) : PantryUiState
}

// [Materi: AndroidViewModel Lifecycle] ViewModel untuk mengelola data bahan spesifik per pengguna
class PantryViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = PantrickPreferences(application)
    private val pantryRepository = PantryRepository(preferences)
    private val pantryApiRepository = PantryApiRepository()
    private val notificationManager = ExpirationNotificationManager(application)

    // [Materi: StateFlow UiState] Aliran status UI lengkap untuk layar Pantry
    private val _uiState = MutableStateFlow<PantryUiState>(PantryUiState.Loading)
    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()

    // [Materi: StateFlow Items] Aliran data bahan murni yang dipertahankan untuk kompatibilitas Home & Profile
    private val _items = MutableStateFlow<List<PantryItem>>(emptyList())
    val items: StateFlow<List<PantryItem>> = _items.asStateFlow()

    private var activeEmail: String? = null

    // [Materi: Dynamic User Data Sync] Sinkronisasi bahan saat user login atau berganti
    fun loadItemsForUser(email: String?) {
        activeEmail = email
        if (email.isNullOrBlank()) {
            _items.value = emptyList()
            _uiState.value = PantryUiState.Success(emptyList())
            Log.d(TAG, "User sesi kosong, membersihkan state pantry")
            return
        }

        _uiState.value = PantryUiState.Loading
        try {
            val loaded = pantryRepository.getItems(email)
            _items.value = loaded
            _uiState.value = PantryUiState.Success(loaded)
            Log.d(TAG, "Berhasil memuat ${loaded.size} bahan untuk $email")
            
            // [Materi: Expiration Notification Trigger] Cek bahan yang akan kedaluwarsa besok
            notificationManager.checkAndNotifyExpiringItems(email)
        } catch (e: Exception) {
            // [Materi: Exception Handling & Logging] Tangkap error parsing JSON dan ekspos state Error
            Log.e(TAG, "Gagal memuat pantry user: $email", e)
            _uiState.value = PantryUiState.Error("Gagal memuat data pantry. Format penyimpanan tidak valid.")
        }
    }

    // [Materi: Retry Action] Mencoba memuat ulang data saat terjadi error
    fun retryLoad() {
        loadItemsForUser(activeEmail)
    }

    // [Materi: Add Item Action] Menambahkan bahan baru ke pantry user aktif
    fun addItem(email: String?, item: PantryItem) {
        if (email.isNullOrBlank()) {
            Log.w(TAG, "Operasi addItem diblokir: User tidak aktif / null")
            return
        }
        try {
            // 1. Simpan ke local storage
            val updated = pantryRepository.addItem(email, item)
            _items.value = updated
            _uiState.value = PantryUiState.Success(updated)
            
            // 2. Sync ke backend API
            syncItemToBackend(item, "ADD")
            
            // 3. Cek notifikasi setelah add
            notificationManager.checkAndNotifyExpiringItems(email)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menambahkan item: ${item.name}", e)
            _uiState.value = PantryUiState.Error("Gagal menyimpan bahan baru.")
        }
    }

    // [Materi: Update Item Action] Memperbarui bahan yang sudah ada
    fun updateItem(email: String?, item: PantryItem) {
        if (email.isNullOrBlank()) {
            Log.w(TAG, "Operasi updateItem diblokir: User tidak aktif / null")
            return
        }
        try {
            // 1. Update di local storage
            val updated = pantryRepository.updateItem(email, item)
            _items.value = updated
            _uiState.value = PantryUiState.Success(updated)
            
            // 2. Sync ke backend API
            syncItemToBackend(item, "UPDATE")
            
            // 3. Cek notifikasi setelah update
            notificationManager.checkAndNotifyExpiringItems(email)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memperbarui item id: ${item.id}", e)
            _uiState.value = PantryUiState.Error("Gagal memperbarui bahan.")
        }
    }

    // [Materi: Move Item Action] Memindahkan lokasi penyimpanan bahan
    fun moveItem(email: String?, itemId: String, newLocation: StorageLocation) {
        if (email.isNullOrBlank()) {
            Log.w(TAG, "Operasi moveItem diblokir: User tidak aktif / null")
            return
        }
        try {
            val updated = pantryRepository.moveItem(email, itemId, newLocation)
            _items.value = updated
            _uiState.value = PantryUiState.Success(updated)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memindahkan item id: $itemId", e)
            _uiState.value = PantryUiState.Error("Gagal memindahkan bahan.")
        }
    }

    // [Materi: Delete Item Action] Menghapus bahan dari pantry user aktif
    fun deleteItem(email: String?, itemId: String) {
        if (email.isNullOrBlank()) {
            Log.w(TAG, "Operasi deleteItem diblokir: User tidak aktif / null")
            return
        }
        try {
            // 1. Hapus dari local storage
            val updated = pantryRepository.deleteItem(email, itemId)
            _items.value = updated
            _uiState.value = PantryUiState.Success(updated)
            
            // 2. Sync ke backend API
            syncDeleteToBackend(itemId)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal menghapus item id: $itemId", e)
            _uiState.value = PantryUiState.Error("Gagal menghapus bahan.")
        }
    }

    // [Materi: Restore Item Action] Mengembalikan bahan (fungsi Undo / Urungkan)
    fun restoreItem(email: String?, item: PantryItem) {
        if (email.isNullOrBlank()) {
            Log.w(TAG, "Operasi restoreItem diblokir: User tidak aktif / null")
            return
        }
        try {
            val updated = pantryRepository.restoreItem(email, item)
            _items.value = updated
            _uiState.value = PantryUiState.Success(updated)
            
            // Sync ke backend
            syncItemToBackend(item, "ADD")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengembalikan item: ${item.name}", e)
            _uiState.value = PantryUiState.Error("Gagal mengembalikan bahan.")
        }
    }

    // [Materi: Backend Sync] Sync pantry item ke backend untuk notifikasi
    private fun syncItemToBackend(item: PantryItem, operation: String) {
        viewModelScope.launch {
            val token = preferences.getJwtToken()
            if (token.isNullOrBlank()) {
                Log.w(TAG, "[SYNC] Token tidak tersedia, skip backend sync")
                return@launch
            }

            try {
                val backendType = when (item.location) {
                    StorageLocation.KULKAS -> "FRIDGE"
                    StorageLocation.FREEZER -> "FREEZER"
                }
                
                val expirationDateStr = LocalDate.ofEpochDay(item.expiryEpochDay).toString()
                
                Log.d(TAG, "[EXPIRY] Syncing item to backend: name=${item.name}, expirationDate=$expirationDateStr, daysRemaining=${item.daysLeft}")

                when (operation) {
                    "ADD" -> {
                        val request = AddPantryItemRequest(
                            name = item.name,
                            quantity = parseQuantity(item.quantityLabel),
                            unit = parseUnit(item.quantityLabel),
                            storageType = backendType,
                            expirationDate = expirationDateStr
                        )
                        val result = pantryApiRepository.addItem(token, request)
                        if (result.isSuccess) {
                            Log.d(TAG, "[SYNC] ADD berhasil: ${item.name}")
                        } else {
                            Log.e(TAG, "[SYNC] ADD gagal: ${result.exceptionOrNull()?.message}")
                        }
                    }
                    "UPDATE" -> {
                        val request = UpdatePantryItemRequest(
                            name = item.name,
                            quantity = parseQuantity(item.quantityLabel),
                            unit = parseUnit(item.quantityLabel),
                            storageType = backendType,
                            expirationDate = expirationDateStr
                        )
                        val result = pantryApiRepository.updateItem(token, item.id, request)
                        if (result.isSuccess) {
                            Log.d(TAG, "[SYNC] UPDATE berhasil: ${item.name}")
                        } else {
                            Log.e(TAG, "[SYNC] UPDATE gagal: ${result.exceptionOrNull()?.message}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "[SYNC] Exception during backend sync", e)
            }
        }
    }

    private fun syncDeleteToBackend(itemId: String) {
        viewModelScope.launch {
            val token = preferences.getJwtToken()
            if (token.isNullOrBlank()) {
                Log.w(TAG, "[SYNC] Token tidak tersedia, skip delete sync")
                return@launch
            }

            try {
                val result = pantryApiRepository.deleteItem(token, itemId)
                if (result.isSuccess) {
                    Log.d(TAG, "[SYNC] DELETE berhasil: $itemId")
                } else {
                    Log.e(TAG, "[SYNC] DELETE gagal: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "[SYNC] Exception during delete sync", e)
            }
        }
    }

    private fun parseQuantity(quantityLabel: String): Double {
        val parts = quantityLabel.trim().split(" ", limit = 2)
        return parts.firstOrNull()?.toDoubleOrNull() ?: 1.0
    }

    private fun parseUnit(quantityLabel: String): String {
        val parts = quantityLabel.trim().split(" ", limit = 2)
        return if (parts.size > 1) parts[1] else "buah"
    }
}
