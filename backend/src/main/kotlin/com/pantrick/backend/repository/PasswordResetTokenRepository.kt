package com.pantrick.backend.repository

import com.pantrick.backend.models.PasswordResetToken
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Repository untuk menyimpan dan mengelola token reset password secara in-memory.
 */
interface PasswordResetTokenRepository {
    /**
     * Menyimpan token reset password baru.
     */
    fun save(token: PasswordResetToken)

    /**
     * Mencari token berdasarkan string tokennya.
     */
    fun findByToken(token: String): PasswordResetToken?

    /**
     * Menandai token sebagai sudah digunakan (invalidate).
     */
    fun markAsUsed(token: String)

    /**
     * Menghapus token-token yang sudah kedaluwarsa untuk membersihkan memory.
     */
    fun removeExpiredTokens()
}

/**
 * Implementasi in-memory dari PasswordResetTokenRepository.
 * Menggunakan ConcurrentHashMap agar thread-safe.
 */
class InMemoryPasswordResetTokenRepository : PasswordResetTokenRepository {
    // Key: token string, Value: PasswordResetToken
    private val tokens = ConcurrentHashMap<String, PasswordResetToken>()

    override fun save(token: PasswordResetToken) {
        tokens[token.token] = token
    }

    override fun findByToken(token: String): PasswordResetToken? {
        return tokens[token]
    }

    override fun markAsUsed(token: String) {
        val existing = tokens[token] ?: return
        tokens[token] = existing.copy(used = true)
    }

    override fun removeExpiredTokens() {
        val now = Instant.now()
        tokens.entries.removeIf { it.value.expiresAt.isBefore(now) }
    }
}
