package com.pantrick.backend.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.models.HomeResponse
import com.pantrick.backend.service.HomeService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.io.File

private val jwtSecret = System.getenv("JWT_SECRET") ?: "pantrick_jwt_secret_key_dev_mode_2026"
private val jwtIssuer = "pantrick-backend"
private val jwtAudience = "pantrick-android-app"
private val jwtVerifier = JWT.require(Algorithm.HMAC256(jwtSecret))
    .withIssuer(jwtIssuer)
    .withAudience(jwtAudience)
    .build()

fun ApplicationCall.getAuthenticatedUserId(): Int? {
    val authHeader = request.headers["Authorization"] ?: return null
    if (!authHeader.startsWith("Bearer ", ignoreCase = true)) return null

    val token = authHeader.substring(7).trim()
    if (token.isBlank()) return null

    return try {
        val decodedJwt = jwtVerifier.verify(token)
        decodedJwt.subject.toIntOrNull()
    } catch (e: Exception) {
        null
    }
}

fun Route.homeRoutes(homeService: HomeService) {
    route("/api") {

        // GET /api/home
        get("/home") {
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

            val category = call.request.queryParameters["category"]
            val needsAttentionLimit = call.request.queryParameters["attentionLimit"]?.toIntOrNull() ?: 5
            val pairingLimit = call.request.queryParameters["recipeLimit"]?.toIntOrNull() ?: 10

            val homeData = homeService.getHomeData(
                userId = userId,
                categoryFilter = category,
                needsAttentionLimit = needsAttentionLimit,
                recipePairingLimit = pairingLimit
            )

            if (homeData == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Pengguna tidak ditemukan")
                )
                return@get
            }

            call.respond(
                HttpStatusCode.OK,
                HomeResponse(
                    success = true,
                    message = "Berhasil mendapatkan data home",
                    data = homeData
                )
            )
        }

        // GET /api/home/attention
        get("/home/attention") {
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

            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 5
            val homeData = homeService.getHomeData(userId = userId, needsAttentionLimit = limit)

            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "success" to true,
                    "message" to "Berhasil mendapatkan data bahan yang perlu diperhatikan",
                    "data" to (homeData?.needsAttention ?: emptyList())
                )
            )
        }

        // GET /api/home/recipes
        get("/home/recipes") {
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
            val homeData = homeService.getHomeData(userId = userId, recipePairingLimit = limit)

            call.respond(
                HttpStatusCode.OK,
                mapOf(
                    "success" to true,
                    "message" to "Berhasil mendapatkan rekomendasi resep",
                    "data" to (homeData?.recipePairings ?: emptyList())
                )
            )
        }

        // GET /api/recipes/{id}/image - Image serving route for recipes
        get("/recipes/{id}/image") {
            val id = call.parameters["id"]
            if (id.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(success = false, message = "ID resep tidak valid"))
                return@get
            }

            val imagesDir = findImagesDir()
            if (imagesDir == null || !imagesDir.exists()) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(success = false, message = "Folder gambar tidak ditemukan"))
                return@get
            }

            // Look up image file by imageName or id slug
            val targetFile = imagesDir.listFiles()?.firstOrNull { file ->
                file.nameWithoutExtension.equals(id, ignoreCase = true) ||
                        file.name.equals(id, ignoreCase = true) ||
                        file.name.equals("$id.jpg", ignoreCase = true)
            }

            if (targetFile != null && targetFile.exists()) {
                call.respondFile(targetFile)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Gambar resep dengan ID '$id' tidak ditemukan")
                )
            }
        }
    }
}

private fun findImagesDir(): File? {
    val candidates = listOf(
        "Dataset_Resep/Food Images/Food Images",
        "../Dataset_Resep/Food Images/Food Images",
        "../../Dataset_Resep/Food Images/Food Images"
    )
    for (path in candidates) {
        val dir = File(path)
        if (dir.exists() && dir.isDirectory) return dir
    }
    return null
}
