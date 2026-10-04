package com.pantrick.backend.routes

import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.models.FilterOptionsResponse
import com.pantrick.backend.models.InstantDinnerResponse
import com.pantrick.backend.models.RecommendationResponse
import com.pantrick.backend.models.IngredientSearchRequest
import com.pantrick.backend.models.IngredientSearchResponse
import com.pantrick.backend.service.RecommendationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

/**
 * Endpoint recommendation resep berbasis pantry & recipe dataset.
 */
fun Route.recommendationRoutes(recommendationService: RecommendationService) {
    route("/api/recipes") {

        // POST /api/recipes/search-by-ingredients (tanpa autentikasi)
        // Body JSON: {"ingredients": ["chicken", "onion", "garlic"]}
        post("/search-by-ingredients") {
            val request = try {
                call.receive<IngredientSearchRequest>()
            } catch (e: Exception) {
                io.ktor.server.application.ApplicationCallPipeline.ApplicationPhase.Plugins // dummy reference or logger
                call.application.environment.log.error("Failed parsing IngredientSearchRequest", e)
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(success = false, message = "Format request tidak valid: ${e.message}")
                )
                return@post
            }

            if (request.ingredients.isEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(success = false, message = "Daftar bahan tidak boleh kosong.")
                )
                return@post
            }

            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

            val recommendations = recommendationService.getRecommendationsByIngredientNames(
                ingredientNames = request.ingredients,
                limit = limit,
                offset = offset
            )

            call.respond(
                HttpStatusCode.OK,
                IngredientSearchResponse(
                    success = true,
                    message = if (recommendations.isEmpty())
                        "Tidak ditemukan resep yang cocok dengan bahan tersebut."
                    else
                        "Ditemukan ${recommendations.size} rekomendasi resep.",
                    total = recommendations.size,
                    limit = limit,
                    offset = offset,
                    recommendations = recommendations
                )
            )
        }

        // GET /api/recipes/recommended/filters
        get("/recommended/filters") {
            call.respond(HttpStatusCode.OK, FilterOptionsResponse())
        }

        // GET /api/recipes/recommended/smart-menu
        get("/recommended/smart-menu") {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val smartMenu = recommendationService.getSmartMenu(userId)
            call.respond(HttpStatusCode.OK, smartMenu)
        }

        // POST /api/recipes/recommended/instant-dinner
        post("/recommended/instant-dinner") {
            val userId = call.getAuthenticatedUserId() ?: return@post call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val dinnerRecipeRec = recommendationService.getInstantDinner(userId)
            if (dinnerRecipeRec == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Tidak ditemukan resep yang cocok untuk Instant Dinner")
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    InstantDinnerResponse(
                        success = true,
                        message = "Berhasil mendapatkan rekomendasi Instant Dinner",
                        data = dinnerRecipeRec
                    )
                )
            }
        }

        // GET /api/recipes/recommendations & GET /api/recipes/recommended
        val handleRecommendationCall: suspend (io.ktor.server.application.ApplicationCall) -> Unit = { call ->
            val userId = call.getAuthenticatedUserId()
            if (userId == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        success = false,
                        message = "Token autentikasi tidak valid atau belum disediakan"
                    )
                )
            } else {
                val search = call.request.queryParameters["search"]
                val filter = call.request.queryParameters["filter"] ?: "all"
                val sort = call.request.queryParameters["sort"] ?: "match"
                val maxTime = call.request.queryParameters["maxTime"]?.toIntOrNull()
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10
                val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

                val (recommendations, pantryItemCount) = recommendationService.getRecommendations(
                    userId = userId,
                    search = search,
                    filter = filter,
                    sort = sort,
                    maxTime = maxTime,
                    limit = limit,
                    offset = offset
                )

                val message = when {
                    pantryItemCount == 0 && search.isNullOrBlank() -> "Pantry Anda kosong. Tambahkan bahan untuk mendapatkan rekomendasi resep."
                    recommendations.isEmpty() -> "Tidak ditemukan resep yang cocok dengan kriteria pencarian di Pantry Anda."
                    else -> "Berhasil mendapatkan ${recommendations.size} rekomendasi resep berdasarkan Pantry Anda."
                }

                call.respond(
                    HttpStatusCode.OK,
                    RecommendationResponse(
                        success = true,
                        message = message,
                        total = recommendations.size,
                        limit = limit,
                        offset = offset,
                        pantryItemCount = pantryItemCount,
                        recommendations = recommendations
                    )
                )
            }
        }

        get("/recommendations") { handleRecommendationCall(call) }
        get("/recommended") { handleRecommendationCall(call) }
    }
}
