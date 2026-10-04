// [Materi: SharedPreferences & Kotlin Serialization] Pengelolaan penyimpanan lokal berbasis SharedPreferences dan JSON
package com.example.pantrick.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.Recipe
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
        private const val KEY_JWT_TOKEN = "jwt_token"
        private const val KEY_PANTRY_PREFIX = "pantry_"
        private const val KEY_EXPIRY_REMINDER_PREFIX = "expiry_reminder_"
        private const val KEY_PROFILE_IMAGE_PREFIX = "profile_image_"
        private const val KEY_SAVED_RECIPES_PREFIX = "saved_recipes_"

        // [Materi: In-Memory Session] Menyimpan sesi sementara jika "Ingat saya" tidak dicentang
        @Volatile
        private var inMemorySessionEmail: String? = null
        
        @Volatile
        private var inMemoryJwtToken: String? = null
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

    // [Materi: Update Profil] Memperbarui nama lengkap pengguna di penyimpanan lokal
    fun updateUserName(email: String, newFullName: String): User? {
        val normalized = User.normalizeEmail(email)
        val users = getUsers().toMutableList()
        val index = users.indexOfFirst { it.email == normalized }
        if (index == -1) return null
        val updatedUser = users[index].copy(fullName = newFullName.trim())
        users[index] = updatedUser
        saveUsers(users)
        return updatedUser
    }

    // [Materi: Update Kata Sandi] Memperbarui hash kata sandi pengguna
    fun updateUserPassword(email: String, newPasswordHash: String): User? {
        val normalized = User.normalizeEmail(email)
        val users = getUsers().toMutableList()
        val index = users.indexOfFirst { it.email == normalized }
        if (index == -1) return null
        val updatedUser = users[index].copy(passwordHash = newPasswordHash)
        users[index] = updatedUser
        saveUsers(users)
        return updatedUser
    }

    // ==================== PENGELOLAAN SESI AKTIF ====================

    // [Materi: Sesi Bersyarat] Mengambil email sesi aktif (memori atau SharedPreferences)
    fun getActiveSessionEmail(): String? {
        return inMemorySessionEmail ?: prefs.getString(KEY_PERSISTED_SESSION_EMAIL, null)
    }

    // [Materi: Fitur Ingat Saya] Menyimpan sesi ke persistent storage atau memori saja
    fun setActiveSession(email: String?, rememberMe: Boolean, jwtToken: String? = null) {
        if (email == null) {
            clearSession()
            return
        }
        val normalized = User.normalizeEmail(email)
        inMemorySessionEmail = normalized
        inMemoryJwtToken = jwtToken
        if (rememberMe) {
            prefs.edit().putString(KEY_PERSISTED_SESSION_EMAIL, normalized).apply()
            if (jwtToken != null) {
                prefs.edit().putString(KEY_JWT_TOKEN, jwtToken).apply()
            }
        } else {
            prefs.edit().remove(KEY_PERSISTED_SESSION_EMAIL).apply()
            prefs.edit().remove(KEY_JWT_TOKEN).apply()
        }
    }

    // [Materi: JWT Token] Mengambil JWT token untuk API calls
    fun getJwtToken(): String? {
        return inMemoryJwtToken ?: prefs.getString(KEY_JWT_TOKEN, null)
    }

    // [Materi: Logout / Pembersihan Sesi] Menghapus sesi baik dari memori maupun SharedPreferences
    fun clearSession() {
        inMemorySessionEmail = null
        inMemoryJwtToken = null
        prefs.edit().remove(KEY_PERSISTED_SESSION_EMAIL).apply()
        prefs.edit().remove(KEY_JWT_TOKEN).apply()
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

    // ==================== PENGELOLAAN PREFERENSI PENGINGAT ====================

    fun isExpiryReminderEnabled(email: String?): Boolean {
        val key = if (email.isNullOrBlank()) "expiry_reminder_default" else "$KEY_EXPIRY_REMINDER_PREFIX${User.normalizeEmail(email)}"
        return prefs.getBoolean(key, true)
    }

    fun setExpiryReminderEnabled(email: String?, enabled: Boolean) {
        val key = if (email.isNullOrBlank()) "expiry_reminder_default" else "$KEY_EXPIRY_REMINDER_PREFIX${User.normalizeEmail(email)}"
        prefs.edit().putBoolean(key, enabled).apply()
    }

    // ==================== PENGELOLAAN FOTO PROFIL ====================

    fun getProfileImagePath(email: String?): String? {
        if (email.isNullOrBlank()) return null
        return prefs.getString("$KEY_PROFILE_IMAGE_PREFIX${User.normalizeEmail(email)}", null)
    }

    fun setProfileImagePath(email: String?, path: String?) {
        if (email.isNullOrBlank()) return
        val key = "$KEY_PROFILE_IMAGE_PREFIX${User.normalizeEmail(email)}"
        if (path == null) {
            prefs.edit().remove(key).apply()
        } else {
            prefs.edit().putString(key, path).apply()
        }
    }

    // ==================== PENGELOLAAN RESEP TERSIMPAN PER USER ====================

    fun getSavedRecipes(email: String?): List<Recipe> {
        if (email.isNullOrBlank()) return emptyList()
        val normalized = User.normalizeEmail(email)
        val rawJson = prefs.getString("$KEY_SAVED_RECIPES_PREFIX$normalized", null) ?: return emptyList()
        return try {
            json.decodeFromString<List<Recipe>>(rawJson)
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengurai daftar resep tersimpan dari SharedPreferences", e)
            emptyList()
        }
    }

    fun saveRecipe(email: String?, recipe: Recipe) {
        if (email.isNullOrBlank()) return
        val normalized = User.normalizeEmail(email)
        val current = getSavedRecipes(email).filterNot { it.id == recipe.id }.toMutableList()
        // Tambahkan ke indeks 0 agar resep paling baru disimpan berada di paling atas
        current.add(0, recipe.copy(savedAtEpochMillis = System.currentTimeMillis()))
        val rawJson = json.encodeToString(current)
        prefs.edit().putString("$KEY_SAVED_RECIPES_PREFIX$normalized", rawJson).apply()
    }

    fun removeSavedRecipe(email: String?, recipeId: String) {
        if (email.isNullOrBlank()) return
        val normalized = User.normalizeEmail(email)
        val current = getSavedRecipes(email).filterNot { it.id == recipeId }
        val rawJson = json.encodeToString(current)
        prefs.edit().putString("$KEY_SAVED_RECIPES_PREFIX$normalized", rawJson).apply()
    }

    fun isRecipeSaved(email: String?, recipeId: String): Boolean {
        if (email.isNullOrBlank()) return false
        return getSavedRecipes(email).any { it.id == recipeId }
    }
}
