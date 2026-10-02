// [Materi: SharedPreferences & Kotlin Serialization] Pengelolaan penyimpanan lokal berbasis SharedPreferences dan JSON
package com.example.pantrick.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val TAG = "PantrickPreferences"

// [Materi: Data Persistence Abstraction] Pembungkus SharedPreferences dengan serialisasi JSON tanpa library Room/DataStore
class PantrickPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // [Materi: Kotlinx Serialization JSON Config]
    // ignoreUnknownKeys: mengabaikan field tak dikenal
    // coerceInputValues: menangani null atau nilai enum tak dikenal ke default parameter
    // encodeDefaults: selalu menuliskan nilai default ke JSON
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
    }

    companion object {
        private const val PREFS_NAME = "pantrick_local_prefs"
        private const val KEY_USERS = "registered_users"
        private const val KEY_PERSISTED_SESSION_EMAIL = "persisted_session_email"
        private const val KEY_PANTRY_PREFIX = "pantry_"

        // [Materi: In-Memory Session] Menyimpan sesi sementara jika "Ingat saya" tidak dicentang
        @Volatile
        private var inMemorySessionEmail: String? = null
    }

    // ==================== PENGELOLAAN PENGGUNA ====================

    // [Materi: JSON Deserialization] Mengambil daftar seluruh pengguna terdaftar
    fun getUsers(): List<User> {
        val rawJson = prefs.getString(KEY_USERS, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<User>>(rawJson)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengurai daftar pengguna dari SharedPreferences", e)
            emptyList()
        }
    }

    // [Materi: JSON Serialization] Menyimpan daftar pengguna yang telah diperbarui
    fun saveUsers(users: List<User>) {
        val rawJson = json.encodeToString(users)
        prefs.edit().putString(KEY_USERS, rawJson).apply()
    }

    // ==================== PENGELOLAAN SESI AKTIF ====================

    // [Materi: Sesi Bersyarat] Mengambil email sesi aktif (memori atau SharedPreferences)
    fun getActiveSessionEmail(): String? {
        return inMemorySessionEmail ?: prefs.getString(KEY_PERSISTED_SESSION_EMAIL, null)
    }

    // [Materi: Fitur Ingat Saya] Menyimpan sesi ke persistent storage atau memori saja
    fun setActiveSession(email: String?, rememberMe: Boolean) {
        if (email == null) {
            clearSession()
            return
        }
        val normalized = User.normalizeEmail(email)
        inMemorySessionEmail = normalized
        if (rememberMe) {
            prefs.edit().putString(KEY_PERSISTED_SESSION_EMAIL, normalized).apply()
        } else {
            prefs.edit().remove(KEY_PERSISTED_SESSION_EMAIL).apply()
        }
    }

    // [Materi: Logout / Pembersihan Sesi] Menghapus sesi baik dari memori maupun SharedPreferences
    fun clearSession() {
        inMemorySessionEmail = null
        prefs.edit().remove(KEY_PERSISTED_SESSION_EMAIL).apply()
    }

    // ==================== PENGELOLAAN BAHAN PANTRY PER USER ====================

    // [Materi: Data Per-User Key] Mengambil bahan khusus untuk email yang bersangkutan (melempar exception jika format korup)
    @Throws(Exception::class)
    fun getPantryItems(email: String): List<PantryItem> {
        val normalized = User.normalizeEmail(email)
        val rawJson = prefs.getString("$KEY_PANTRY_PREFIX$normalized", null) ?: return emptyList()
        return json.decodeFromString<List<PantryItem>>(rawJson)
    }

    // [Materi: Data Per-User Key] Menyimpan bahan khusus untuk email yang bersangkutan
    fun savePantryItems(email: String, items: List<PantryItem>) {
        val normalized = User.normalizeEmail(email)
        val rawJson = json.encodeToString(items)
        prefs.edit().putString("$KEY_PANTRY_PREFIX$normalized", rawJson).apply()
    }
}
