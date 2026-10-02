package com.pantrick.backend.routes

import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.models.RecommendationResponse
import com.pantrick.backend.service.RecommendationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

/**
 * Endpoint recommendation — didaftarkan di bawah /api/recipes/recommendations.
 * Endpoint WAJIB menempel di dalam blok route("/api/recipes") agar sesuai
 * struktur routing yang sudah ada di RecipeRoutes.
 *
 * Karena Ktor mencocokkan rute secara urutan, endpoint spesifik (/recommendations)
 * harus didaftarkan SEBELUM /{id} di RecipeRoutes.
 */
fun Route.recommendationRoutes(recommendationService: RecommendationService) {
    route("/api/recipes") {

        /**
         * GET /api/recipes/recommendations?limit=10
         *
         * Headers wajib: Authorization: Bearer <JWT>
         *
         * Response 200:
         * {
         *   "success": true,
         *   "message": "...",
         *   "total": N,
         *   "limit": N,
         *   "pantryItemCount": N,
         *   "recommendations": [ { recipe, matchedIngredients, ... } ]
         * }
         *
         * Response 401: jika token tidak valid / tidak ada
         * Response 200 dengan recommendations = [] jika pantry kosong
         */
        get("/recommendations") {
            val userId = call.getAuthenticatedUserId()
            if (userId == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(
                        success = false,
                        message = "Token autentikasi tidak valid atau belum disediakan"
                    )
                )
                return@get
            }

            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 10

            val (recommendations, pantryItemCount) = recommendationService.getRecommendations(
                userId = userId,
                limit = limit
            )

            val message = when {
                pantryItemCount == 0 -> "Pantry Anda kosong. Tambahkan bahan untuk mendapatkan rekomendasi resep."
                recommendations.isEmpty() -> "Tidak ditemukan resep yang cocok dengan bahan di Pantry Anda."
                else -> "Berhasil mendapatkan ${recommendations.size} rekomendasi resep berdasarkan ${pantryItemCount} bahan di Pantry Anda."
            }

            call.respond(
                HttpStatusCode.OK,
                RecommendationResponse(
                    success = true,
                    message = message,
                    total = recommendations.size,
                    limit = limit,
                    pantryItemCount = pantryItemCount,
                    recommendations = recommendations
                )
            )
        }
    }
}
