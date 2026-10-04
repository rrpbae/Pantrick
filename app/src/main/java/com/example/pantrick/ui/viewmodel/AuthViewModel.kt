// [Materi: ViewModel & StateFlow] Pengelolaan state autentikasi dengan arsitektur AndroidViewModel
package com.example.pantrick.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pantrick.data.local.PantrickPreferences
import com.example.pantrick.data.model.User
import com.example.pantrick.data.repository.AuthRepository
import com.example.pantrick.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// [Materi: AndroidViewModel Lifecycle] ViewModel berbasis Application Context untuk mengelola autentikasi
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = PantrickPreferences(application)
    private val authRepository = AuthRepository(preferences)

    // [Materi: StateFlow] Aliran data reaktif pengguna yang sedang login
    private val _currentUser = MutableStateFlow<User?>(authRepository.getCurrentUser())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _profileImagePath = MutableStateFlow<String?>(authRepository.getProfileImagePath(authRepository.getCurrentUser()?.email))
    val profileImagePath: StateFlow<String?> = _profileImagePath.asStateFlow()

    // [Materi: JWT Token] Expose JWT token untuk API calls yang memerlukan authentication
    private val _jwtToken = MutableStateFlow<String?>(authRepository.getJwtToken())
    val jwtToken: StateFlow<String?> = _jwtToken.asStateFlow()

    // Fungsi helper untuk mendapatkan token (fallback jika null = gunakan dummy untuk development)
    fun getJwtTokenOrEmpty(): String {
        return _jwtToken.value ?: ""
    }

    // [Materi: Login Handler] Memproses login dan memperbarui state sesi
    fun login(
        email: String,
        password: String,
        rememberMe: Boolean,
        onResult: (AuthResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.login(email, password, rememberMe)
            if (result is AuthResult.Success) {
                _currentUser.value = result.user
                _profileImagePath.value = authRepository.getProfileImagePath(result.user.email)
                _jwtToken.value = authRepository.getJwtToken()
            }
            onResult(result)
        }
    }

    // [Materi: Register Handler] Mendaftarkan akun baru dan langsung menjadikan sesi aktif
    fun register(
        fullName: String,
        email: String,
        password: String,
        rememberMe: Boolean,
        onResult: (AuthResult) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.register(fullName, email, password, rememberMe)
            if (result is AuthResult.Success) {
                _currentUser.value = result.user
                _profileImagePath.value = authRepository.getProfileImagePath(result.user.email)
                _jwtToken.value = authRepository.getJwtToken()
            }
            onResult(result)
        }
    }

    // [Materi: Logout Handler] Menghapus sesi aktif dan membersihkan state pengguna
    fun logout() {
        authRepository.logout()
        _currentUser.value = null
        _profileImagePath.value = null
        _jwtToken.value = null
    }

    // [Materi: Initial Session Check] Memeriksa apakah ada sesi aktif saat app pertama kali dibuka
    fun hasActiveSession(): Boolean {
        return _currentUser.value != null
    }

    // [Materi: Edit Profil] Memperbarui nama pengguna aktif
    fun updateProfileName(newFullName: String): Boolean {
        val user = _currentUser.value ?: return false
        val updated = authRepository.updateUserName(user.email, newFullName)
        if (updated != null) {
            _currentUser.value = updated
            return true
        }
        return false
    }

    // [Materi: Pengaturan Pengingat] Toggle pengingat kedaluwarsa
    fun isExpiryReminderEnabled(): Boolean {
        return authRepository.isExpiryReminderEnabled(_currentUser.value?.email)
    }

    fun setExpiryReminderEnabled(enabled: Boolean) {
        authRepository.setExpiryReminderEnabled(_currentUser.value?.email, enabled)
    }

    // [Materi: Foto Profil] Mengambil dan memperbarui foto profil pengguna
    fun getProfileImagePath(): String? {
        return authRepository.getProfileImagePath(_currentUser.value?.email)
    }

    fun updateProfilePhoto(path: String?) {
        val user = _currentUser.value ?: return
        authRepository.setProfileImagePath(user.email, path)
        _profileImagePath.value = path
    }

    // [Materi: Ubah Password] Memverifikasi password lama dan menyimpan password baru
    fun changePassword(oldPassword: String, newPassword: String): String? {
        val user = _currentUser.value ?: return "Sesi pengguna tidak valid."
        val oldHash = User.hashPassword(oldPassword)
        if (oldHash != user.passwordHash) {
            return "Password lama salah."
        }
        val newHash = User.hashPassword(newPassword)
        val updated = authRepository.updateUserPassword(user.email, newHash)
        if (updated != null) {
            _currentUser.value = updated
            return null
        }
        return "Gagal memperbarui password."
    }
}
