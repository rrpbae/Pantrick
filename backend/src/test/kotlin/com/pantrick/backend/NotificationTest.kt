package com.pantrick.backend

import com.pantrick.backend.models.CreatePantryItemRequest
import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.models.ExpirationPriority
import com.pantrick.backend.models.ExpirationStatus
import com.pantrick.backend.models.MarkNotificationReadResponse
import com.pantrick.backend.models.NotificationListResponse
import com.pantrick.backend.repository.InMemoryPantryRepository
import com.pantrick.backend.repository.InMemoryUserRepository
import com.pantrick.backend.security.TokenProvider
import com.pantrick.backend.service.PantryService
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
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
import java.time.LocalDate

/**
 * Test suite untuk fitur Notification Backend (Expiration Notifications).
 *
 * Memeriksa 12 skenario pengujian sesuai spesifikasi prompt.
 */
class NotificationTest {

    // Helper untuk membuat token JWT valid
    private fun generateToken(userId: Int, email: String): String {
        return TokenProvider.generateToken(userId, email)
    }

    // =========================================================
    // TEST 1: Pantry kosong -> notifications kosong
    // =========================================================
    @Test
    fun testEmptyPantryReturnsEmptyNotifications() = testApplication {
        val pantryRepo = InMemoryPantryRepository()
        // Clear demo items
        val user1Items = pantryRepo.getItemsByUserId(1)
        user1Items.forEach { pantryRepo.deleteItem(it.id, 1) }

        application { module(userRepository = InMemoryUserRepository(), pantryRepository = pantryRepo) }

        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = generateToken(1, "user@example.com")
        val response = client.get("/api/notifications") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<NotificationListResponse>()
        assertTrue(body.success)
        assertEquals(0, body.unreadCount)
        assertTrue(body.notifications.isEmpty())
    }

    // =========================================================
    // TEST 2: Bahan masih fresh -> tidak menghasilkan notification
    // =========================================================
    @Test
    fun testFreshItemsDoNotGenerateNotifications() = testApplication {
        val pantryRepo = InMemoryPantryRepository()
        val user1Items = pantryRepo.getItemsByUserId(1)
        user1Items.forEach { pantryRepo.deleteItem(it.id, 1) }

        // Tambah bahan yang exp 30 hari lagi
        val pantryService = PantryService(pantryRepo)
        pantryService.createPantryItem(
            userId = 1,
            request = CreatePantryItemRequest(
                name = "Garam Dapur",
                quantity = 1.0,
                unit = "Pack",
                category = "Pantry",
                expirationDate = "2026-11-01"
            ),
            nowDate = LocalDate.of(2026, 10, 1)
        )

        application { module(userRepository = InMemoryUserRepository(), pantryRepository = pantryRepo) }

        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = generateToken(1, "user@example.com")
        val response = client.get("/api/notifications") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<NotificationListResponse>()
        assertEquals(0, body.unreadCount)
        assertTrue(body.notifications.isEmpty())
    }

    // =========================================================
    // TEST 3 & 5 & 6 & 7: Bahan akan expired (2 hari lagi) -> menghasilkan notification
    // =========================================================
    @Test
    fun testExpiringSoonItemGeneratesNotification() = testApplication {
        val pantryRepo = InMemoryPantryRepository()
        val user1Items = pantryRepo.getItemsByUserId(1)
        user1Items.forEach { pantryRepo.deleteItem(it.id, 1) }

        val pantryService = PantryService(pantryRepo)
        pantryService.createPantryItem(
            userId = 1,
            request = CreatePantryItemRequest(
                name = "Susu UHT",
                quantity = 1.0,
                unit = "Liter",
                category = "Dairy & Eggs",
                expirationDate = "2026-10-05"
            ),
            nowDate = LocalDate.of(2026, 10, 3)
        )

        application { module(userRepository = InMemoryUserRepository(), pantryRepository = pantryRepo) }

        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = generateToken(1, "user@example.com")
        val response = client.get("/api/notifications") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<NotificationListResponse>()
        assertEquals(1, body.notifications.size)

        val notif = body.notifications.first()
        assertEquals("Susu UHT", notif.name)
        assertEquals(2L, notif.daysRemaining)
        assertEquals(ExpirationStatus.EXPIRING_SOON, notif.type)
        assertEquals("Susu UHT akan kedaluwarsa dalam 2 hari.", notif.message)
    }

    // =========================================================
    // TEST 4: Bahan sudah expired -> menghasilkan notification EXPIRED
    // =========================================================
    @Test
    fun testExpiredItemGeneratesNotification() = testApplication {
        val pantryRepo = InMemoryPantryRepository()
        val user1Items = pantryRepo.getItemsByUserId(1)
        user1Items.forEach { pantryRepo.deleteItem(it.id, 1) }

        val pantryService = PantryService(pantryRepo)
        pantryService.createPantryItem(
            userId = 1,
            request = CreatePantryItemRequest(
                name = "Cabai Rawit",
                quantity = 100.0,
                unit = "gram",
                category = "Produce",
                expirationDate = "2026-10-01"
            ),
            nowDate = LocalDate.of(2026, 10, 3)
        )

        application { module(userRepository = InMemoryUserRepository(), pantryRepository = pantryRepo) }

        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = generateToken(1, "user@example.com")
        val response = client.get("/api/notifications") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<NotificationListResponse>()
        assertEquals(1, body.notifications.size)

        val notif = body.notifications.first()
        assertEquals("Cabai Rawit", notif.name)
        assertEquals(-2L, notif.daysRemaining)
        assertEquals(ExpirationStatus.EXPIRED, notif.type)
        assertEquals(ExpirationPriority.HIGH, notif.priority)
        assertEquals("Cabai Rawit sudah kedaluwarsa.", notif.message)
    }

