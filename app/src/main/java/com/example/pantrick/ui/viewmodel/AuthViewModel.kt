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
            }
            onResult(result)
        }
    }

    // [Materi: Logout Handler] Menghapus sesi aktif dan membersihkan state pengguna
    fun logout() {
        authRepository.logout()
        _currentUser.value = null
    }

    // [Materi: Initial Session Check] Memeriksa apakah ada sesi aktif saat app pertama kali dibuka
    fun hasActiveSession(): Boolean {
        return _currentUser.value != null
    }
}
