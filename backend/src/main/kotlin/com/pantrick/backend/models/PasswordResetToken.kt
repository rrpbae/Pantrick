package com.pantrick.backend.models

import java.time.Instant

/**
 * Model yang merepresentasikan token reset password.
 *
 * @property token Token acak yang aman untuk reset password.
 * @property userId ID user yang meminta reset password.
 * @property expiresAt Waktu kedaluwarsa token (15 menit dari saat dibuat).
 * @property used Flag apakah token sudah pernah digunakan.
 */
data class PasswordResetToken(
    val token: String,
    val userId: Int,
    val expiresAt: Instant,
    val used: Boolean = false
)