    // =========================================================
    // TEST 8: unreadCount hitungannya benar
    // =========================================================
    @Test
    fun testUnreadCountIsCorrect() = testApplication {
        val pantryRepo = InMemoryPantryRepository()
        val user1Items = pantryRepo.getItemsByUserId(1)
        user1Items.forEach { pantryRepo.deleteItem(it.id, 1) }

        val pantryService = PantryService(pantryRepo)
        pantryService.createPantryItem(
            userId = 1,
            request = CreatePantryItemRequest(name = "Item A", quantity = 1.0, unit = "pcs", expirationDate = "2026-10-02"),
            nowDate = LocalDate.of(2026, 10, 3)
        )
        pantryService.createPantryItem(
            userId = 1,
            request = CreatePantryItemRequest(name = "Item B", quantity = 1.0, unit = "pcs", expirationDate = "2026-10-04"),
            nowDate = LocalDate.of(2026, 10, 3)
        )

        application { module(userRepository = InMemoryUserRepository(), pantryRepository = pantryRepo) }

        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = generateToken(1, "user@example.com")
        val response = client.get("/api/notifications") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        val body = response.body<NotificationListResponse>()
        assertEquals(2, body.notifications.size)
        assertEquals(2, body.unreadCount)
    }

    // =========================================================
    // TEST 9: Mark as read berhasil & mengurangi unreadCount
    // =========================================================
    @Test
    fun testMarkAsReadSuccess() = testApplication {
        val pantryRepo = InMemoryPantryRepository()
        val user1Items = pantryRepo.getItemsByUserId(1)
        user1Items.forEach { pantryRepo.deleteItem(it.id, 1) }

        val pantryService = PantryService(pantryRepo)
        val created = pantryService.createPantryItem(
            userId = 1,
            request = CreatePantryItemRequest(name = "Keju Prochiz", quantity = 1.0, unit = "box", expirationDate = "2026-10-04"),
            nowDate = LocalDate.of(2026, 10, 3)
        )

        application { module(userRepository = InMemoryUserRepository(), pantryRepository = pantryRepo) }

        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = generateToken(1, "user@example.com")
        val notifId = "notif-${created.id}"

        // Mark as read
        val markResponse = client.patch("/api/notifications/$notifId/read") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, markResponse.status)
        val markBody = markResponse.body<MarkNotificationReadResponse>()
        assertTrue(markBody.success)
        assertTrue(markBody.data?.isRead == true)

        // Verifikasi unreadCount berkurang
        val getResponse = client.get("/api/notifications") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        val getBody = getResponse.body<NotificationListResponse>()
        assertEquals(0, getBody.unreadCount)
        assertTrue(getBody.notifications.first().isRead)
    }

    // =========================================================
    // TEST 10 & 11: User isolation — User A tidak dapat mengakses/melihat notification User B
    // =========================================================
    @Test
    fun testUserIsolationNotifications() = testApplication {
        val pantryRepo = InMemoryPantryRepository()
        // Hapus demo items
        pantryRepo.getItemsByUserId(1).forEach { pantryRepo.deleteItem(it.id, 1) }
        pantryRepo.getItemsByUserId(2).forEach { pantryRepo.deleteItem(it.id, 2) }

        val pantryService = PantryService(pantryRepo)
        // User 1
        pantryService.createPantryItem(
            userId = 1,
            request = CreatePantryItemRequest(name = "Susu User A", quantity = 1.0, unit = "pcs", expirationDate = "2026-10-04"),
            nowDate = LocalDate.of(2026, 10, 3)
        )
        // User 2
        val itemB = pantryService.createPantryItem(
            userId = 2,
            request = CreatePantryItemRequest(name = "Daging User B", quantity = 1.0, unit = "kg", expirationDate = "2026-10-04"),
            nowDate = LocalDate.of(2026, 10, 3)
        )

        application { module(userRepository = InMemoryUserRepository(), pantryRepository = pantryRepo) }

        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val tokenA = generateToken(1, "user@example.com")
        val tokenB = generateToken(2, "wahid@pantrick.com")

        // User A GET notifications
        val responseA = client.get("/api/notifications") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
        }
        val bodyA = responseA.body<NotificationListResponse>()
        assertEquals(1, bodyA.notifications.size)
        assertEquals("Susu User A", bodyA.notifications.first().name)

        // User A coba mark as read notification milik User B
        val notifIdB = "notif-${itemB.id}"
        val markResponse = client.patch("/api/notifications/$notifIdB/read") {
            header(HttpHeaders.Authorization, "Bearer $tokenA")
        }

        assertEquals(HttpStatusCode.NotFound, markResponse.status)
    }

    // =========================================================
    // TEST 12: Request tanpa JWT menghasilkan 401 Unauthorized
    // =========================================================
    @Test
    fun testRequestWithoutJwtReturnsUnauthorized() = testApplication {
        application { module(userRepository = InMemoryUserRepository()) }

        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val response = client.get("/api/notifications")
        assertEquals(HttpStatusCode.Unauthorized, response.status)

        val errorBody = response.body<ErrorResponse>()
        assertFalse(errorBody.success)
    }
}
