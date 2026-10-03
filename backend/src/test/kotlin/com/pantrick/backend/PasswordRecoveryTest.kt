package com.pantrick.backend

import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.models.ForgotPasswordResponse
import com.pantrick.backend.models.LoginRequest
import com.pantrick.backend.models.LoginResponse
import com.pantrick.backend.models.ResetPasswordRequest
import com.pantrick.backend.models.ResetPasswordResponse
import com.pantrick.backend.models.VerifyResetTokenResponse
import com.pantrick.backend.repository.InMemoryPasswordResetTokenRepository
import com.pantrick.backend.repository.InMemoryUserRepository
import com.pantrick.backend.service.PasswordResetService
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test suite untuk fitur password recovery (Lupa Kata Sandi).
 *
 * Mencakup 15 skenario test sesuai spesifikasi A6.
 */
class PasswordRecoveryTest {

    // Helper: membuat test application dengan UserRepository dan PasswordResetService sendiri
    // sehingga bisa langsung inspect state-nya
    private fun buildTestComponents(): Triple<InMemoryUserRepository, InMemoryPasswordResetTokenRepository, PasswordResetService> {
        val userRepo = InMemoryUserRepository()
        val tokenRepo = InMemoryPasswordResetTokenRepository()
        val service = PasswordResetService(userRepo, tokenRepo)
        return Triple(userRepo, tokenRepo, service)
    }

