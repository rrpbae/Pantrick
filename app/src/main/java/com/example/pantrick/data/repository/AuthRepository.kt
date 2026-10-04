// [Materi: Repository Pattern] Abstraksi logika autentikasi pengguna dan pengelolaan sesi
package com.example.pantrick.data.repository

import android.util.Log
import com.example.pantrick.data.local.PantrickPreferences
import com.example.pantrick.data.model.User
import com.example.pantrick.util.JwtHelper

private const val TAG = "AuthRepository"

// [Materi: Sealed Interface] Pemodelan hasil operasi autentikasi (Success vs Error)
sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data class Error(val message: String) : AuthResult
}

// [Materi: Repository Pattern] Penghubung antara sumber data lokal dan ViewModel
class AuthRepository(private val preferences: PantrickPreferences) {

    // [Materi: Normalisasi & Validasi Autentikasi] Fungsi pendaftaran akun baru
    fun register(
        fullName: String,
        email: String,
        password: String,
        rememberMe: Boolean = false,
        jwtToken: String? = null
    ): AuthResult {
        val normalizedEmail = User.normalizeEmail(email)
        val users = preferences.getUsers().toMutableList()

        // Periksa apakah email sudah pernah terdaftar
        if (users.any { it.email == normalizedEmail }) {
            Log.w(TAG, "Register failed: Email already registered ($normalizedEmail)")
            return AuthResult.Error("Email sudah terdaftar. Silakan masuk.")
        }

        // Simpan user baru dengan kata sandi ter-hash
        val newUser = User(
            fullName = fullName.trim(),
            email = normalizedEmail,
            passwordHash = User.hashPassword(password)
        )
        users.add(newUser)
        preferences.saveUsers(users)

        // Generate JWT token untuk development (jika tidak disediakan dari backend)
        val token = jwtToken ?: JwtHelper.generateDummyToken(
            userId = JwtHelper.generateUserId(normalizedEmail),
            email = normalizedEmail
        )
        
        // Jadikan sesi aktif dengan JWT token
        preferences.setActiveSession(normalizedEmail, rememberMe, token)
        Log.i(TAG, "Register success for user: $normalizedEmail with JWT token")
        return AuthResult.Success(newUser)
    }

    // [Materi: Autentikasi Pengguna & Keamanan] Fungsi masuk akun pengguna
    fun login(
        email: String,
        password: String,
        rememberMe: Boolean = false,
        jwtToken: String? = null
    ): AuthResult {
        val normalizedEmail = User.normalizeEmail(email)
        val users = preferences.getUsers()
        val existingUser = users.find { it.email == normalizedEmail }

        if (existingUser == null) {
            Log.w(TAG, "Login failed: Account not registered ($normalizedEmail)")
            return AuthResult.Error("Akun belum terdaftar. Silakan daftar terlebih dahulu.")
        }

        val inputHash = User.hashPassword(password)
        if (existingUser.passwordHash != inputHash) {
            Log.w(TAG, "Login failed: Incorrect password ($normalizedEmail)")
            return AuthResult.Error("Kata sandi salah.")
        }

        // Generate JWT token untuk development (jika tidak disediakan dari backend)
        val token = jwtToken ?: JwtHelper.generateDummyToken(
            userId = JwtHelper.generateUserId(normalizedEmail),
            email = normalizedEmail
        )
        
        preferences.setActiveSession(normalizedEmail, rememberMe, token)
        Log.i(TAG, "Login success for user: $normalizedEmail with JWT token")
        return AuthResult.Success(existingUser)
    }

    // [Materi: Logout / Sesi] Menghapus sesi aktif
    fun logout() {
        val currentEmail = preferences.getActiveSessionEmail()
        Log.i(TAG, "Logging out user: $currentEmail")
        preferences.clearSession()
    }

    // [Materi: State Retrieval] Mengambil data user yang sedang aktif login
    fun getCurrentUser(): User? {
        val activeEmail = preferences.getActiveSessionEmail() ?: return null
        return preferences.getUsers().find { it.email == activeEmail }
    }

    // [Materi: Update Profil] Memperbarui nama pengguna
    fun updateUserName(email: String, newFullName: String): User? {
        return preferences.updateUserName(email, newFullName)
    }

    // [Materi: Preferensi Pengingat] Pengaturan toggle pengingat kedaluwarsa
    fun isExpiryReminderEnabled(email: String?): Boolean {
        return preferences.isExpiryReminderEnabled(email)
    }

    fun setExpiryReminderEnabled(email: String?, enabled: Boolean) {
        preferences.setExpiryReminderEnabled(email, enabled)
    }

    // [Materi: Update Kata Sandi] Memperbarui hash kata sandi pengguna
    fun updateUserPassword(email: String, newPasswordHash: String): User? {
        return preferences.updateUserPassword(email, newPasswordHash)
    }

    // [Materi: Foto Profil] Pengelolaan path foto profil pengguna
    fun getProfileImagePath(email: String?): String? {
        return preferences.getProfileImagePath(email)
    }

    fun setProfileImagePath(email: String?, path: String?) {
        preferences.setProfileImagePath(email, path)
    }

    // [Materi: JWT Token] Mendapatkan token untuk API calls
    fun getJwtToken(): String? {
        return preferences.getJwtToken()
    }
}
