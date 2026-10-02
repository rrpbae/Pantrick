package com.pantrick.backend

import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.repository.InMemoryPantryRepository
import com.pantrick.backend.repository.InMemoryRecipeRepository
import com.pantrick.backend.repository.InMemoryUserRepository
import com.pantrick.backend.repository.PantryRepository
import com.pantrick.backend.repository.RecipeRepository
import com.pantrick.backend.repository.UserRepository
import com.pantrick.backend.routes.authRoutes
import com.pantrick.backend.routes.homeRoutes
import com.pantrick.backend.routes.recipeRoutes
import com.pantrick.backend.service.HomeService
import com.pantrick.backend.service.RecipeDatasetLoader
import com.pantrick.backend.service.RecipeService
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

import com.pantrick.backend.routes.pantryRoutes
import com.pantrick.backend.service.PantryService

private val appLogger = LoggerFactory.getLogger("com.pantrick.backend.Application")

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8081
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module(
    userRepository: UserRepository = InMemoryUserRepository(),
    recipeRepository: RecipeRepository? = null,
    pantryRepository: PantryRepository = InMemoryPantryRepository()
) {
    val actualRecipeRepo = recipeRepository ?: run {
        appLogger.info("Initializing Recipe Repository from dataset...")
        val datasetResult = RecipeDatasetLoader.loadDataset()
        InMemoryRecipeRepository(datasetResult.recipes)
    }

    val recipeService = RecipeService(actualRecipeRepo)
    val pantryService = PantryService(pantryRepository)
    val homeService = HomeService(userRepository, pantryRepository, actualRecipeRepo)

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
        authRoutes(userRepository)
        recipeRoutes(recipeService)
        homeRoutes(homeService)
        pantryRoutes(pantryService)
    }
}
