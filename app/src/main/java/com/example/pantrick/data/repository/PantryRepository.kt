// [Materi: Repository Pattern] Abstraksi data pantry per-pengguna yang terisolasi aman
package com.example.pantrick.data.repository

import android.util.Log
import com.example.pantrick.data.local.PantrickPreferences
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.StorageLocation

private const val TAG = "PantryRepository"

// [Materi: Repository Pattern] Mengelola operasi CRUD bahan makanan per akun pengguna
class PantryRepository(private val preferences: PantrickPreferences) {

    // [Materi: Data Retrieval Per-User] Mengambil daftar bahan spesifik milik email user (melempar exception jika format korup)
    @Throws(Exception::class)
    fun getItems(email: String): List<PantryItem> {
        return preferences.getPantryItems(email)
    }

    // [Materi: List Mutation & Persistence] Menambahkan bahan baru ke daftar pantry user
    fun addItem(email: String, item: PantryItem): List<PantryItem> {
        val currentItems = preferences.getPantryItems(email).toMutableList()
        currentItems.add(0, item) // Tempatkan bahan terbaru di paling atas
        preferences.savePantryItems(email, currentItems)
        Log.d(TAG, "Added item: ${item.name} for user: $email, total items: ${currentItems.size}")
        return currentItems
    }
    
    // [Materi: Batch Save] Simpan seluruh list items (untuk caching dari backend)
    fun saveItems(email: String, items: List<PantryItem>) {
        preferences.savePantryItems(email, items)
        Log.d(TAG, "Saved ${items.size} items for user: $email")
    }

    // [Materi: Update Item Action] Memperbarui bahan yang sudah ada dengan ID tetap
    fun updateItem(email: String, item: PantryItem): List<PantryItem> {
        val currentItems = preferences.getPantryItems(email)
        val updatedItems = currentItems.map { if (it.id == item.id) item else it }
        preferences.savePantryItems(email, updatedItems)
        Log.d(TAG, "Updated item id: ${item.id} (${item.name}) for user: $email")
        return updatedItems
    }

    // [Materi: Move Item Action] Memindahkan bahan ke lokasi penyimpanan baru
    fun moveItem(email: String, itemId: String, newLocation: StorageLocation): List<PantryItem> {
        val currentItems = preferences.getPantryItems(email)
        val updatedItems = currentItems.map {
            if (it.id == itemId) it.copy(location = newLocation) else it
        }
        preferences.savePantryItems(email, updatedItems)
        Log.d(TAG, "Moved item id: $itemId to $newLocation for user: $email")
        return updatedItems
    }

    // [Materi: List Filtering & Removal] Menghapus bahan berdasarkan ID
    fun deleteItem(email: String, itemId: String): List<PantryItem> {
        val currentItems = preferences.getPantryItems(email)
        val updatedItems = currentItems.filterNot { it.id == itemId }
        preferences.savePantryItems(email, updatedItems)
        Log.d(TAG, "Deleted item id: $itemId for user: $email, remaining items: ${updatedItems.size}")
        return updatedItems
    }

    // [Materi: Restore Action] Mengembalikan item yang dihapus (fungsi Undo)
    fun restoreItem(email: String, item: PantryItem): List<PantryItem> {
        val currentItems = preferences.getPantryItems(email).toMutableList()
        // Pastikan tidak ada duplikasi ID jika tombol undo ditekan
        if (currentItems.none { it.id == item.id }) {
            currentItems.add(0, item)
            preferences.savePantryItems(email, currentItems)
            Log.d(TAG, "Restored item: ${item.name} for user: $email, total items: ${currentItems.size}")
        }
        return currentItems
    }
}
