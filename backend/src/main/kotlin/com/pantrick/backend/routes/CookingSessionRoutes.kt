package com.pantrick.backend.routes

import com.pantrick.backend.models.CookingReadinessResponse
import com.pantrick.backend.models.CookingSessionResponse
import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.service.CookingSessionService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.cookingSessionRoutes(cookingSessionService: CookingSessionService) {

    // ────────────────────────────────────────────────────────────────────────
    // RECIPE COOKING ACTIONS  (dibawah /api/recipes untuk konsistensi)
    // ────────────────────────────────────────────────────────────────────────
    route("/api/recipes/{recipeId}") {

        // GET /api/recipes/{recipeId}/cook/readiness — cek apakah bahan cukup
        get("/cook/readiness") {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid")
            )
            val recipeId = call.parameters["recipeId"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID resep tidak valid")
            )

            val result = cookingSessionService.checkReadiness(userId, recipeId)
            call.respond(HttpStatusCode.OK, result)
        }

        // POST /api/recipes/{recipeId}/cook — mulai memasak
        post("/cook") {
            val userId = call.getAuthenticatedUserId() ?: return@post call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid")
            )
            val recipeId = call.parameters["recipeId"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID resep tidak valid")
            )

            try {
                val session = cookingSessionService.startCooking(userId, recipeId)
                call.respond(
                    HttpStatusCode.Created,
                    CookingSessionResponse(
                        success = true,
                        message = "Sesi memasak dimulai. Pantry telah diperbarui.",
                        data = session
                    )
                )
            } catch (e: IllegalStateException) {
                // Sesi sudah ada (double-tap) atau bahan tidak cukup
                val status = if (e.message?.contains("aktif") == true)
                    HttpStatusCode.Conflict else HttpStatusCode.UnprocessableEntity
                call.respond(status, ErrorResponse(success = false, message = e.message ?: "Gagal memulai memasak"))
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = e.message ?: "Resep tidak ditemukan")
                )
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // COOKING SESSION MANAGEMENT
    // ────────────────────────────────────────────────────────────────────────
    route("/api/cooking-sessions") {

        // GET /api/cooking-sessions/active?recipeId={id}
        get("/active") {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid")
            )
            val recipeId = call.request.queryParameters["recipeId"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "Parameter recipeId diperlukan")
            )

            val session = cookingSessionService.getActiveSession(userId, recipeId)
            call.respond(
                HttpStatusCode.OK,
                CookingSessionResponse(
                    success = true,
                    message = if (session != null) "Sesi aktif ditemukan" else "Tidak ada sesi aktif",
                    data = session
                )
            )
        }

        // GET /api/cooking-sessions/{id}
        get("/{id}") {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid")
            )
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID sesi tidak valid")
            )

            val session = cookingSessionService.getSession(userId, id)
            if (session == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Sesi memasak tidak ditemukan")
                )
            } else {
                call.respond(HttpStatusCode.OK, CookingSessionResponse(success = true, message = "OK", data = session))
            }
        }

        // POST /api/cooking-sessions/{id}/cancel
        post("/{id}/cancel") {
            val userId = call.getAuthenticatedUserId() ?: return@post call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid")
            )
            val id = call.parameters["id"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID sesi tidak valid")
            )

            try {
                val session = cookingSessionService.cancelCooking(userId, id)
                call.respond(
                    HttpStatusCode.OK,
                    CookingSessionResponse(
                        success = true,
                        message = "Sesi memasak dibatalkan. Bahan pantry dikembalikan.",
                        data = session
                    )
                )
            } catch (e: IllegalStateException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse(success = false, message = e.message ?: "Tidak dapat membatalkan sesi")
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = e.message ?: "Sesi tidak ditemukan")
                )
            } catch (e: SecurityException) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    ErrorResponse(success = false, message = "Akses ditolak")
                )
            }
        }

        // POST /api/cooking-sessions/{id}/complete
        post("/{id}/complete") {
            val userId = call.getAuthenticatedUserId() ?: return@post call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid")
            )
            val id = call.parameters["id"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID sesi tidak valid")
            )

            try {
                val session = cookingSessionService.completeCooking(userId, id)
                call.respond(
                    HttpStatusCode.OK,
                    CookingSessionResponse(
                        success = true,
                        message = "Selamat! Masakan selesai.",
                        data = session
                    )
                )
            } catch (e: IllegalStateException) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse(success = false, message = e.message ?: "Tidak dapat menyelesaikan sesi")
                )
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = e.message ?: "Sesi tidak ditemukan")
                )
            } catch (e: SecurityException) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    ErrorResponse(success = false, message = "Akses ditolak")
                )
            }
        }
    }
}
