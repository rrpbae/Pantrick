package com.pantrick.backend.routes

import com.pantrick.backend.models.AddMissingIngredientsRequest
import com.pantrick.backend.models.AddShoppingListItemRequest
import com.pantrick.backend.models.CookingHistoryResponse
import com.pantrick.backend.models.CreateCollectionRequest
import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.models.RecipeCollectionListResponse
import com.pantrick.backend.models.RecipeCollectionResponse
import com.pantrick.backend.models.RecipeListResponse
import com.pantrick.backend.models.RecipeSavedStatusResponse
import com.pantrick.backend.models.SavedRecipeListResponse
import com.pantrick.backend.models.ShoppingListResponse
import com.pantrick.backend.service.SavedRecipeService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

/**
 * Endpoints untuk Saved Recipes, Recipe Collections, Cooking Action, dan Shopping List.
 */
fun Route.savedRecipeRoutes(savedRecipeService: SavedRecipeService) {

    // --- SAVED RECIPES & COOKING ---
    route("/api/recipes") {

        // GET /api/recipes/saved
        get("/saved") {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val savedRecipes = savedRecipeService.getSavedRecipes(userId)
            call.respond(
                HttpStatusCode.OK,
                SavedRecipeListResponse(
                    success = true,
                    message = "Berhasil mendapatkan resep tersimpan",
                    total = savedRecipes.size,
                    data = savedRecipes
                )
            )
        }

        // POST /api/recipes/{id}/save
        post("/{id}/save") {
            val userId = call.getAuthenticatedUserId() ?: return@post call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )
            val recipeId = call.parameters["id"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID resep tidak valid")
            )

            val success = savedRecipeService.saveRecipe(userId, recipeId)
            if (success) {
                call.respond(
                    HttpStatusCode.OK,
                    RecipeSavedStatusResponse(
                        success = true,
                        message = "Resep berhasil disimpan",
                        recipeId = recipeId,
                        isSaved = true
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Resep dengan ID '$recipeId' tidak ditemukan di dataset")
                )
            }
        }

        // DELETE /api/recipes/{id}/save
        delete("/{id}/save") {
            val userId = call.getAuthenticatedUserId() ?: return@delete call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )
            val recipeId = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID resep tidak valid")
            )

            val removed = savedRecipeService.unsaveRecipe(userId, recipeId)
            call.respond(
                HttpStatusCode.OK,
                RecipeSavedStatusResponse(
                    success = true,
                    message = if (removed) "Resep berhasil dihapus dari simpanan" else "Resep tidak ditemukan di simpanan",
                    recipeId = recipeId,
                    isSaved = false
                )
            )
        }

        // GET /api/recipes/{id}/saved
        get("/{id}/saved") {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )
            val recipeId = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID resep tidak valid")
            )

            val isSaved = savedRecipeService.isRecipeSaved(userId, recipeId)
            call.respond(
                HttpStatusCode.OK,
                RecipeSavedStatusResponse(
                    success = true,
                    message = if (isSaved) "Resep ini tersimpan" else "Resep ini belum disimpan",
                    recipeId = recipeId,
                    isSaved = isSaved
                )
            )
        }

        // POST /api/recipes/{id}/cook
        post("/{id}/cook") {
            val userId = call.getAuthenticatedUserId() ?: return@post call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )
            val recipeId = call.parameters["id"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID resep tidak valid")
            )

            val record = savedRecipeService.recordCooking(userId, recipeId)
            if (record != null) {
                call.respond(
                    HttpStatusCode.OK,
                    CookingHistoryResponse(
                        success = true,
                        message = "Aktivitas memasak berhasil dicatat",
                        data = record
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Resep dengan ID '$recipeId' tidak ditemukan")
                )
            }
        }
    }

    // --- RECIPE COLLECTIONS ---
    route("/api/recipe-collections") {

        // GET /api/recipe-collections
        get {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val collections = savedRecipeService.getCollections(userId)
            call.respond(
                HttpStatusCode.OK,
                RecipeCollectionListResponse(
                    success = true,
                    message = "Berhasil mendapatkan koleksi resep",
                    total = collections.size,
                    data = collections
                )
            )
        }

        // POST /api/recipe-collections
        post {
            val userId = call.getAuthenticatedUserId() ?: return@post call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )
            val body = runCatching { call.receive<CreateCollectionRequest>() }.getOrNull() ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "Nama koleksi tidak valid")
            )

            if (body.name.isBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(success = false, message = "Nama koleksi tidak boleh kosong")
                )
            }

            val collection = savedRecipeService.createCollection(userId, body.name.trim())
            call.respond(
                HttpStatusCode.Created,
                RecipeCollectionResponse(
                    success = true,
                    message = "Koleksi resep berhasil dibuat",
                    data = collection
                )
            )
        }

        // GET /api/recipe-collections/{id}
        get("/{id}") {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )
            val collectionId = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID koleksi tidak valid")
            )

            val collection = savedRecipeService.getCollectionById(userId, collectionId)
            if (collection == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Koleksi resep tidak ditemukan")
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    RecipeCollectionResponse(
                        success = true,
                        message = "Berhasil mendapatkan detail koleksi",
                        data = collection
                    )
                )
            }
        }

        // PUT /api/recipe-collections/{id}
        put("/{id}") {
            val userId = call.getAuthenticatedUserId() ?: return@put call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )
            val collectionId = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID koleksi tidak valid")
            )
            val body = runCatching { call.receive<CreateCollectionRequest>() }.getOrNull() ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "Nama koleksi tidak valid")
            )

            if (body.name.isBlank()) {
                return@put call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(success = false, message = "Nama koleksi tidak boleh kosong")
                )
            }

            val updated = savedRecipeService.updateCollection(userId, collectionId, body.name.trim())
            if (updated == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Koleksi tidak ditemukan")
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    RecipeCollectionResponse(
                        success = true,
                        message = "Koleksi berhasil diperbarui",
                        data = updated
                    )
                )
            }
        }

        // DELETE /api/recipe-collections/{id}
        delete("/{id}") {
            val userId = call.getAuthenticatedUserId() ?: return@delete call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )
            val collectionId = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID koleksi tidak valid")
            )

            val deleted = savedRecipeService.deleteCollection(userId, collectionId)
            if (deleted) {
                call.respond(
                    HttpStatusCode.OK,
                    ErrorResponse(success = true, message = "Koleksi berhasil dihapus")
                )
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Koleksi tidak ditemukan")
                )
            }
        }

        // POST /api/recipe-collections/{collectionId}/recipes/{recipeId}
        post("/{collectionId}/recipes/{recipeId}") {
            val userId = call.getAuthenticatedUserId() ?: return@post call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )
            val collectionId = call.parameters["collectionId"] ?: return@post call.respond(
                HttpStatusCode.BadRequest, ErrorResponse(success = false, message = "ID koleksi tidak valid")
            )
            val recipeId = call.parameters["recipeId"] ?: return@post call.respond(
                HttpStatusCode.BadRequest, ErrorResponse(success = false, message = "ID resep tidak valid")
            )

            val updated = savedRecipeService.addRecipeToCollection(userId, collectionId, recipeId)
            if (updated == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Koleksi atau Resep tidak ditemukan")
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    RecipeCollectionResponse(
                        success = true,
                        message = "Resep berhasil ditambahkan ke koleksi",
                        data = updated
                    )
                )
            }
        }

        // DELETE /api/recipe-collections/{collectionId}/recipes/{recipeId}
        delete("/{collectionId}/recipes/{recipeId}") {
            val userId = call.getAuthenticatedUserId() ?: return@delete call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )
            val collectionId = call.parameters["collectionId"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest, ErrorResponse(success = false, message = "ID koleksi tidak valid")
            )
            val recipeId = call.parameters["recipeId"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest, ErrorResponse(success = false, message = "ID resep tidak valid")
            )

            val updated = savedRecipeService.removeRecipeFromCollection(userId, collectionId, recipeId)
            if (updated == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Koleksi tidak ditemukan")
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    RecipeCollectionResponse(
                        success = true,
                        message = "Resep berhasil dihapus dari koleksi",
                        data = updated
                    )
                )
            }
        }

        // GET /api/recipe-collections/{collectionId}/recipes
        get("/{collectionId}/recipes") {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )
            val collectionId = call.parameters["collectionId"] ?: return@get call.respond(
                HttpStatusCode.BadRequest, ErrorResponse(success = false, message = "ID koleksi tidak valid")
            )

            val recipes = savedRecipeService.getRecipesInCollection(userId, collectionId)
            if (recipes == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Koleksi tidak ditemukan")
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    RecipeListResponse(
                        success = true,
                        message = "Berhasil mendapatkan resep dalam koleksi",
                        total = recipes.size,
                        limit = recipes.size,
                        offset = 0,
                        data = recipes
                    )
                )
            }
        }
    }

    // --- SHOPPING LIST ---
    route("/api/shopping-list") {

        // GET /api/shopping-list/items
        get("/items") {
            val userId = call.getAuthenticatedUserId() ?: return@get call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val items = savedRecipeService.getShoppingList(userId)
            call.respond(
                HttpStatusCode.OK,
                ShoppingListResponse(
                    success = true,
                    message = "Berhasil mendapatkan daftar keranjang belanja",
                    total = items.size,
                    data = items
                )
            )
        }

        // POST /api/shopping-list/items
        post("/items") {
            val userId = call.getAuthenticatedUserId() ?: return@post call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val body = runCatching { call.receive<AddShoppingListItemRequest>() }.getOrNull() ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "Data item belanja tidak valid")
            )

            if (body.ingredientName.isBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(success = false, message = "Nama bahan tidak boleh kosong")
                )
            }

            try {
                val item = savedRecipeService.addShoppingItem(
                    userId = userId,
                    ingredientName = body.ingredientName,
                    quantity = body.quantity,
                    unit = body.unit,
                    recipeId = body.recipeId
                )
                call.respond(
                    HttpStatusCode.Created,
                    ShoppingListResponse(
                        success = true,
                        message = "Bahan berhasil ditambahkan ke keranjang belanja",
                        total = 1,
                        data = listOf(item)
                    )
                )
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(success = false, message = e.message ?: "Validasi data gagal"))
            }
        }

        // DELETE /api/shopping-list/items/{id}
        delete("/items/{id}") {
            val userId = call.getAuthenticatedUserId() ?: return@delete call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )
            val itemId = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID item belanja tidak valid")
            )

            val deleted = savedRecipeService.deleteShoppingItem(userId, itemId)
            if (!deleted) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Item belanja tidak ditemukan atau tidak memiliki akses")
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    ErrorResponse(success = true, message = "Item belanja berhasil dihapus")
                )
            }
        }

        // PATCH /api/shopping-list/items/{id}/purchased
        patch("/items/{id}/purchased") {
            val userId = call.getAuthenticatedUserId() ?: return@patch call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )
            val itemId = call.parameters["id"] ?: return@patch call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID item belanja tidak valid")
            )

            val updatedItem = savedRecipeService.markShoppingItemAsPurchased(userId, itemId)
            if (updatedItem == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Item belanja tidak ditemukan atau tidak memiliki akses")
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    ShoppingListResponse(
                        success = true,
                        message = "Item berhasil ditandai sebagai sudah dibeli",
                        total = 1,
                        data = listOf(updatedItem)
                    )
                )
            }
        }

        // POST /api/shopping-list/add-missing-ingredients
        post("/add-missing-ingredients") {
            val userId = call.getAuthenticatedUserId() ?: return@post call.respond(
                HttpStatusCode.Unauthorized,
                ErrorResponse(success = false, message = "Token autentikasi tidak valid atau belum disediakan")
            )

            val body = runCatching { call.receive<AddMissingIngredientsRequest>() }.getOrNull() ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "recipeId wajib diisi")
            )

            val added = savedRecipeService.addMissingIngredientsToShoppingList(userId, body.recipeId)
            if (added == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(success = false, message = "Resep tidak ditemukan")
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    ShoppingListResponse(
                        success = true,
                        message = "Berhasil menambahkan ${added.size} bahan kurang ke keranjang belanja",
                        total = added.size,
                        data = added
                    )
                )
            }
        }
    }
}
