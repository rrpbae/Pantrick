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

class AuthTest {

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
