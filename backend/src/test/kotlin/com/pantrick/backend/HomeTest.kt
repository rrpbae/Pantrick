package com.pantrick.backend

import com.pantrick.backend.models.ExpirationPriority
import com.pantrick.backend.models.ExpirationStatus
import com.pantrick.backend.models.HomeResponse
import com.pantrick.backend.models.PantryItem
import com.pantrick.backend.models.Recipe
import com.pantrick.backend.repository.InMemoryPantryRepository
import com.pantrick.backend.repository.InMemoryRecipeRepository
import com.pantrick.backend.repository.InMemoryUserRepository
import com.pantrick.backend.security.TokenProvider
import com.pantrick.backend.service.HomeService
import com.pantrick.backend.service.IngredientParser
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class HomeTest {

    private val userRepo = InMemoryUserRepository()
    private val defaultUser = userRepo.findByEmail("user@example.com")!!

    private val sampleRecipes = listOf(
        Recipe(
            id = "0",
            title = "Creamy Garlic Herb Pasta",
            instructions = "Boil pasta and mix with cream sauce.",
            imageName = "creamy-garlic-pasta",
            hasImage = true,
            ingredients = IngredientParser.parseIngredientsList("['1 cup milk', '2 tbsp butter', '2 cloves garlic', '200g pasta']")
        ),
        Recipe(
            id = "1",
            title = "Chicken Garlic Stir Fry",
            instructions = "Stir fry chicken with garlic.",
            imageName = "chicken-garlic-stir-fry",
            hasImage = true,
            ingredients = IngredientParser.parseIngredientsList("['500g chicken breast', '3 cloves garlic', '2 tbsp butter']")
        )
    )

    private val recipeRepo = InMemoryRecipeRepository(sampleRecipes)

    @Test
    fun testExpiringItemsDetectionAndPrioritization() {
        val pantryRepo = InMemoryPantryRepository()
        val homeService = HomeService(userRepo, pantryRepo, recipeRepo)

        // Seed items: milk expires in 2 days, garlic expires today (0 days)
        val nowDate = LocalDate.of(2026, 10, 1)
        val userItems = pantryRepo.getItemsByUserId(defaultUser.id)

        val expiringItems = homeService.calculateExpiringItems(userItems, nowDate)

        assertTrue(expiringItems.isNotEmpty())
        // Garlic (0 days left) should be first priority
        assertEquals("Fresh Garlic", expiringItems.first().ingredientName)
        assertEquals(0L, expiringItems.first().daysRemaining)
        assertEquals(ExpirationStatus.EXPIRING_SOON, expiringItems.first().status)
        assertEquals(ExpirationPriority.HIGH, expiringItems.first().priority)
    }

    @Test
    fun testRecipePairingFindsMatchesAndMissingIngredients() {
        val pantryRepo = InMemoryPantryRepository()
        val homeService = HomeService(userRepo, pantryRepo, recipeRepo)

        val userItems = pantryRepo.getItemsByUserId(defaultUser.id)
        val expiringItems = homeService.calculateExpiringItems(userItems, LocalDate.of(2026, 10, 1))

        val pairings = homeService.generateRecipePairings(userItems, expiringItems, limit = 5)

        assertTrue("Should return recipe pairings", pairings.isNotEmpty())
        val topPairing = pairings.first()
        assertNotNull(topPairing.recipeId)
        assertTrue(topPairing.matchedIngredientsCount > 0)
        assertTrue(topPairing.matchPercentage > 0)
    }

    @Test
    fun testEmptyPantryReturnsZeroCountsAndEmptyLists() {
        val emptyPantryRepo = InMemoryPantryRepository()
        // Clear all items
        val allItems = emptyPantryRepo.getItemsByUserId(defaultUser.id)
        allItems.forEach { emptyPantryRepo.deleteItem(it.id, defaultUser.id) }

        val homeService = HomeService(userRepo, emptyPantryRepo, recipeRepo)
        val homeData = homeService.getHomeData(defaultUser.id, nowDate = LocalDate.of(2026, 10, 1))

        assertNotNull(homeData)
        assertEquals(0, homeData?.pantrySummary?.totalItems)
        assertEquals(0, homeData?.pantrySummary?.expiringSoonItems)
        assertTrue(homeData?.needsAttention!!.isEmpty())
        assertTrue(homeData.recipePairings.isEmpty())
    }

    @Test
    fun testHomeEndpointAuthenticatedSuccess() = testApplication {
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
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val validToken = TokenProvider.generateToken(defaultUser.id, defaultUser.email)

        val response = client.get("/api/home") {
            header("Authorization", "Bearer $validToken")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<HomeResponse>()
        assertTrue(body.success)
        assertEquals("Berhasil mendapatkan data home", body.message)
        assertEquals(defaultUser.id, body.data.user.id)
        assertEquals(defaultUser.name, body.data.user.name)
        assertTrue(body.data.pantrySummary.totalItems > 0)
    }

    @Test
    fun testHomeEndpointUnauthorized() = testApplication {
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
                json(Json { ignoreUnknownKeys = true })
            }
        }

        // Request without token
        val noTokenResp = client.get("/api/home")
        assertEquals(HttpStatusCode.Unauthorized, noTokenResp.status)

        // Request with invalid token
        val invalidTokenResp = client.get("/api/home") {
            header("Authorization", "Bearer invalid.jwt.token")
        }
        assertEquals(HttpStatusCode.Unauthorized, invalidTokenResp.status)
    }

    @Test
    fun testUserIsolationUserACannotAccessUserBPantry() = testApplication {
        val pantryRepo = InMemoryPantryRepository()

        // Create User B
        val userB = userRepo.createUser("User B", "userb@example.com", "hash")

        val homeService = HomeService(userRepo, pantryRepo, recipeRepo)

        val homeDataA = homeService.getHomeData(defaultUser.id, nowDate = LocalDate.of(2026, 10, 1))
        val homeDataB = homeService.getHomeData(userB.id, nowDate = LocalDate.of(2026, 10, 1))

        assertNotNull(homeDataA)
        assertNotNull(homeDataB)
        assertTrue(homeDataA!!.pantrySummary.totalItems > 0)
        assertEquals(0, homeDataB!!.pantrySummary.totalItems)
    }
}