    // =========================================================
    // TEST 1: forgot password email valid
    // =========================================================
    @Test
    fun testForgotPasswordValidEmail() = testApplication {
        application { module(InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val response = client.post("/api/auth/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"user@example.com"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<ForgotPasswordResponse>()
        assertEquals("Jika email terdaftar, instruksi reset kata sandi telah dibuat.", body.message)
    }

    // =========================================================
    // TEST 2: forgot password email tidak terdaftar
    // =========================================================
    @Test
    fun testForgotPasswordEmailNotRegistered() = testApplication {
        application { module(InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val response = client.post("/api/auth/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"notregistered@example.com"}""")
        }

        // Harus tetap 200 OK, bukan 404
        assertEquals(HttpStatusCode.OK, response.status)
    }

    // =========================================================
    // TEST 3: response tetap generik (tidak mengungkapkan apakah email terdaftar)
    // =========================================================
    @Test
    fun testForgotPasswordResponseAlwaysGeneric() = testApplication {
        application { module(InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Email terdaftar
        val responseRegistered = client.post("/api/auth/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"user@example.com"}""")
        }

        // Email tidak terdaftar
        val responseUnregistered = client.post("/api/auth/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody("""{"email":"unknown@example.com"}""")
        }

        val bodyRegistered = responseRegistered.body<ForgotPasswordResponse>()
        val bodyUnregistered = responseUnregistered.body<ForgotPasswordResponse>()

        // Kedua response harus identik
        assertEquals(bodyRegistered.message, bodyUnregistered.message)
        assertEquals(HttpStatusCode.OK, responseRegistered.status)
        assertEquals(HttpStatusCode.OK, responseUnregistered.status)
    }

    // =========================================================
    // TEST 4: token berhasil dibuat untuk email valid
    // =========================================================
    @Test
    fun testForgotPasswordTokenCreatedForValidEmail() {
        val (_, _, service) = buildTestComponents()

        val token = service.requestPasswordReset("user@example.com")
        assertNotNull("Token seharusnya dibuat untuk email valid", token)
        assertTrue("Token seharusnya tidak kosong", token!!.isNotBlank())
    }

    // =========================================================
    // TEST 5: token valid dapat diverifikasi
    // =========================================================
    @Test
    fun testVerifyTokenValid() = testApplication {
        val (userRepo, tokenRepo, service) = buildTestComponents()
        application { module(userRepo, service) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Buat token langsung via service
        val token = service.requestPasswordReset("user@example.com")
        assertNotNull(token)

        val response = client.get("/api/auth/reset-password/verify?token=$token")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<VerifyResetTokenResponse>()
        assertTrue("Token harus valid", body.valid)
        assertNull("Pesan tidak boleh ada jika token valid", body.message)
    }

    // =========================================================
    // TEST 6: token invalid (random token yang tidak ada)
    // =========================================================
    @Test
    fun testVerifyTokenInvalid() = testApplication {
        application { module(InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val response = client.get("/api/auth/reset-password/verify?token=random-invalid-token-xyz")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<VerifyResetTokenResponse>()
        assertFalse("Token tidak valid harus mengembalikan valid=false", body.valid)
        assertEquals(
            "Token reset kata sandi tidak valid atau sudah kedaluwarsa.",
            body.message
        )
    }

    // =========================================================
    // TEST 7: token expired
    // =========================================================
    @Test
    fun testVerifyTokenExpired() {
        val (userRepo, tokenRepo, service) = buildTestComponents()

        // Buat token lalu manipulasi agar expired dengan membuat token baru secara manual
        val tokenString = "expired-test-token-12345"
        val expiredToken = com.pantrick.backend.models.PasswordResetToken(
            token = tokenString,
            userId = 1,
            expiresAt = java.time.Instant.now().minusSeconds(60), // Sudah expired 1 menit lalu
            used = false
        )
        tokenRepo.save(expiredToken)

        val result = service.validateToken(tokenString)
        assertEquals(
            "Token expired harus mengembalikan EXPIRED",
            PasswordResetService.TokenValidationResult.EXPIRED,
            result
        )
    }

    // =========================================================
    // TEST 8: token sudah digunakan
    // =========================================================
    @Test
    fun testVerifyTokenAlreadyUsed() {
        val (userRepo, tokenRepo, service) = buildTestComponents()

        val tokenString = "used-test-token-12345"
        val usedToken = com.pantrick.backend.models.PasswordResetToken(
            token = tokenString,
            userId = 1,
            expiresAt = java.time.Instant.now().plusSeconds(900), // Masih berlaku
            used = true
        )
        tokenRepo.save(usedToken)

        val result = service.validateToken(tokenString)
        assertEquals(
            "Token yang sudah digunakan harus mengembalikan ALREADY_USED",
            PasswordResetService.TokenValidationResult.ALREADY_USED,
            result
        )
    }

    // =========================================================
    // TEST 9: password confirmation mismatch
    // =========================================================
    @Test
    fun testResetPasswordConfirmationMismatch() = testApplication {
        val (userRepo, tokenRepo, service) = buildTestComponents()
        application { module(userRepo, service) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = service.requestPasswordReset("user@example.com")
        assertNotNull(token)

        val response = client.post("/api/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(
                ResetPasswordRequest(
                    token = token,
                    password = "newpassword123",
                    passwordConfirmation = "differentpassword456"
                )
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertFalse(body.success)
        assertNotNull(body.errors?.get("passwordConfirmation"))
    }

    // =========================================================
    // TEST 10: reset password berhasil
    // =========================================================
    @Test
    fun testResetPasswordSuccess() = testApplication {
        val (userRepo, tokenRepo, service) = buildTestComponents()
        application { module(userRepo, service) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = service.requestPasswordReset("user@example.com")
        assertNotNull(token)

        val response = client.post("/api/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(
                ResetPasswordRequest(
                    token = token,
                    password = "newSecurePassword123",
                    passwordConfirmation = "newSecurePassword123"
                )
            )
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<ResetPasswordResponse>()
        assertEquals("Kata sandi berhasil diubah.", body.message)
    }

    // =========================================================
    // TEST 11: login menggunakan password baru berhasil
    // =========================================================
    @Test
    fun testLoginWithNewPasswordAfterReset() = testApplication {
        val (userRepo, tokenRepo, service) = buildTestComponents()
        application { module(userRepo, service) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Reset password
        val token = service.requestPasswordReset("user@example.com")
        client.post("/api/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(
                ResetPasswordRequest(
                    token = token,
                    password = "brandNewPassword999",
                    passwordConfirmation = "brandNewPassword999"
                )
            )
        }

        // Login dengan password baru harus berhasil
        val loginResponse = client.post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = "user@example.com", password = "brandNewPassword999"))
        }

        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val loginBody = loginResponse.body<LoginResponse>()
        assertTrue("Login dengan password baru harus berhasil", loginBody.success)
        assertNotNull(loginBody.data?.token)
    }

    // =========================================================
    // TEST 12: login menggunakan password lama gagal
    // =========================================================
    @Test
    fun testLoginWithOldPasswordFailsAfterReset() = testApplication {
        val (userRepo, tokenRepo, service) = buildTestComponents()
        application { module(userRepo, service) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Reset password
        val token = service.requestPasswordReset("user@example.com")
        client.post("/api/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(
                ResetPasswordRequest(
                    token = token,
                    password = "newPasswordAfterReset",
                    passwordConfirmation = "newPasswordAfterReset"
                )
            )
        }

        // Login dengan password lama harus gagal
        val loginResponse = client.post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = "user@example.com", password = "password123"))
        }

        assertEquals(HttpStatusCode.Unauthorized, loginResponse.status)
        val loginBody = loginResponse.body<ErrorResponse>()
        assertFalse("Login dengan password lama harus gagal", loginBody.success)
    }

    // =========================================================
    // TEST 13: password tidak muncul dalam response
    // =========================================================
    @Test
    fun testPasswordNotInResponse() = testApplication {
        val (userRepo, tokenRepo, service) = buildTestComponents()
        application { module(userRepo, service) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = service.requestPasswordReset("user@example.com")
        val response = client.post("/api/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(
                ResetPasswordRequest(
                    token = token,
                    password = "checkPassword123",
                    passwordConfirmation = "checkPassword123"
                )
            )
        }

        val rawJson = response.body<String>()
        assertFalse(
            "Response tidak boleh mengandung kata 'password' sebagai nilai",
            rawJson.contains("\"password\":\"")
        )
        assertFalse(
            "Response tidak boleh mengandung kata 'checkPassword123'",
            rawJson.contains("checkPassword123")
        )
    }

    // =========================================================
    // TEST 14: passwordHash tidak muncul dalam response
    // =========================================================
    @Test
    fun testPasswordHashNotInResponse() = testApplication {
        val (userRepo, tokenRepo, service) = buildTestComponents()
        application { module(userRepo, service) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = service.requestPasswordReset("user@example.com")
        val response = client.post("/api/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(
                ResetPasswordRequest(
                    token = token,
                    password = "checkHash123",
                    passwordConfirmation = "checkHash123"
                )
            )
        }

        val rawJson = response.body<String>()
        assertFalse(
            "Response tidak boleh mengandung field 'passwordHash'",
            rawJson.contains("passwordHash")
        )
        // Pastikan tidak ada bcrypt hash (dimulai dengan $2a$ atau $2b$)
        assertFalse(
            "Response tidak boleh mengandung bcrypt hash",
            rawJson.contains("\$2a\$") || rawJson.contains("\$2b\$")
        )
    }

    // =========================================================
    // TEST 15: token tidak bisa digunakan dua kali
    // =========================================================
    @Test
    fun testTokenCannotBeUsedTwice() = testApplication {
        val (userRepo, tokenRepo, service) = buildTestComponents()
        application { module(userRepo, service) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = service.requestPasswordReset("user@example.com")
        assertNotNull(token)

        // Penggunaan pertama - harus berhasil
        val firstReset = client.post("/api/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(
                ResetPasswordRequest(
                    token = token,
                    password = "firstNewPassword123",
                    passwordConfirmation = "firstNewPassword123"
                )
            )
        }
        assertEquals(HttpStatusCode.OK, firstReset.status)

        // Penggunaan kedua dengan token yang sama - harus gagal
        val secondReset = client.post("/api/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(
                ResetPasswordRequest(
                    token = token,
                    password = "secondNewPassword456",
                    passwordConfirmation = "secondNewPassword456"
                )
            )
        }
        assertEquals(
            "Token yang sudah digunakan tidak boleh dipakai lagi",
            HttpStatusCode.BadRequest,
            secondReset.status
        )
        val body = secondReset.body<ErrorResponse>()
        assertFalse(body.success)
    }
}
