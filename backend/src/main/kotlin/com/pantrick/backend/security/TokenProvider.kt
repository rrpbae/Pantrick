package com.pantrick.backend.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

object TokenProvider {
    private val secret = System.getenv("JWT_SECRET") ?: "pantrick_jwt_secret_key_dev_mode_2026"
    private const val issuer = "pantrick-backend"
    private const val audience = "pantrick-android-app"
    private const val validityInMs = 3600_000L * 24 * 7 // 7 days

    fun generateToken(userId: Int, email: String): String {
        return JWT.create()
            .withSubject(userId.toString())
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .sign(Algorithm.HMAC256(secret))
    }
}
