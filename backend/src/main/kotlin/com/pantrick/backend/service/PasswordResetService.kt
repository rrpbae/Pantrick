package com.pantrick.backend.service

import com.pantrick.backend.models.PasswordResetToken
import com.pantrick.backend.repository.PasswordResetTokenRepository
import com.pantrick.backend.repository.UserRepository
import com.pantrick.backend.security.PasswordHasher
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64

/**
 * Service yang menangani logika bisnis reset password.
 *
 * Menggunakan SecureRandom untuk menghasilkan token yang aman secara kriptografis.
 * Token berlaku selama 15 menit dan hanya dapat digunakan satu kali.
 */
class PasswordResetService(
    private val userRepository: UserRepository,
    private val tokenRepository: PasswordResetTokenRepository
) {
    companion object {
        /** Masa berlaku token reset password dalam milidetik (15 menit). */
        const val TOKEN_VALIDITY_MS = 15L * 60 * 1000

        /** Panjang byte token acak sebelum di-encode. */
        private const val TOKEN_BYTE_LENGTH = 32
    }

    private val secureRandom = SecureRandom()

    /**
     * Memproses permintaan reset password untuk email tertentu.
     *
     * Jika email terdaftar, akan membuat token reset yang aman dan menyimpannya.
     * Jika email tidak terdaftar, fungsi tetap berjalan normal tanpa mengungkapkan
     * bahwa email tidak ada (mencegah email enumeration attack).
     *
     * @param email Email pengguna yang meminta reset password.
     * @return Token yang dibuat jika email terdaftar, null jika tidak.
     */
    fun requestPasswordReset(email: String): String? {
        // Bersihkan token kedaluwarsa sebelum memproses
        tokenRepository.removeExpiredTokens()

        val user = userRepository.findByEmail(email) ?: return null

        val tokenBytes = ByteArray(TOKEN_BYTE_LENGTH)
        secureRandom.nextBytes(tokenBytes)
        val tokenString = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes)

        val resetToken = PasswordResetToken(
            token = tokenString,
            userId = user.id,
            expiresAt = Instant.now().plusMillis(TOKEN_VALIDITY_MS),
            used = false
        )

        tokenRepository.save(resetToken)
        return tokenString
    }

    /**
     * Enum hasil validasi token reset password.
     */
    enum class TokenValidationResult {
        VALID,
        INVALID_OR_NOT_FOUND,
        EXPIRED,
        ALREADY_USED
    }

    /**
     * Memvalidasi token reset password.
     *
     * @param token String token yang akan divalidasi.
     * @return Hasil validasi berupa [TokenValidationResult].
     */
    fun validateToken(token: String): TokenValidationResult {
        val resetToken = tokenRepository.findByToken(token)
            ?: return TokenValidationResult.INVALID_OR_NOT_FOUND

        if (resetToken.used) {
            return TokenValidationResult.ALREADY_USED
        }

        if (Instant.now().isAfter(resetToken.expiresAt)) {
            return TokenValidationResult.EXPIRED
        }

        return TokenValidationResult.VALID
    }

    /**
     * Hasil operasi reset password.
     */
    sealed class ResetPasswordResult {
        object Success : ResetPasswordResult()
        object InvalidToken : ResetPasswordResult()
        object ExpiredToken : ResetPasswordResult()
        object AlreadyUsedToken : ResetPasswordResult()
        object PasswordMismatch : ResetPasswordResult()
        object UserNotFound : ResetPasswordResult()
    }

    /**
     * Melakukan reset password menggunakan token yang valid.
     *
     * @param token Token reset password.
     * @param newPassword Password baru.
     * @param passwordConfirmation Konfirmasi password baru.
     * @return Hasil operasi berupa [ResetPasswordResult].
     */
    fun resetPassword(
        token: String,
        newPassword: String,
        passwordConfirmation: String
    ): ResetPasswordResult {
        if (newPassword != passwordConfirmation) {
            return ResetPasswordResult.PasswordMismatch
        }

        val validationResult = validateToken(token)
        return when (validationResult) {
            TokenValidationResult.INVALID_OR_NOT_FOUND -> ResetPasswordResult.InvalidToken
            TokenValidationResult.EXPIRED -> ResetPasswordResult.ExpiredToken
            TokenValidationResult.ALREADY_USED -> ResetPasswordResult.AlreadyUsedToken
            TokenValidationResult.VALID -> {
                val resetToken = tokenRepository.findByToken(token)!!
                val user = userRepository.findById(resetToken.userId)
                    ?: return ResetPasswordResult.UserNotFound

                val newHash = PasswordHasher.hashPassword(newPassword)
                userRepository.updatePasswordHash(user.id, newHash)
                tokenRepository.markAsUsed(token)

                ResetPasswordResult.Success
            }
        }
    }
}
