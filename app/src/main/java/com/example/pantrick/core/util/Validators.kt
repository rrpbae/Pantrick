// [Materi: Validasi Form & Regular Expression] Validasi input formulir autentikasi berbahasa Indonesia
package com.example.pantrick.core.util

import com.example.pantrick.util.PantrickConstants
import java.util.regex.Pattern

// [Materi: Sealed Interface Validation] Hasil validasi masukan formulir
sealed interface ValidationResult {
    data object Success : ValidationResult
    data class Error(val message: String) : ValidationResult
}

// [Materi: Object Singleton Validators] Utilitas validasi email dan kata sandi
object Validators {
    private val EMAIL_PATTERN: Pattern = Pattern.compile(
        "^[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+$"
    )

    fun validateEmail(email: String): ValidationResult {
        val trimmed = email.trim()
        return when {
            trimmed.isEmpty() -> ValidationResult.Error("Alamat email tidak boleh kosong")
            !EMAIL_PATTERN.matcher(trimmed).matches() -> ValidationResult.Error("Format email tidak valid")
            else -> ValidationResult.Success
        }
    }

    fun validatePassword(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Error("Kata sandi tidak boleh kosong")
            password.length < PantrickConstants.MIN_PASSWORD_LENGTH ->
                ValidationResult.Error("Kata sandi minimal ${PantrickConstants.MIN_PASSWORD_LENGTH} karakter")
            else -> ValidationResult.Success
        }
    }
}
