package com.pantrick.backend

import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.models.LoginRequest
import com.pantrick.backend.models.LoginResponse
import com.pantrick.backend.repository.InMemoryUserRepository
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

import com.pantrick.backend.models.RegisterRequest
import com.pantrick.backend.models.RegisterResponse

class AuthTest {

    @Test
    fun testRegisterSuccess() = testApplication {
        val repo = InMemoryUserRepository()
        application {
            module(repo)
        }
        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.post("/api/register") {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterRequest(
                    name = "New User",
                    email = "newuser@example.com",
                    password = "password123",
                    passwordConfirmation = "password123"
                )
            )
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<RegisterResponse>()
        assertTrue(body.success)
        assertEquals("Registrasi berhasil", body.message)
        assertNotNull(body.data)
        assertEquals("New User", body.data?.user?.name)
        assertEquals("newuser@example.com", body.data?.user?.email)

        // Verify user in repo is hashed
        val savedUser = repo.findByEmail("newuser@example.com")
        assertNotNull(savedUser)
        assertTrue(savedUser?.passwordHash != "password123")
    }

    @Test
    fun testRegisterDuplicateEmail() = testApplication {
        application {
            module(InMemoryUserRepository())
        }
        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.post("/api/register") {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterRequest(
                    name = "Duplicate User",
                    email = "user@example.com",
                    password = "password123",
                    passwordConfirmation = "password123"
                )
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals(false, body.success)
        assertEquals("Email sudah terdaftar", body.errors?.get("email"))
    }

    @Test
    fun testRegisterEmptyName() = testApplication {
        application {
            module(InMemoryUserRepository())
        }
        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.post("/api/register") {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterRequest(
                    name = "",
                    email = "freshuser@example.com",
                    password = "password123",
                    passwordConfirmation = "password123"
                )
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals(false, body.success)
        assertEquals("Nama wajib diisi", body.errors?.get("name"))
    }

    @Test
    fun testRegisterEmptyEmail() = testApplication {
        application {
            module(InMemoryUserRepository())
        }
        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.post("/api/register") {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterRequest(
                    name = "Valid Name",
                    email = "",
                    password = "password123",
                    passwordConfirmation = "password123"
                )
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals(false, body.success)
        assertEquals("Email wajib diisi", body.errors?.get("email"))
    }

    @Test
    fun testRegisterInvalidEmailFormat() = testApplication {
        application {
            module(InMemoryUserRepository())
        }
        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.post("/api/register") {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterRequest(
                    name = "Valid Name",
                    email = "bademailformat",
                    password = "password123",
                    passwordConfirmation = "password123"
                )
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals(false, body.success)
        assertEquals("Format email tidak valid", body.errors?.get("email"))
    }

    @Test
    fun testRegisterEmptyPassword() = testApplication {
        application {
            module(InMemoryUserRepository())
        }
        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.post("/api/register") {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterRequest(
                    name = "Valid Name",
                    email = "freshuser@example.com",
                    password = "",
                    passwordConfirmation = ""
                )
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals(false, body.success)
        assertEquals("Password wajib diisi", body.errors?.get("password"))
    }

    @Test
    fun testRegisterPasswordMismatch() = testApplication {
        application {
            module(InMemoryUserRepository())
        }
        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.post("/api/register") {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterRequest(
                    name = "Valid Name",
                    email = "freshuser@example.com",
                    password = "password123",
                    passwordConfirmation = "password456"
                )
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals(false, body.success)
        assertEquals("Konfirmasi password tidak cocok", body.errors?.get("password_confirmation"))
    }

    @Test
    fun testRegisterAndLoginIntegration() = testApplication {
        val repo = InMemoryUserRepository()
        application {
            module(repo)
        }
        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        // 1. Register
        val registerResponse = client.post("/api/register") {
            contentType(ContentType.Application.Json)
            setBody(
                RegisterRequest(
                    name = "Integrated User",
                    email = "integrated@example.com",
                    password = "mypassword123",
                    passwordConfirmation = "mypassword123"
                )
            )
        }
        assertEquals(HttpStatusCode.Created, registerResponse.status)

        // 2. Login using the registered credentials
        val loginResponse = client.post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = "integrated@example.com", password = "mypassword123"))
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val loginBody = loginResponse.body<LoginResponse>()
        assertTrue(loginBody.success)
        assertNotNull(loginBody.data?.token)
        assertEquals("integrated@example.com", loginBody.data?.user?.email)
    }

    @Test
    fun testLoginSuccess() = testApplication {

        application {
            module(InMemoryUserRepository())
        }
        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = "user@example.com", password = "password123"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<LoginResponse>()
        assertTrue(body.success)
        assertEquals("Login berhasil", body.message)
        assertNotNull(body.data)
        assertNotNull(body.data?.token)
        assertEquals("user@example.com", body.data?.user?.email)
    }

    @Test
    fun testLoginWrongPassword() = testApplication {
        application {
            module(InMemoryUserRepository())
        }
        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = "user@example.com", password = "wrongpassword"))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals(false, body.success)
        assertEquals("Email atau password salah", body.message)
    }

    @Test
    fun testLoginUserNotFound() = testApplication {
        application {
            module(InMemoryUserRepository())
        }
        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = "nonexistent@example.com", password = "password123"))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals(false, body.success)
        assertEquals("Email atau password salah", body.message)
    }

    @Test
    fun testLoginEmptyEmail() = testApplication {
        application {
            module(InMemoryUserRepository())
        }
        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = "", password = "password123"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals(false, body.success)
        assertNotNull(body.errors)
        assertTrue(body.errors?.containsKey("email") == true)
    }

    @Test
    fun testLoginEmptyPassword() = testApplication {
        application {
            module(InMemoryUserRepository())
        }
        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = "user@example.com", password = ""))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals(false, body.success)
        assertNotNull(body.errors)
        assertTrue(body.errors?.containsKey("password") == true)
    }

    @Test
    fun testLoginInvalidEmailFormat() = testApplication {
        application {
            module(InMemoryUserRepository())
        }
        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val response = client.post("/api/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email = "notanemail", password = "password123"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val body = response.body<ErrorResponse>()
        assertEquals(false, body.success)
        assertNotNull(body.errors)
        assertEquals("Format email tidak valid", body.errors?.get("email"))
    }
}
