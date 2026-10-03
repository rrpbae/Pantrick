package com.pantrick.backend

import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.repository.InMemoryCookingHistoryRepository
import com.pantrick.backend.repository.InMemoryPantryRepository
import com.pantrick.backend.repository.InMemoryPasswordResetTokenRepository
import com.pantrick.backend.repository.InMemoryRecipeRepository
import com.pantrick.backend.repository.InMemorySavedRecipeRepository
import com.pantrick.backend.repository.InMemoryShoppingListRepository
import com.pantrick.backend.repository.InMemoryUserRepository
import com.pantrick.backend.repository.PantryRepository
import com.pantrick.backend.repository.RecipeRepository
import com.pantrick.backend.repository.SavedRecipeRepository
import com.pantrick.backend.repository.UserRepository
import com.pantrick.backend.routes.authRoutes
import com.pantrick.backend.routes.homeRoutes
import com.pantrick.backend.routes.legalRoutes
import com.pantrick.backend.routes.pantryRoutes
import com.pantrick.backend.routes.recipeRoutes
import com.pantrick.backend.routes.recommendationRoutes
import com.pantrick.backend.routes.savedRecipeRoutes
import com.pantrick.backend.service.HomeService
import com.pantrick.backend.service.PantryService
import com.pantrick.backend.service.PasswordResetService
import com.pantrick.backend.service.RecipeDatasetLoader
import com.pantrick.backend.service.RecipeService
import com.pantrick.backend.service.RecommendationService
import com.pantrick.backend.service.SavedRecipeService
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

private val appLogger = LoggerFactory.getLogger("com.pantrick.backend.Application")

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8081
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(
    userRepository: UserRepository = InMemoryUserRepository(),
    recipeRepository: RecipeRepository? = null,
    pantryRepository: PantryRepository = InMemoryPantryRepository(),
    savedRecipeRepository: SavedRecipeRepository = InMemorySavedRecipeRepository(),
    passwordResetService: PasswordResetService? = null
) {
    val actualRecipeRepo = recipeRepository ?: try {
        appLogger.info("Initializing Recipe Repository from dataset...")
        val datasetResult = RecipeDatasetLoader.loadDataset()
        InMemoryRecipeRepository(datasetResult.recipes)
    } catch (e: Exception) {
        appLogger.warn("Dataset loading failed, falling back to empty RecipeRepository: ${e.message}")
        InMemoryRecipeRepository(emptyList())
    }

    val cookingHistoryRepo = InMemoryCookingHistoryRepository()
    val shoppingListRepo = InMemoryShoppingListRepository()
    val passwordResetTokenRepository = InMemoryPasswordResetTokenRepository()
    val actualPasswordResetService = passwordResetService ?: PasswordResetService(userRepository, passwordResetTokenRepository)

    val recipeService = RecipeService(actualRecipeRepo)
    val pantryService = PantryService(pantryRepository)
    val homeService = HomeService(userRepository, pantryRepository, actualRecipeRepo)
    val recommendationService = RecommendationService(actualRecipeRepo, pantryRepository, savedRecipeRepository)
    val savedRecipeService = SavedRecipeService(
        savedRecipeRepository = savedRecipeRepository,
        recipeRepository = actualRecipeRepo,
        cookingHistoryRepository = cookingHistoryRepo,
        shoppingListRepository = shoppingListRepo,
        pantryRepository = pantryRepository
    )

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            appLogger.error("Unhandled exception in route handler", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(
                    success = false,
                    message = "Terjadi kesalahan internal pada server"
                )
            )
        }
    }

    routing {
        authRoutes(userRepository, actualPasswordResetService)
        legalRoutes()
        // Recommendation & Saved Recipe routes HARUS didaftarkan SEBELUM recipeRoutes
        // agar rute spesifik tidak tertangkap oleh /{id}
        recommendationRoutes(recommendationService)
        savedRecipeRoutes(savedRecipeService)
        recipeRoutes(recipeService)
        homeRoutes(homeService)
        pantryRoutes(pantryService)
    }
}
