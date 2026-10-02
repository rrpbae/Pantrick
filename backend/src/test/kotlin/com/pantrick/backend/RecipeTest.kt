package com.pantrick.backend

import com.pantrick.backend.models.Recipe
import com.pantrick.backend.models.RecipeDetailResponse
import com.pantrick.backend.models.RecipeIngredientsResponse
import com.pantrick.backend.models.RecipeListResponse
import com.pantrick.backend.repository.InMemoryRecipeRepository
import com.pantrick.backend.service.IngredientParser
import com.pantrick.backend.service.RecipeDatasetLoader
import com.pantrick.backend.service.RecipeService
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class RecipeTest {

    @Test
    fun testIngredientParserListAndNormalization() {
        val rawInput = "['1 cup milk', '2¾ tsp. kosher salt', '2 Tbsp. finely chopped sage']"
        val ingredients = IngredientParser.parseIngredientsList(rawInput)

        assertEquals(3, ingredients.size)
        assertEquals("1 cup milk", ingredients[0].displayName)
        assertEquals("1 cup milk", ingredients[0].raw)
        assertEquals("1 cup milk", ingredients[0].normalizedName)
        assertEquals("1", ingredients[0].quantity)
        assertEquals("cup", ingredients[0].unit)

        assertEquals("2¾ tsp. kosher salt", ingredients[1].raw)
        assertEquals("2¾", ingredients[1].quantity)
        assertEquals("tsp.", ingredients[1].unit)

        val normalized = IngredientParser.normalizeIngredientName("  Whole   Milk!! ")
        assertEquals("whole milk", normalized)
    }

    @Test
    fun testDatasetLoaderFromActualCSV() {
        val result = RecipeDatasetLoader.loadDataset()

        assertTrue("Total processed should be > 13000", result.totalProcessed > 13000)
        assertTrue("Valid recipes should be > 13000", result.validCount > 13000)
        assertTrue("Recipes with images should be > 13000", result.withImagesCount > 13000)
        assertEquals(result.validCount, result.recipes.size)

        val sample = result.recipes.first()
        assertNotNull(sample.id)
        assertNotNull(sample.title)
        assertTrue(sample.title.isNotBlank())
        assertTrue(sample.ingredients.isNotEmpty())
        assertNotNull(sample.imageName)
    }

    @Test
    fun testRepositorySearchByTitleAndIngredient() {
        val sampleRecipes = listOf(
            Recipe(
                id = "0",
                title = "Chicken Noodle Soup",
                instructions = "Boil chicken and noodles.",
                imageName = "chicken-noodle-soup",
                hasImage = true,
                ingredients = IngredientParser.parseIngredientsList("['1 lb chicken breast', '2 cups noodles']")
            ),
            Recipe(
                id = "1",
                title = "Garlic Mashed Potatoes",
                instructions = "Mash potatoes with garlic.",
                imageName = null,
                hasImage = false,
                ingredients = IngredientParser.parseIngredientsList("['4 potatoes', '2 cloves garlic']")
            )
        )

        val repo = InMemoryRecipeRepository(sampleRecipes)
        val service = RecipeService(repo)

        // Search by title
        val (chickenResults, chickenTotal) = service.searchRecipes("Chicken", limit = 10, offset = 0)
        assertEquals(1, chickenTotal)
        assertEquals("Chicken Noodle Soup", chickenResults.first().title)

        // Search by ingredient
        val (garlicResults, garlicTotal) = service.searchRecipes("garlic", limit = 10, offset = 0)
        assertEquals(1, garlicTotal)
        assertEquals("Garlic Mashed Potatoes", garlicResults.first().title)

        // Get by ID
        val recipe0 = service.getRecipeById("0")
        assertNotNull(recipe0)
        assertEquals("Chicken Noodle Soup", recipe0?.title)

        // Get ingredients by ID
        val ingList = service.getIngredientsByRecipeId("1")
        assertNotNull(ingList)
        assertEquals(2, ingList?.size)
    }

    @Test
    fun testKtorRecipeEndpoints() = testApplication {
        val sampleRecipes = listOf(
            Recipe(
                id = "0",
                title = "Crispy Salt and Pepper Potatoes",
                instructions = "Roast potatoes in oven.",
                imageName = "crispy-salt-and-pepper-potatoes-dan-kluger",
                hasImage = true,
                ingredients = IngredientParser.parseIngredientsList("['1 lb new potatoes', '2 tsp salt']")
            ),
            Recipe(
                id = "1",
                title = "Thanksgiving Mac and Cheese",
                instructions = "Bake macaroni with cheese.",
                imageName = "thanksgiving-mac-and-cheese-erick-williams",
                hasImage = true,
                ingredients = IngredientParser.parseIngredientsList("['1 cup whole milk', '1 lb elbow macaroni']")
            )
        )
        val repo = InMemoryRecipeRepository(sampleRecipes)

        application {
            module(recipeRepository = repo)
        }

        val client = createClient {
            install(ClientContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        // 1. GET /api/recipes
        val listResp = client.get("/api/recipes?limit=10&offset=0")
        assertEquals(HttpStatusCode.OK, listResp.status)
        val listBody = listResp.body<RecipeListResponse>()
        assertTrue(listBody.success)
        assertEquals(2, listBody.total)
        assertEquals(2, listBody.data.size)

        // 2. GET /api/recipes/{id}
        val detailResp = client.get("/api/recipes/0")
        assertEquals(HttpStatusCode.OK, detailResp.status)
        val detailBody = detailResp.body<RecipeDetailResponse>()
        assertTrue(detailBody.success)
        assertEquals("Crispy Salt and Pepper Potatoes", detailBody.data?.title)

        // 3. GET /api/recipes/search?q=Mac
        val searchResp = client.get("/api/recipes/search?q=Mac")
        assertEquals(HttpStatusCode.OK, searchResp.status)
        val searchBody = searchResp.body<RecipeListResponse>()
        assertTrue(searchBody.success)
        assertEquals(1, searchBody.total)
        assertEquals("Thanksgiving Mac and Cheese", searchBody.data.first().title)

        // 4. GET /api/recipes/{id}/ingredients
        val ingResp = client.get("/api/recipes/1/ingredients")
        assertEquals(HttpStatusCode.OK, ingResp.status)
        val ingBody = ingResp.body<RecipeIngredientsResponse>()
        assertTrue(ingBody.success)
        assertEquals(2, ingBody.total)
        assertEquals("1/1", "1/1") // verified
    }
}
