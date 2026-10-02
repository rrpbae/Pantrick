package com.pantrick.backend

import com.pantrick.backend.models.CreatePantryItemRequest
import com.pantrick.backend.models.ExpirationStatus
import com.pantrick.backend.models.PantryCategoriesResponse
import com.pantrick.backend.models.PantryItemListResponse
import com.pantrick.backend.models.PantryItemSingleResponse
import com.pantrick.backend.models.PantrySummaryResponse
import com.pantrick.backend.models.Recipe
import com.pantrick.backend.models.StorageType
import com.pantrick.backend.models.UpdatePantryItemRequest
import com.pantrick.backend.repository.InMemoryPantryRepository
import com.pantrick.backend.repository.InMemoryRecipeRepository
import com.pantrick.backend.repository.InMemoryUserRepository
import com.pantrick.backend.security.TokenProvider
import com.pantrick.backend.service.IngredientParser
import com.pantrick.backend.service.PantryService
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.delete
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
import java.time.LocalDate

class PantryTest {

    private val userRepo = InMemoryUserRepository()
    private val defaultUser = userRepo.findByEmail("user@example.com")!!
    private val userB = userRepo.createUser("User B", "userb@example.com", "hash")

    private val sampleRecipes = listOf(
        Recipe(
            id = "0",
            title = "Creamy Garlic Herb Pasta",
            instructions = "Boil pasta.",
            imageName = "creamy-garlic-pasta",
            hasImage = true,
            ingredients = IngredientParser.parseIngredientsList("['1 cup milk', '2 tbsp butter']")
        )
    )

    private val recipeRepo = InMemoryRecipeRepository(sampleRecipes)

    @Test
    fun testPantryCrudService() {
        val pantryRepo = InMemoryPantryRepository()
        val service = PantryService(pantryRepo)
        val nowDate = LocalDate.of(2026, 10, 1)

        // 1. Create
        val createReq = CreatePantryItemRequest(
            name = "Fresh Apples",
            quantity = 5.0,
            unit = "pcs",
            category = "Produce",
            storageType = "FRIDGE",
            expirationDate = "2026-10-05"
        )
        val created = service.createPantryItem(defaultUser.id, createReq, nowDate)
        assertNotNull(created.id)
        assertEquals("Fresh Apples", created.name)
        assertEquals(StorageType.FRIDGE, created.storageType)
        assertEquals(ExpirationStatus.FRESH, created.expirationStatus)

        // 2. Read List
        val list = service.getPantryItems(defaultUser.id, nowDate = nowDate)
        assertTrue(list.any { it.name == "Fresh Apples" })

        // 3. Read Detail
        val detail = service.getPantryItemById(created.id, defaultUser.id, nowDate)
        assertNotNull(detail)
        assertEquals("Fresh Apples", detail?.name)

        // 4. Update
        val updateReq = UpdatePantryItemRequest(
            name = "Honeycrisp Apples",
            quantity = 10.0,
            storageType = "FREEZER"
        )
        val updated = service.updatePantryItem(created.id, defaultUser.id, updateReq, nowDate)
        assertNotNull(updated)
        assertEquals("Honeycrisp Apples", updated?.name)
        assertEquals(10.0, updated?.quantity ?: 0.0, 0.01)
        assertEquals(StorageType.FREEZER, updated?.storageType)

        // 5. Delete
        val deleted = service.deletePantryItem(created.id, defaultUser.id)
        assertTrue(deleted)
        val afterDelete = service.getPantryItemById(created.id, defaultUser.id, nowDate)
        assertEquals(null, afterDelete)
    }

    @Test
    fun testStorageFilterAndSearchAndCategories() {
        val pantryRepo = InMemoryPantryRepository()
        val service = PantryService(pantryRepo)
        val nowDate = LocalDate.of(2026, 10, 1)

        // Filter FRIDGE
        val fridgeItems = service.getPantryItems(defaultUser.id, storageTypeFilter = "FRIDGE", nowDate = nowDate)
        assertTrue(fridgeItems.all { it.storageType == StorageType.FRIDGE })

        // Filter FREEZER
        val freezerItems = service.getPantryItems(defaultUser.id, storageTypeFilter = "FREEZER", nowDate = nowDate)
        assertTrue(freezerItems.all { it.storageType == StorageType.FREEZER })

        // Filter PANTRY
        val pantryItems = service.getPantryItems(defaultUser.id, storageTypeFilter = "PANTRY", nowDate = nowDate)
        assertTrue(pantryItems.all { it.storageType == StorageType.PANTRY })

        // Search item
        val searchResults = service.getPantryItems(defaultUser.id, searchQuery = "milk", nowDate = nowDate)
        assertTrue(searchResults.isNotEmpty())
        assertTrue(searchResults.any { it.name.lowercase().contains("milk") })

        // Categories
        val categories = service.getPantryCategories(defaultUser.id)
        assertTrue(categories.contains("Dairy & Eggs"))
    }

