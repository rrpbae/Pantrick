// [Materi: JWT Helper] Utility untuk generate JWT token dengan HMAC256 signature
package com.example.pantrick.util

import android.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Helper untuk generate JWT token yang valid.
 * Token ini di-sign dengan HMAC256 menggunakan secret key yang sama dengan backend.
 * 
 * Format JWT: header.payload.signature
 * - header: {"alg":"HS256","typ":"JWT"}
 * - payload: {"sub":"userId","iss":"pantrick-backend","aud":"pantrick-android-app","email":"...","exp":...}
 * - signature: HMAC-SHA256(header.payload, secret)
 */
object JwtHelper {
    
    // Secret key MUST match backend TokenProvider.kt
    private const val SECRET = "pantrick_jwt_secret_key_dev_mode_2026"
    private const val ISSUER = "pantrick-backend"
    private const val AUDIENCE = "pantrick-android-app"
    
    /**
     * Generate valid JWT token dengan HMAC256 signature.
     * Token ini dapat diverifikasi oleh backend authentication middleware.
     */
    fun generateDummyToken(userId: Int, email: String): String {
        val now = System.currentTimeMillis() / 1000
        val exp = now + (7 * 24 * 60 * 60) // 7 days
        
        // Build header JSON
        val header = """{"alg":"HS256","typ":"JWT"}"""
        
        // Build payload JSON with all required claims
        val payload = buildString {
            append("{")
            append(""""sub":"$userId",""")
            append(""""iss":"$ISSUER",""")
            append(""""aud":"$AUDIENCE",""")
            append(""""email":"$email",""")
            append(""""exp":$exp""")
            append("}")
        }
        
        // Encode header and payload
        val headerEncoded = base64UrlEncode(header)
        val payloadEncoded = base64UrlEncode(payload)
        val toSign = "$headerEncoded.$payloadEncoded"
        
        // Sign with HMAC-SHA256
        val signature = signHmacSha256(toSign, SECRET)
        
        return "$toSign.$signature"
    }
    
    /**
     * Sign data dengan HMAC-SHA256 algorithm.
     */
    private fun signHmacSha256(data: String, secret: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        val secretKey = SecretKeySpec(secret.toByteArray(Charsets.UTF_8), "HmacSHA256")
        mac.init(secretKey)
        val signatureBytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(signatureBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
    
    /**
     * Encode string to Base64 URL-safe format.
     */
    private fun base64UrlEncode(input: String): String {
        val bytes = input.toByteArray(Charsets.UTF_8)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
    
    /**
     * Generate userId dari email untuk konsistensi.
     * Dalam production, userId harus dari backend database.
     */
    fun generateUserId(email: String): Int {
        return email.hashCode().and(0x7FFFFFFF) % 100000
    }
}
