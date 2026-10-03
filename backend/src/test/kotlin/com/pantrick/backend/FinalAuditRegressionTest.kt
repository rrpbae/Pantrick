package com.pantrick.backend

import com.pantrick.backend.models.CreatePantryItemRequest
import com.pantrick.backend.models.PantryItemSingleResponse
import com.pantrick.backend.models.StorageType
import com.pantrick.backend.repository.InMemoryPantryRepository
import com.pantrick.backend.repository.InMemoryRecipeRepository
import com.pantrick.backend.repository.InMemorySavedRecipeRepository
import com.pantrick.backend.repository.InMemoryShoppingListRepository
import com.pantrick.backend.repository.InMemoryUserRepository
import com.pantrick.backend.security.TokenProvider
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Assert.assertNotEquals

class FinalAuditRegressionTest {

    private val userRepo = InMemoryUserRepository()
    private val pantryRepo = InMemoryPantryRepository()
    private val recipeRepo = InMemoryRecipeRepository(emptyList())
    private val savedRecipeRepo = InMemorySavedRecipeRepository()

    @Test
    fun testUserIsolationComprehensive() = testApplication {
        application {
            module(
                userRepository = userRepo,
                recipeRepository = recipeRepo,
                pantryRepository = pantryRepo,
                savedRecipeRepository = savedRecipeRepo
            )
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val userA = userRepo.createUser("User A", "usera@example.com", "hash")
        val userB = userRepo.createUser("User B", "userb@example.com", "hash")

        val tokenA = TokenProvider.generateToken(userA.id, userA.email)
        val tokenB = TokenProvider.generateToken(userB.id, userB.email)

        // User A creates pantry item
        val createRespA = client.post("/api/pantry/items") {
            header("Authorization", "Bearer $tokenA")
            contentType(ContentType.Application.Json)
            setBody(CreatePantryItemRequest("Apple", 5.0, "pcs", "Produce", "FRIDGE", "2026-10-10"))
        }
        assertEquals(HttpStatusCode.Created, createRespA.status)
        val itemAId = createRespA.body<PantryItemSingleResponse>().data!!.id

        // User B tries to GET User A's pantry item
        val getRespB = client.get("/api/pantry/items/$itemAId") {
            header("Authorization", "Bearer $tokenB")
        }
        assertEquals(HttpStatusCode.NotFound, getRespB.status) // Or unauthorized depending on implementation

        // User B tries to PUT (modify) User A's pantry item
        val putRespB = client.put("/api/pantry/items/$itemAId") {
            header("Authorization", "Bearer $tokenB")
            contentType(ContentType.Application.Json)
            setBody(CreatePantryItemRequest("Hacked Apple", 5.0, "pcs", "Produce", "FRIDGE", "2026-10-10"))
        }
        assertEquals(HttpStatusCode.NotFound, putRespB.status) // Not found because User B doesn't own it
    }

    @Test
    fun testCrossFeatureFlowA() = testApplication {
        application {
            module(
                userRepository = userRepo,
                recipeRepository = recipeRepo,
                pantryRepository = pantryRepo,
                savedRecipeRepository = savedRecipeRepo
            )
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val userA = userRepo.createUser("FlowA User", "flowa@example.com", "hash")
        val token = TokenProvider.generateToken(userA.id, userA.email)

        // 1. JWT / Create Pantry Item
        val createResp = client.post("/api/pantry/items") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(CreatePantryItemRequest("FlowA Item", 1.0, "kg", "Pantry", "PANTRY", "2026-12-31"))
        }
        assertEquals(HttpStatusCode.Created, createResp.status)

        // 2. Home
        val homeResp = client.get("/api/home") {
            header("Authorization", "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, homeResp.status)

        // 3. Notification
        val notifResp = client.get("/api/notifications") {
            header("Authorization", "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, notifResp.status)
    }

    @Test
    fun testMalformedJsonHandling() = testApplication {
        application {
            module(
                userRepository = userRepo,
                recipeRepository = recipeRepo,
                pantryRepository = pantryRepo,
                savedRecipeRepository = savedRecipeRepo
            )
        }
        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val userA = userRepo.createUser("Malformed User", "malformed@example.com", "hash")
        val token = TokenProvider.generateToken(userA.id, userA.email)

        val resp = client.post("/api/pantry/items") {
            header("Authorization", "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("{ invalid json ")
        }
        
        // Ktor default parsing failure often results in 400 BadRequest
        assertTrue(resp.status == HttpStatusCode.BadRequest || resp.status == HttpStatusCode.UnsupportedMediaType)
    }
}
