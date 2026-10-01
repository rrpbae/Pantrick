// [Materi: Kotlin Serialization & Data Class] Model representasi pengguna Pantrick
package com.example.pantrick.data.model

import kotlinx.serialization.Serializable
import java.security.MessageDigest

// [Materi: @Serializable User Entity] Entitas pengguna dengan password tersandi SHA-256
@Serializable
data class User(
    val fullName: String,
    val email: String,
    val passwordHash: String
) {
    companion object {
        // [Materi: Keamanan & Hashing SHA-256] Mengamankan password tanpa menyimpan teks polos
        fun hashPassword(password: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        // [Materi: Normalisasi Input Email] Menghapus spasi luar dan mengubah huruf kecil
        fun normalizeEmail(email: String): String {
            return email.trim().lowercase()
        }
    }
}
