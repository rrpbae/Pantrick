package com.pantrick.backend.routes

import com.pantrick.backend.models.CreatePantryItemRequest
import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.models.PantryCategoriesResponse
import com.pantrick.backend.models.PantryDeleteResponse
import com.pantrick.backend.models.PantryItemListResponse
import com.pantrick.backend.models.PantryItemSingleResponse
import com.pantrick.backend.models.PantrySummaryResponse
import com.pantrick.backend.models.UpdatePantryItemRequest
import com.pantrick.backend.service.PantryService
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import java.io.ByteArrayOutputStream
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

fun Route.pantryRoutes(pantryService: PantryService) {
    route("/api/pantry") {

        // GET /api/pantry/summary
        get("/summary") {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val summaryData = pantryService.getPantrySummary(userId)
            call.respond(
                HttpStatusCode.OK,
                PantrySummaryResponse(
                    success = true,
                    message = "Berhasil mendapatkan ringkasan pantry",
                    data = summaryData
                )
            )
        }

        // GET /api/pantry/categories
        get("/categories") {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val categories = pantryService.getPantryCategories(userId)
            call.respond(
                HttpStatusCode.OK,
                PantryCategoriesResponse(
                    success = true,
                    message = "Berhasil mendapatkan daftar kategori pantry",
                    data = categories
                )
            )
        }

        // GET /api/pantry/attention
        get("/attention") {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val attentionItems = pantryService.getNeedsAttentionItems(userId)
            call.respond(
                HttpStatusCode.OK,
                PantryItemListResponse(
                    success = true,
                    message = "Berhasil mendapatkan item yang membutuhkan perhatian",
                    total = attentionItems.size,
                    data = attentionItems
                )
            )
        }

        // CRUD Item Routes under /api/pantry/items
        route("/items") {

            // GET /api/pantry/items
            get {
                val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
                )

                val storageType = call.request.queryParameters["storageType"]
                val category = call.request.queryParameters["category"]
                val search = call.request.queryParameters["search"]
                val status = call.request.queryParameters["status"]

                val items = pantryService.getPantryItems(
                    userId = userId,
                    storageTypeFilter = storageType,
                    categoryFilter = category,
                    searchQuery = search,
                    statusFilter = status
                )

                call.respond(
                    HttpStatusCode.OK,
                    PantryItemListResponse(
                        success = true,
                        message = "Berhasil mendapatkan daftar item pantry",
                        total = items.size,
                        data = items
                    )
                )
            }

            // POST /api/pantry/items
            post {
                println("[PANTRY ROUTE] POST /api/pantry/items called")
                val userId = call.getAuthenticatedUserId() ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
                )

                println("[PANTRY ROUTE] User authenticated: userId=$userId")
                
                val request = runCatching { call.receive<CreatePantryItemRequest>() }.getOrNull()
                if (request == null) {
                    println("[PANTRY ROUTE] Invalid request body")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(success = false, message = "Body request tidak valid")
                    )
                    return@post
                }

                println("[PANTRY ROUTE] Request received: name=${request.name}, expirationDate=${request.expirationDate}")

                try {
                    val createdItem = pantryService.createPantryItem(userId, request)
                    println("[PANTRY ROUTE] Item created: id=${createdItem.id}, name=${createdItem.name}, expirationDate=${createdItem.expirationDate}")
                    call.respond(
                        HttpStatusCode.Created,
                        PantryItemSingleResponse(
                            success = true,
                            message = "Berhasil menambahkan item ke pantry",
                            data = createdItem
                        )
                    )
                } catch (e: IllegalArgumentException) {
                    println("[PANTRY ROUTE] Validation error: ${e.message}")
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(success = false, message = e.message ?: "Validasi data gagal")
                    )
                }
            }

            // GET /api/pantry/items/{id}
            get("/{id}") {
                val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
                )

                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(success = false, message = "ID item tidak valid")
                )

                val item = pantryService.getPantryItemById(id, userId)
                if (item == null) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse(success = false, message = "Item pantry tidak ditemukan")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        PantryItemSingleResponse(
                            success = true,
                            message = "Berhasil mendapatkan detail item pantry",
                            data = item
                        )
                    )
                }
            }

            // PUT /api/pantry/items/{id}
            put("/{id}") {
                val userId = call.getAuthenticatedUserId() ?: return@put call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
                )

                val id = call.parameters["id"] ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(success = false, message = "ID item tidak valid")
                )

                val request = runCatching { call.receive<UpdatePantryItemRequest>() }.getOrNull()
                if (request == null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(success = false, message = "Body request tidak valid")
                    )
                    return@put
                }

                try {
                    val updatedItem = pantryService.updatePantryItem(id, userId, request)
                    if (updatedItem == null) {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse(success = false, message = "Item pantry tidak ditemukan atau tidak memiliki akses")
                        )
                    } else {
                        call.respond(
                            HttpStatusCode.OK,
                            PantryItemSingleResponse(
                                success = true,
                                message = "Berhasil memperbarui item pantry",
                                data = updatedItem
                            )
                        )
                    }
                } catch (e: IllegalArgumentException) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(success = false, message = e.message ?: "Validasi data gagal")
                    )
                }
            }

            // DELETE /api/pantry/items/{id}
            delete("/{id}") {
                val userId = call.getAuthenticatedUserId() ?: return@delete call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
                )

                val id = call.parameters["id"] ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(success = false, message = "ID item tidak valid")
                )

                val deleted = pantryService.deletePantryItem(id, userId)
                if (!deleted) {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse(success = false, message = "Item pantry tidak ditemukan atau tidak memiliki akses")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.OK,
                        PantryDeleteResponse(
                            success = true,
                            message = "Berhasil menghapus item pantry"
                        )
                    )
                }
            }

            // GET /api/pantry/items/{id}/image - Serves item image or generates dynamic placeholder badge
            get("/{id}/image") {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(success = false, message = "ID item tidak valid")
                )

                val item = pantryService.getPantryItemById(id, userId = call.getAuthenticatedUserId() ?: 0)
                val displayName = item?.name ?: id

                val svgString = generateSvgPlaceholderImage(displayName)
                call.respondBytes(svgString.toByteArray(Charsets.UTF_8), ContentType.Image.SVG, HttpStatusCode.OK)
            }
        }
    }
}

private fun generateSvgPlaceholderImage(name: String): String {
    val initial = name.trim().take(1).uppercase().ifBlank { "P" }
    return """
        <svg xmlns="http://www.w3.org/2000/svg" width="200" height="200" viewBox="0 0 200 200">
            <rect width="200" height="200" rx="32" fill="#3B82F6"/>
            <text x="50%" y="55%" dominant-baseline="middle" text-anchor="middle" fill="#FFFFFF" font-family="sans-serif" font-size="72" font-weight="bold">$initial</text>
        </svg>
    """.trimIndent()
}
