package com.pantrick.backend

import com.pantrick.backend.models.PantryItem
import com.pantrick.backend.models.Recipe
import com.pantrick.backend.models.RecipeIngredient
import com.pantrick.backend.models.RecommendationResponse
import com.pantrick.backend.models.StorageType
import com.pantrick.backend.repository.InMemoryCookingHistoryRepository
import com.pantrick.backend.repository.InMemoryPantryRepository
import com.pantrick.backend.repository.InMemoryRecipeRepository
import com.pantrick.backend.repository.InMemorySavedRecipeRepository
import com.pantrick.backend.repository.InMemoryShoppingListRepository
import com.pantrick.backend.repository.InMemoryUserRepository
import com.pantrick.backend.security.TokenProvider
import com.pantrick.backend.service.IngredientParser
import com.pantrick.backend.service.RecipeDatasetLoader
import com.pantrick.backend.service.RecommendationService
import com.pantrick.backend.service.SavedRecipeService
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecipeSavedTest {

    private val json = Json { ignoreUnknownKeys = true }
    private val userRepo = InMemoryUserRepository()
    private val defaultUser = userRepo.findByEmail("user@example.com")!!

    private fun createTestRecipes(): List<Recipe> {
        return listOf(
            Recipe(
                id = "recipe-1",
                title = "Ayam Goreng Mentega",
                instructions = "Goreng ayam lalu tumis mentega",
                cookingTimeMinutes = 15,
                ingredients = listOf(
                    RecipeIngredient(raw = "Ayam", displayName = "Ayam", normalizedName = "ayam"),
                    RecipeIngredient(raw = "Mentega", displayName = "Mentega", normalizedName = "mentega")
                )
            ),
            Recipe(
                id = "recipe-2",
                title = "Sop Ayam",
                instructions = "Rebus ayam dengan wortel",
                cookingTimeMinutes = 30,
                ingredients = listOf(
                    RecipeIngredient(raw = "Ayam", displayName = "Ayam", normalizedName = "ayam"),
                    RecipeIngredient(raw = "Wortel", displayName = "Wortel", normalizedName = "wortel"),
                    RecipeIngredient(raw = "Garam", displayName = "Garam", normalizedName = "garam")
                )
            ),
            Recipe(
                id = "recipe-3",
                title = "Telur Dadar Cepat",
                instructions = "Kocok telur lalu goreng",
                cookingTimeMinutes = 10,
                ingredients = listOf(
                    RecipeIngredient(raw = "Telur", displayName = "Telur", normalizedName = "telur")
                )
            )
        )
    }

    private fun makeItem(id: String, userId: Int, name: String, quantity: Double = 1.0, unit: String = "unit"): PantryItem = PantryItem(
        id = id,
        userId = userId,
        name = name,
        ingredientName = name,
        normalizedName = IngredientParser.normalizeIngredientName(name),
        quantity = quantity,
        unit = unit,
        storageType = StorageType.PANTRY
    )

    // --- UNIT & SERVICE TESTS ---

    @Test
    fun testUserCanSaveAndUnsaveRecipe() {
        val repo = InMemorySavedRecipeRepository()
        val recipeRepo = InMemoryRecipeRepository(createTestRecipes())
        val service = SavedRecipeService(repo, recipeRepo, InMemoryCookingHistoryRepository(), InMemoryShoppingListRepository())

        val userId1 = 1
        val userId2 = 2
        val recipeId = "recipe-1"

        assertTrue(service.saveRecipe(userId1, recipeId))
        assertTrue(service.isRecipeSaved(userId1, recipeId))
        assertFalse(service.isRecipeSaved(userId2, recipeId)) // User isolation

        val saved1 = service.getSavedRecipes(userId1)
        assertEquals(1, saved1.size)
        assertEquals("recipe-1", saved1[0].id)

        val saved2 = service.getSavedRecipes(userId2)
        assertEquals(0, saved2.size)

        assertTrue(service.unsaveRecipe(userId1, recipeId))
        assertFalse(service.isRecipeSaved(userId1, recipeId))
    }

    @Test
    fun testCannotSaveNonExistentRecipe() {
        val repo = InMemorySavedRecipeRepository()
        val recipeRepo = InMemoryRecipeRepository(createTestRecipes())
        val service = SavedRecipeService(repo, recipeRepo, InMemoryCookingHistoryRepository(), InMemoryShoppingListRepository())

        assertFalse(service.saveRecipe(userId = 1, recipeId = "invalid-recipe-999"))
    }

    @Test
    fun testRecipeCollectionsManagementAndIsolation() {
        val repo = InMemorySavedRecipeRepository()
        val recipeRepo = InMemoryRecipeRepository(createTestRecipes())
        val service = SavedRecipeService(repo, recipeRepo, InMemoryCookingHistoryRepository(), InMemoryShoppingListRepository())

        val col1 = service.createCollection(userId = 1, name = "Favoritku")
        assertNotNull(col1)
        assertEquals("Favoritku", col1.name)

        // User 2 cannot access user 1 collection
        assertNull(service.getCollectionById(userId = 2, collectionId = col1.id))

        // Add recipe to collection
        val updatedCol = service.addRecipeToCollection(userId = 1, collectionId = col1.id, recipeId = "recipe-1")
        assertNotNull(updatedCol)
        assertTrue(updatedCol!!.recipeIds.contains("recipe-1"))

        // Add non-existent recipe to collection fails
        assertNull(service.addRecipeToCollection(userId = 1, collectionId = col1.id, recipeId = "invalid-recipe"))

        // Remove recipe from collection
        val removedCol = service.removeRecipeFromCollection(userId = 1, collectionId = col1.id, recipeId = "recipe-1")
        assertNotNull(removedCol)
        assertFalse(removedCol!!.recipeIds.contains("recipe-1"))
    }

    @Test
    fun testAddOnlyMissingIngredientsToShoppingList() {
        val savedRepo = InMemorySavedRecipeRepository()
        val recipeRepo = InMemoryRecipeRepository(createTestRecipes())
        val pantryRepo = InMemoryPantryRepository()
        listOf("pantry-1", "pantry-2", "pantry-3", "pantry-4", "pantry-5").forEach { pantryRepo.deleteItem(it, 1) }
        pantryRepo.addItem(makeItem("p1", 1, "Ayam"))

        val service = SavedRecipeService(savedRepo, recipeRepo, InMemoryCookingHistoryRepository(), InMemoryShoppingListRepository(), pantryRepo)

        // recipe-1 has Ayam & Mentega. User 1 already has Ayam in pantry.
        val addedItems = service.addMissingIngredientsToShoppingList(userId = 1, recipeId = "recipe-1")
        assertNotNull(addedItems)
        assertEquals(1, addedItems!!.size)
        assertEquals("Mentega", addedItems[0].ingredientName) // HANYA mentega yang kurang
    }

    @Test
    fun testCookingHistoryWorking() {
        val savedRepo = InMemorySavedRecipeRepository()
        val recipeRepo = InMemoryRecipeRepository(createTestRecipes())
        val cookRepo = InMemoryCookingHistoryRepository()
        val service = SavedRecipeService(savedRepo, recipeRepo, cookRepo, InMemoryShoppingListRepository())

        val record = service.recordCooking(userId = 1, recipeId = "recipe-1")
        assertNotNull(record)
        assertEquals("recipe-1", record!!.recipeId)
        assertEquals(1, service.getCookingHistory(userId = 1).size)
    }

    @Test
    fun testRecommendationFilterAndSort() {
        val recipeRepo = InMemoryRecipeRepository(createTestRecipes())
        val pantryRepo = InMemoryPantryRepository()
        listOf("pantry-1", "pantry-2", "pantry-3", "pantry-4", "pantry-5").forEach { pantryRepo.deleteItem(it, 1) }
        pantryRepo.addItem(makeItem("p1", 1, "Ayam"))
        pantryRepo.addItem(makeItem("p2", 1, "Mentega"))

        val recService = RecommendationService(recipeRepo, pantryRepo)

        // 100% Ready filter -> recipe-1 (Ayam & Mentega)
        val (readyRecs, _) = recService.getRecommendations(userId = 1, filter = "ready")
        assertEquals(1, readyRecs.size)
        assertEquals("recipe-1", readyRecs[0].recipe.id)

        // < 20m Quick filter -> recipe-1 (15m), recipe-3 (10m)
        val (quickRecs, _) = recService.getRecommendations(userId = 1, filter = "quick")
        assertTrue(quickRecs.all { (it.recipe.cookingTimeMinutes ?: 99) < 20 })

        // Ingredient search "Wortel" -> recipe-2
        val (searchRecs, _) = recService.getRecommendations(userId = 1, search = "Wortel")
        assertEquals(1, searchRecs.size)
        assertEquals("recipe-2", searchRecs[0].recipe.id)
    }

    // --- INTEGRATION TESTS ON REAL DATASET ---

    @Test
    fun testRealDatasetLoaderAndRecommendationIntegration() {
        val datasetResult = RecipeDatasetLoader.loadDataset()
        assertTrue("Dataset resep tidak boleh kosong", datasetResult.recipes.isNotEmpty())
        assertTrue("Dataset harus berisi banyak resep (>1000)", datasetResult.recipes.size >= 1000)

        val recipeRepo = InMemoryRecipeRepository(datasetResult.recipes)
        val pantryRepo = InMemoryPantryRepository()
        listOf("pantry-1", "pantry-2", "pantry-3", "pantry-4", "pantry-5").forEach { pantryRepo.deleteItem(it, 1) }
        pantryRepo.addItem(makeItem("p1", 1, "butter"))
        pantryRepo.addItem(makeItem("p2", 1, "garlic"))
        pantryRepo.addItem(makeItem("p3", 1, "milk"))

        val recService = RecommendationService(recipeRepo, pantryRepo)
        val (recs, pantryCount) = recService.getRecommendations(userId = 1, limit = 5)

        assertTrue("Harus ada rekomendasi resep dari dataset asli", recs.isNotEmpty())
        assertEquals(3, pantryCount)

        val topRec = recs.first()
        assertNotNull(topRec.recipe.id)
        assertNotNull(recipeRepo.getRecipeById(topRec.recipe.id))
        assertTrue("Matched count harus > 0", topRec.matchedCount > 0)
        assertTrue("Match percentage harus > 0", topRec.matchPercentage > 0.0)
    }

    // --- HTTP ENDPOINT INTEGRATION TESTS ---

    @Test
    fun testSavedRecipeEndpointAuthenticationRequired() = testApplication {
        application { module(userRepo) }

        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val response = client.get("/api/recipes/saved")
        assertEquals(HttpStatusCode.Unauthorized, response.status)

        val saveResp = client.post("/api/recipes/1/save")
        assertEquals(HttpStatusCode.Unauthorized, saveResp.status)

        val colResp = client.get("/api/recipe-collections")
        assertEquals(HttpStatusCode.Unauthorized, colResp.status)
    }

    @Test
    fun testFullSavedRecipeAndCollectionFlow() = testApplication {
        val sampleRecipes = createTestRecipes()
        val recipeRepo = InMemoryRecipeRepository(sampleRecipes)
        val pantryRepo = InMemoryPantryRepository()
        listOf("pantry-1", "pantry-2", "pantry-3", "pantry-4", "pantry-5").forEach { pantryRepo.deleteItem(it, 1) }
        pantryRepo.addItem(makeItem("p1", 1, "Ayam"))

        application { module(userRepo, recipeRepo, pantryRepo) }

        val client = createClient {
            install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

        val token = TokenProvider.generateToken(defaultUser.id, defaultUser.email)

        // GET /api/recipes/recommended
        val recResp = client.get("/api/recipes/recommended?limit=5") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals("GET /api/recipes/recommended harus HTTP 200", HttpStatusCode.OK, recResp.status)
        val recBody = recResp.body<RecommendationResponse>()
        assertTrue("Recommendation response success harus true", recBody.success)
        assertTrue("Daftar rekomendasi tidak boleh kosong", recBody.recommendations.isNotEmpty())

        val targetRecipeId = recBody.recommendations[0].recipe.id

        // Save recipe
        val saveResp = client.post("/api/recipes/$targetRecipeId/save") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals("Save recipe harus HTTP 200", HttpStatusCode.OK, saveResp.status)

        // Verify is saved
        val isSavedResp = client.get("/api/recipes/$targetRecipeId/saved") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals("Get isSaved status harus HTTP 200", HttpStatusCode.OK, isSavedResp.status)

        // Create Collection
        val createColResp = client.post("/api/recipe-collections") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"name": "Dinner Ideas"}""")
        }
        assertEquals("Create collection harus HTTP 201", HttpStatusCode.Created, createColResp.status)

        // Get Smart Menu
        val smartMenuResp = client.get("/api/recipes/recommended/smart-menu") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals("Smart Menu harus HTTP 200", HttpStatusCode.OK, smartMenuResp.status)

        // Instant Dinner
        val dinnerResp = client.post("/api/recipes/recommended/instant-dinner") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals("Instant Dinner harus HTTP 200", HttpStatusCode.OK, dinnerResp.status)

        // Add missing ingredients to shopping list
        val shopResp = client.post("/api/shopping-list/add-missing-ingredients") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"recipeId": "$targetRecipeId"}""")
        }
        assertEquals("Add missing ingredients harus HTTP 200", HttpStatusCode.OK, shopResp.status)

        // Record cooking
        val cookResp = client.post("/api/recipes/$targetRecipeId/cook") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals("Record cooking harus HTTP 200", HttpStatusCode.OK, cookResp.status)

        // Unsave recipe
        val unsaveResp = client.delete("/api/recipes/$targetRecipeId/save") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals("Unsave recipe harus HTTP 200", HttpStatusCode.OK, unsaveResp.status)
    }
}
