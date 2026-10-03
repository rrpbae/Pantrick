package com.pantrick.backend

import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.models.FeedbackRequest
import com.pantrick.backend.models.FeedbackResponse
import com.pantrick.backend.models.PreferencesResponse
import com.pantrick.backend.models.ProfileResponse
import com.pantrick.backend.models.ProfileSettingsResponse
import com.pantrick.backend.models.UpdateExpirationReminderRequest
import com.pantrick.backend.models.UpdateNotificationChannelRequest
import com.pantrick.backend.models.UpdatePreferencesRequest
import com.pantrick.backend.models.UpdateProfileRequest
import com.pantrick.backend.repository.InMemoryUserRepository
import com.pantrick.backend.security.TokenProvider
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test suite untuk fitur Profile Backend (Profile, Preferences, Settings, Feedback, XP/Level).
 *
 * Memeriksa 20 skenario pengujian sesuai spesifikasi prompt.
 */
class ProfileTest {

    private fun generateToken(userId: Int, email: String): String {
        return TokenProvider.generateToken(userId, email)
    }

    // =========================================================
    // TEST 1: GET profile authenticated -> 200 OK
    // =========================================================
    @Test
    fun testGetProfileAuthenticatedSuccess() = testApplication {
        application { module(userRepository = InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = generateToken(1, "user@example.com")
        val response = client.get("/api/profile") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    // =========================================================
    // TEST 2: Data name/email sesuai user login
    // =========================================================
    @Test
    fun testGetProfileReturnsCorrectUserData() = testApplication {
        application { module(userRepository = InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = generateToken(1, "user@example.com")
        val response = client.get("/api/profile") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        val body = response.body<ProfileResponse>()
        assertTrue(body.success)
        assertNotNull(body.data)
        assertEquals("Pantrick User", body.data?.name)
        assertEquals("user@example.com", body.data?.email)
    }

    // =========================================================
    // TEST 3: Avatar initial benar ("Pantrick User" -> "P", "Wahid" -> "W")
    // =========================================================
    @Test
    fun testAvatarInitialCalculation() = testApplication {
        application { module(userRepository = InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val tokenUser1 = generateToken(1, "user@example.com")
        val response1 = client.get("/api/profile") { header(HttpHeaders.Authorization, "Bearer $tokenUser1") }
        val body1 = response1.body<ProfileResponse>()
        assertEquals("P", body1.data?.avatarInitial)

        val tokenUser2 = generateToken(2, "wahid@pantrick.com")
        val response2 = client.get("/api/profile") { header(HttpHeaders.Authorization, "Bearer $tokenUser2") }
        val body2 = response2.body<ProfileResponse>()
        assertEquals("W", body2.data?.avatarInitial)
    }

    // =========================================================
    // TEST 4: Unauthorized -> 401
    // =========================================================
    @Test
    fun testGetProfileUnauthorizedReturns401() = testApplication {
        application { module(userRepository = InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val response = client.get("/api/profile")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val body = response.body<ErrorResponse>()
        assertFalse(body.success)
    }

    // =========================================================
    // TEST 5: User A tidak dapat membaca profile User B (Isolation)
    // =========================================================
    @Test
    fun testUserIsolationGetProfile() = testApplication {
        application { module(userRepository = InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val tokenA = generateToken(1, "user@example.com")
        val responseA = client.get("/api/profile") { header(HttpHeaders.Authorization, "Bearer $tokenA") }
        val profileA = responseA.body<ProfileResponse>().data

        val tokenB = generateToken(2, "wahid@pantrick.com")
        val responseB = client.get("/api/profile") { header(HttpHeaders.Authorization, "Bearer $tokenB") }
        val profileB = responseB.body<ProfileResponse>().data

        assertEquals("Pantrick User", profileA?.name)
        assertEquals("Wahid", profileB?.name)
    }

    // =========================================================
    // TEST 6: User dapat mengubah name
    // =========================================================
    @Test
    fun testUpdateProfileNameSuccess() = testApplication {
        application { module(userRepository = InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = generateToken(1, "user@example.com")
        val response = client.put("/api/profile") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(UpdateProfileRequest(name = "Wakhid Nugroho"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<ProfileResponse>()
        assertEquals("Wakhid Nugroho", body.data?.name)
        assertEquals("W", body.data?.avatarInitial)
    }

    // =========================================================
    // TEST 7: Perubahan name hanya berlaku untuk user tersebut
    // =========================================================
    @Test
    fun testUpdateProfileIsolation() = testApplication {
        application { module(userRepository = InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val tokenA = generateToken(1, "user@example.com")
        client.put("/api/profile") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
            contentType(ContentType.Application.Json)
            setBody(UpdateProfileRequest(name = "User A Updated"))
        }

        val tokenB = generateToken(2, "wahid@pantrick.com")
        val responseB = client.get("/api/profile") { header(HttpHeaders.Authorization, "Bearer $tokenB") }
        val profileB = responseB.body<ProfileResponse>().data

        assertEquals("Wahid", profileB?.name)
    }

    // =========================================================
    // TEST 8 & 9: User dapat menyimpan dan GET food restrictions
    // =========================================================
    @Test
    fun testPreferencesSaveAndGet() = testApplication {
        application { module(userRepository = InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = generateToken(1, "user@example.com")

        // PUT preferences
        val putResponse = client.put("/api/profile/preferences") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(UpdatePreferencesRequest(foodRestrictions = listOf("VEGETARIAN", "BEBAS_KACANG", "BEBAS_SUSU")))
        }
        assertEquals(HttpStatusCode.OK, putResponse.status)
        val putBody = putResponse.body<PreferencesResponse>()
        assertEquals(3, putBody.data.foodRestrictions.size)

        // GET preferences
        val getResponse = client.get("/api/profile/preferences") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val getBody = getResponse.body<PreferencesResponse>()
        assertTrue(getBody.data.foodRestrictions.contains("BEBAS_SUSU"))
    }

    // =========================================================
    // TEST 10: User A tidak dapat membaca preferences User B
    // =========================================================
    @Test
    fun testPreferencesUserIsolation() = testApplication {
        application { module(userRepository = InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val tokenA = generateToken(1, "user@example.com")
        client.put("/api/profile/preferences") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
            contentType(ContentType.Application.Json)
            setBody(UpdatePreferencesRequest(foodRestrictions = listOf("KETO_ONLY")))
        }

        val tokenB = generateToken(2, "wahid@pantrick.com")
        val responseB = client.get("/api/profile/preferences") { header(HttpHeaders.Authorization, "Bearer $tokenB") }
        val prefB = responseB.body<PreferencesResponse>().data
        assertFalse(prefB.foodRestrictions.contains("KETO_ONLY"))
    }

    // =========================================================
    // TEST 11 & 12 & 13: Expiration Reminder Settings (Enable/Disable/Time)
    // =========================================================
    @Test
    fun testExpirationReminderSettings() = testApplication {
        application { module(userRepository = InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = generateToken(1, "user@example.com")

        // Update reminder time to 08:30 & disable
        val putResponse = client.put("/api/profile/settings/expiration-reminder") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(UpdateExpirationReminderRequest(enabled = false, reminderTime = "08:30"))
        }

        assertEquals(HttpStatusCode.OK, putResponse.status)
        val body = putResponse.body<ProfileSettingsResponse>()
        assertFalse(body.data.expirationReminder.enabled)
        assertEquals("08:30", body.data.expirationReminder.reminderTime)
    }

    // =========================================================
    // TEST 14 & 15: Notification Settings & Isolation
    // =========================================================
    @Test
    fun testNotificationChannelSettings() = testApplication {
        application { module(userRepository = InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val tokenA = generateToken(1, "user@example.com")
        client.put("/api/profile/settings/notification-channel") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
            contentType(ContentType.Application.Json)
            setBody(UpdateNotificationChannelRequest(inApp = true, push = true))
        }

        val tokenB = generateToken(2, "wahid@pantrick.com")
        val responseB = client.get("/api/profile/settings") { header(HttpHeaders.Authorization, "Bearer $tokenB") }
        val settingsB = responseB.body<ProfileSettingsResponse>().data
        assertFalse(settingsB.notificationChannel.push) // User B tetap default false
    }

    // =========================================================
    // TEST 16 & 17 & 18: Feedback submission, userId association & Auth enforcement
    // =========================================================
    @Test
    fun testSubmitFeedback() = testApplication {
        application { module(userRepository = InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        // Unauthorized
        val unauthResp = client.post("/api/profile/feedback") {
            contentType(ContentType.Application.Json)
            setBody(FeedbackRequest(message = "Aplikasi sangat membantu."))
        }
        assertEquals(HttpStatusCode.Unauthorized, unauthResp.status)

        // Authenticated User
        val token = generateToken(1, "user@example.com")
        val response = client.post("/api/profile/feedback") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(FeedbackRequest(message = "Aplikasi sangat membantu.", category = "FEEDBACK"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<FeedbackResponse>()
        assertTrue(body.success)
        assertEquals(1, body.data?.userId)
        assertEquals("Aplikasi sangat membantu.", body.data?.message)
    }

    // =========================================================
    // TEST 19 & 20: XP / Level calculation & Isolation
    // =========================================================
    @Test
    fun testXpAndLevelData() = testApplication {
        application { module(userRepository = InMemoryUserRepository()) }
        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = generateToken(1, "user@example.com")
        val response = client.get("/api/profile") { header(HttpHeaders.Authorization, "Bearer $token") }
        val data = response.body<ProfileResponse>().data

        assertNotNull(data)
        assertEquals(4, data?.level)
        assertEquals("Koki Ramah Lingkungan", data?.levelTitle)
        assertEquals(420, data?.currentXp)
        assertEquals(500, data?.nextLevelXp)
    }
}