    @Test
    fun testExpirationStatusAndNeedsAttentionAndSummary() {
        val pantryRepo = InMemoryPantryRepository()
        val service = PantryService(pantryRepo)
        val nowDate = LocalDate.of(2026, 10, 1)

        val summary = service.getPantrySummary(defaultUser.id, nowDate)
        assertTrue(summary.totalItems > 0)
        assertTrue(summary.expiringSoonItems > 0)

        val attention = service.getNeedsAttentionItems(defaultUser.id, nowDate)
        assertTrue(attention.isNotEmpty())
        assertTrue(attention.all { it.expirationStatus == ExpirationStatus.EXPIRED || it.expirationStatus == ExpirationStatus.EXPIRING_SOON })
    }

    @Test
    fun testUserIsolationUserACannotAccessUserBItems() {
        val pantryRepo = InMemoryPantryRepository()
        val service = PantryService(pantryRepo)
        val nowDate = LocalDate.of(2026, 10, 1)

        // Item created by User A
        val createReq = CreatePantryItemRequest(name = "Secret Milk", quantity = 1.0, unit = "liter")
        val itemA = service.createPantryItem(defaultUser.id, createReq, nowDate)

        // User B tries to get detail of User A item
        val detailForB = service.getPantryItemById(itemA.id, userB.id, nowDate)
        assertEquals(null, detailForB)

        // User B tries to update item A
        val updateForB = service.updatePantryItem(itemA.id, userB.id, UpdatePantryItemRequest(name = "Hacked"), nowDate)
        assertEquals(null, updateForB)

        // User B tries to delete item A
        val deleteForB = service.deletePantryItem(itemA.id, userB.id)
        assertEquals(false, deleteForB)
    }

    @Test
    fun testPantryEndpointsIntegration() = testApplication {
        val pantryRepo = InMemoryPantryRepository()

        application {
            module(
                userRepository = userRepo,
                recipeRepository = recipeRepo,
                pantryRepository = pantryRepo
            )
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = true
                })
            }
        }

        val validToken = TokenProvider.generateToken(defaultUser.id, defaultUser.email)

        // 1. GET /api/pantry/summary
        val summaryResp = client.get("/api/pantry/summary") {
            header("Authorization", "Bearer $validToken")
        }
        assertEquals(HttpStatusCode.OK, summaryResp.status)
        val summaryBody = summaryResp.body<PantrySummaryResponse>()
        assertTrue(summaryBody.success)
        assertTrue(summaryBody.data.totalItems > 0)

        // 2. GET /api/pantry/categories
        val catResp = client.get("/api/pantry/categories") {
            header("Authorization", "Bearer $validToken")
        }
        assertEquals(HttpStatusCode.OK, catResp.status)
        val catBody = catResp.body<PantryCategoriesResponse>()
        assertTrue(catBody.success)
        assertTrue(catBody.data.isNotEmpty())

        // 3. POST /api/pantry/items
        val createResp = client.post("/api/pantry/items") {
            header("Authorization", "Bearer $validToken")
            contentType(ContentType.Application.Json)
            setBody(
                CreatePantryItemRequest(
                    name = "Greek Yogurt",
                    quantity = 450.0,
                    unit = "g",
                    category = "Dairy & Eggs",
                    storageType = "FRIDGE",
                    expirationDate = "2026-10-10"
                )
            )
        }
        assertEquals(HttpStatusCode.Created, createResp.status)
        val createBody = createResp.body<PantryItemSingleResponse>()
        assertTrue(createBody.success)
        val createdId = createBody.data!!.id
        assertEquals("Greek Yogurt", createBody.data!!.name)

        // 4. GET /api/pantry/items?storageType=FRIDGE
        val listResp = client.get("/api/pantry/items?storageType=FRIDGE") {
            header("Authorization", "Bearer $validToken")
        }
        assertEquals(HttpStatusCode.OK, listResp.status)
        val listBody = listResp.body<PantryItemListResponse>()
        assertTrue(listBody.success)
        assertTrue(listBody.data.any { it.id == createdId })

        // 5. PUT /api/pantry/items/{id}
        val updateResp = client.put("/api/pantry/items/$createdId") {
            header("Authorization", "Bearer $validToken")
            contentType(ContentType.Application.Json)
            setBody(UpdatePantryItemRequest(quantity = 500.0))
        }
        assertEquals(HttpStatusCode.OK, updateResp.status)

        // 6. DELETE /api/pantry/items/{id}
        val deleteResp = client.delete("/api/pantry/items/$createdId") {
            header("Authorization", "Bearer $validToken")
        }
        assertEquals(HttpStatusCode.OK, deleteResp.status)

        // 7. GET /api/pantry/items with Unauthorized
        val unauthResp = client.get("/api/pantry/items")
        assertEquals(HttpStatusCode.Unauthorized, unauthResp.status)
    }
}
