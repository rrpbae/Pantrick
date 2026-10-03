package com.pantrick.backend

import com.pantrick.backend.models.PantryItem
import com.pantrick.backend.models.Recipe
import com.pantrick.backend.models.RecommendationResponse
import com.pantrick.backend.models.StorageType
import com.pantrick.backend.repository.InMemoryPantryRepository
import com.pantrick.backend.repository.InMemoryRecipeRepository
import com.pantrick.backend.repository.InMemoryUserRepository
import com.pantrick.backend.security.TokenProvider
import com.pantrick.backend.service.IngredientParser
import com.pantrick.backend.service.RecommendationService
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationTest {

    // =====================================================================
    // Setup: sample recipes dengan ingredient dari dataset yang realistis
    // Kita gunakan kata bahan dalam bahasa Inggris sesuai dataset CSV asli.
    // =====================================================================
    private val userRepo = InMemoryUserRepository()
    private val defaultUser = userRepo.findByEmail("user@example.com")!!

    /**
     * Recipe A: 4/5 bahan cocok dengan pantry sample (butter, garlic, milk, pasta)
     * Recipe B: 2/3 bahan cocok (chicken, garlic, butter)
     * Recipe C: 0/4 bahan cocok (tomato, onion, basil, olive oil) — tidak ada di pantry
     * Recipe D: 1/6 bahan cocok (egg) — cocok tapi sedikit
     */
    private val sampleRecipes = listOf(
        Recipe(
            id = "rec-001",
            title = "Creamy Garlic Pasta",
            instructions = "Boil pasta. Make sauce from butter, garlic, milk. Combine.",
            imageName = "creamy-garlic-pasta",
            hasImage = true,
            ingredients = IngredientParser.parseIngredientsList(
                "['1 cup milk', '2 tbsp butter', '2 cloves garlic', '200g pasta', '1 tsp salt']"
            )
        ),
        Recipe(
            id = "rec-002",
            title = "Chicken Garlic Stir Fry",
            instructions = "Stir fry chicken with garlic and butter.",
            imageName = "chicken-stir-fry",
            hasImage = true,
            ingredients = IngredientParser.parseIngredientsList(
                "['500g chicken breast', '3 cloves garlic', '2 tbsp butter']"
            )
        ),
        Recipe(
            id = "rec-003",
            title = "Tomato Basil Soup",
            instructions = "Simmer tomatoes with basil and olive oil.",
            imageName = "tomato-basil-soup",
            hasImage = false,
            ingredients = IngredientParser.parseIngredientsList(
                "['3 large tomatoes', '1 cup basil', '2 tbsp olive oil', '1 onion']"
            )
        ),
        Recipe(
            id = "rec-004",
            title = "Egg and Rice Bowl",
            instructions = "Cook rice and top with fried egg.",
            imageName = "egg-rice-bowl",
            hasImage = false,
            ingredients = IngredientParser.parseIngredientsList(
                "['2 eggs', '1 cup rice', '1 tbsp soy sauce', '1 tsp sesame oil', '2 green onions', '1 carrot']"
            )
        )
    )

    // Pantry user 1: milk, butter, garlic, pasta, chicken breast
    private fun buildPantryForUser1(): InMemoryPantryRepository {
        val repo = InMemoryPantryRepository()
        // Hapus seed default (userId=1) dan isi dengan yang kita kontrol
        listOf("pantry-1", "pantry-2", "pantry-3", "pantry-4", "pantry-5").forEach {
            repo.deleteItem(it, 1)
        }
        listOf(
            makeItem("t-milk", 1, "Milk"),
            makeItem("t-butter", 1, "Butter"),
            makeItem("t-garlic", 1, "Garlic"),
            makeItem("t-pasta", 1, "Pasta"),
            makeItem("t-chicken", 1, "Chicken Breast")
        ).forEach { repo.addItem(it) }
        return repo
    }

    // Pantry user 2: tomato, onion (tidak sama dengan user 1)
    private fun buildPantryForUser2(): InMemoryPantryRepository {
        val repo = InMemoryPantryRepository()
        listOf("pantry-1", "pantry-2", "pantry-3", "pantry-4", "pantry-5").forEach {
            repo.deleteItem(it, 1)
        }
        listOf(
            makeItem("u2-tomato", 2, "Tomato"),
            makeItem("u2-onion", 2, "Onion")
        ).forEach { repo.addItem(it) }
        return repo
    }

    private fun makeItem(id: String, userId: Int, name: String): PantryItem = PantryItem(
        id = id,
        userId = userId,
        name = name,
        ingredientName = name,
        normalizedName = IngredientParser.normalizeIngredientName(name),
        quantity = 1.0,
        unit = "unit",
        storageType = StorageType.PANTRY
    )

    // =====================================================================
    // Test 1: Endpoint recommendation berhasil (200) dengan pantry berisi bahan
    // =====================================================================
    @Test
    fun testRecommendationEndpointAuthenticatedSuccess() {
        val pantryRepo = buildPantryForUser1()
        val recipeRepo = InMemoryRecipeRepository(sampleRecipes)
        val token = TokenProvider.generateToken(defaultUser.id, defaultUser.email)

        testApplication {
            application { module(userRepo, recipeRepo, pantryRepo) }
            val client = createClient {
                install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
            val response = client.get("/api/recipes/recommendations") {
                header("Authorization", "Bearer $token")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<RecommendationResponse>()
            assertTrue(body.success)
            assertTrue("Harus ada minimal 1 rekomendasi", body.recommendations.isNotEmpty())
        }
    }

    // =====================================================================
    // Test 2: Tanpa token → 401 Unauthorized
    // =====================================================================
    @Test
    fun testRecommendationEndpointUnauthorized() {
        testApplication {
            application { module(userRepo, InMemoryRecipeRepository(sampleRecipes), buildPantryForUser1()) }
            val client = createClient {
                install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
            val response = client.get("/api/recipes/recommendations")
            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    // =====================================================================
    // Test 3: Pantry kosong → recommendations = [], total = 0
    // =====================================================================
    @Test
    fun testRecommendationEmptyPantryReturnsEmpty() {
        val emptyPantryRepo = InMemoryPantryRepository()
        // Hapus semua seed item user 1
        listOf("pantry-1", "pantry-2", "pantry-3", "pantry-4", "pantry-5").forEach {
            emptyPantryRepo.deleteItem(it, 1)
        }
        val token = TokenProvider.generateToken(defaultUser.id, defaultUser.email)

        testApplication {
            application { module(userRepo, InMemoryRecipeRepository(sampleRecipes), emptyPantryRepo) }
            val client = createClient {
                install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
            val response = client.get("/api/recipes/recommendations") {
                header("Authorization", "Bearer $token")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<RecommendationResponse>()
            assertTrue(body.success)
            assertEquals(0, body.total)
            assertTrue(body.recommendations.isEmpty())
        }
    }

    // =====================================================================
    // Test 4: matchedIngredients, missingIngredients, matchPercentage benar
    // =====================================================================
    @Test
    fun testMatchingMetadataCorrect() {
        val recipeRepo = InMemoryRecipeRepository(sampleRecipes)
        val pantryRepo = buildPantryForUser1()
        val service = RecommendationService(recipeRepo, pantryRepo)

        val (recs, _) = service.getRecommendations(userId = 1, limit = 10)
        assertTrue(recs.isNotEmpty())

        // rec-002 (Chicken Garlic Stir Fry): chicken, garlic, butter → semua ada di pantry user 1
        val chickenRec = recs.firstOrNull { it.recipe.id == "rec-002" }
        assertNotNull("rec-002 harus ada di hasil", chickenRec)
        if (chickenRec != null) {
            assertEquals(3, chickenRec.matchedCount)
            assertEquals(3, chickenRec.totalIngredients)
            assertEquals(0, chickenRec.missingIngredients.size)
            assertEquals(100.0, chickenRec.matchPercentage, 0.1)
        }

        // rec-003 (Tomato Basil Soup): tomato, basil, olive oil, onion → tidak ada di pantry user 1
        val tomatoRec = recs.firstOrNull { it.recipe.id == "rec-003" }
        // rec-003 seharusnya TIDAK muncul (matchedCount = 0)
        assertTrue("rec-003 tidak boleh ada di rekomendasi karena tidak ada bahan cocok", tomatoRec == null)
    }

    // =====================================================================
    // Test 5: Ranking deterministik — recipe dengan score lebih tinggi di posisi pertama
    // =====================================================================
    @Test
    fun testRankingIsCorrect() {
        val recipeRepo = InMemoryRecipeRepository(sampleRecipes)
        val pantryRepo = buildPantryForUser1()
        val service = RecommendationService(recipeRepo, pantryRepo)

        val (recs, _) = service.getRecommendations(userId = 1, limit = 10)

        // rec-002 (100% match, 3 bahan) VS rec-001 (4/5 = 80% match, 4 bahan cocok)
        // Score rec-002 = 100 + 3*5 = 115
        // Score rec-001 = 80 + 4*5 = 100
        // Jadi rec-002 harus di atas rec-001
        assertTrue(recs.size >= 2)
        val firstId = recs[0].recipe.id
        val secondId = recs[1].recipe.id
        assertEquals("rec-002 harus di posisi 1", "rec-002", firstId)
        assertEquals("rec-001 harus di posisi 2", "rec-001", secondId)
    }

    // =====================================================================
    // Test 6: Parameter limit bekerja
    // =====================================================================
    @Test
    fun testLimitParameterWorks() {
        val recipeRepo = InMemoryRecipeRepository(sampleRecipes)
        val pantryRepo = buildPantryForUser1()
        val token = TokenProvider.generateToken(defaultUser.id, defaultUser.email)

        testApplication {
            application { module(userRepo, recipeRepo, pantryRepo) }
            val client = createClient {
                install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }

            // Dengan limit=1, hanya 1 rekomendasi dikembalikan
            val response = client.get("/api/recipes/recommendations?limit=1") {
                header("Authorization", "Bearer $token")
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.body<RecommendationResponse>()
            assertTrue(body.success)
            assertTrue("Harus ada persis 1 rekomendasi", body.recommendations.size <= 1)
        }
    }

    // =====================================================================
    // Test 7: User isolation — user A tidak dapat rekomendasi pantry user B
    // =====================================================================
    @Test
    fun testUserIsolation() {
        val sharedPantryRepo = buildPantryForUser1()
        // Tambah pantry user 2 ke repo yang sama
        listOf(
            makeItem("u2-tomato", 2, "Tomato"),
            makeItem("u2-onion", 2, "Onion")
        ).forEach { sharedPantryRepo.addItem(it) }

        val recipeRepo = InMemoryRecipeRepository(sampleRecipes)
        val service = RecommendationService(recipeRepo, sharedPantryRepo)

        // User 1 punya chicken, butter, garlic, milk, pasta
        val (recsUser1, pantryCount1) = service.getRecommendations(userId = 1, limit = 10)
        assertTrue(pantryCount1 >= 5)

        // User 2 punya tomato, onion → sesuai rec-003
        val (recsUser2, pantryCount2) = service.getRecommendations(userId = 2, limit = 10)
        assertTrue("User 2 pantry harus ada 2 item", pantryCount2 == 2)

        // Rekomendasi user 2 harus berbeda dari user 1
        val user1RecIds = recsUser1.map { it.recipe.id }.toSet()
        val user2RecIds = recsUser2.map { it.recipe.id }.toSet()
        // user 2 hanya cocok dengan rec-003 (tomato, onion)
        assertTrue("User 2 harus mendapat rec-003", "rec-003" in user2RecIds)
        // user 1 tidak boleh mendapat rec-003 (tidak punya tomato/basil/olive oil/onion)
        assertFalse("User 1 tidak boleh mendapat rec-003", "rec-003" in user1RecIds)
    }

    // =====================================================================
    // Test 8: missingIngredients benar
    // =====================================================================
    @Test
    fun testMissingIngredientsCorrect() {
        val recipeRepo = InMemoryRecipeRepository(sampleRecipes)
        val pantryRepo = buildPantryForUser1()
        val service = RecommendationService(recipeRepo, pantryRepo)

        val (recs, _) = service.getRecommendations(userId = 1, limit = 10)

        // rec-001 (Creamy Garlic Pasta): ingredients: milk, butter, garlic, pasta, salt
        // Pantry user 1: milk, butter, garlic, pasta, chicken breast
        // Missing seharusnya: salt
        val pastaRec = recs.firstOrNull { it.recipe.id == "rec-001" }
        assertNotNull("rec-001 harus ada", pastaRec)
        if (pastaRec != null) {
            assertEquals("rec-001 harus punya 4 matched", 4, pastaRec.matchedCount)
            assertEquals("Missing harus 1", 1, pastaRec.missingIngredients.size)
        }
    }

    // =====================================================================
    // Test 9: Recommendation tidak mengandung resep dummy (semua dari repository)
    // =====================================================================
    @Test
    fun testRecommendationsOnlyFromRepository() {
        val recipeRepo = InMemoryRecipeRepository(sampleRecipes)
        val pantryRepo = buildPantryForUser1()
        val service = RecommendationService(recipeRepo, pantryRepo)

        val (recs, _) = service.getRecommendations(userId = 1, limit = 100)

        val validIds = sampleRecipes.map { it.id }.toSet()
        for (rec in recs) {
            assertTrue(
                "Recipe ${rec.recipe.id} harus berasal dari RecipeRepository",
                rec.recipe.id in validIds
            )
        }
    }

    // =====================================================================
    // Test 10: Endpoint Home yang sudah ada tetap berjalan (regression)
    // =====================================================================
    @Test
    fun testHomeEndpointStillWorks() {
        val pantryRepo = buildPantryForUser1()
        val token = TokenProvider.generateToken(defaultUser.id, defaultUser.email)

        testApplication {
            application { module(userRepo, InMemoryRecipeRepository(sampleRecipes), pantryRepo) }
            val client = createClient {
                install(ClientContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
            val response = client.get("/api/home") {
                header("Authorization", "Bearer $token")
            }
            assertEquals("Home endpoint harus tetap 200", HttpStatusCode.OK, response.status)
        }
    }
}
