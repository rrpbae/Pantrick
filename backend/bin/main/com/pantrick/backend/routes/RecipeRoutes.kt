package com.pantrick.backend.routes

import com.pantrick.backend.models.ErrorResponse
import com.pantrick.backend.models.RecipeDetailResponse
import com.pantrick.backend.models.RecipeIngredientsResponse
import com.pantrick.backend.models.RecipeListResponse
import com.pantrick.backend.service.RecipeService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.recipeRoutes(recipeService: RecipeService) {
    route("/api/recipes") {

        // GET /api/recipes
        get {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

            val (recipes, total) = recipeService.getRecipes(limit, offset)

            call.respond(
                HttpStatusCode.OK,
                RecipeListResponse(
                    success = true,
                    message = "Berhasil mendapatkan daftar resep",
                    total = total,
                    limit = limit,
                    offset = offset,
                    data = recipes
                )
            )
        }

        // GET /api/recipes/search?q=...
        get("/search") {
            val query = call.request.queryParameters["q"] ?: ""
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

            val (recipes, total) = recipeService.searchRecipes(query, limit, offset)

            call.respond(
                HttpStatusCode.OK,
                RecipeListResponse(
                    success = true,
                    message = "Berhasil mencari resep dengan kata kunci '$query'",
                    total = total,
                    limit = limit,
                    offset = offset,
                    data = recipes
                )
            )
        }

        // GET /api/recipes/{id}
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID resep tidak valid")
            )

            val recipe = recipeService.getRecipeById(id)
            if (recipe == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    RecipeDetailResponse(
                        success = false,
                        message = "Resep dengan ID '$id' tidak ditemukan",
                        data = null
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    RecipeDetailResponse(
                        success = true,
                        message = "Berhasil mendapatkan detail resep",
                        data = recipe
                    )
                )
            }
        }

        // GET /api/recipes/{id}/ingredients
        get("/{id}/ingredients") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(success = false, message = "ID resep tidak valid")
            )

            val ingredients = recipeService.getIngredientsByRecipeId(id)
            if (ingredients == null) {
                call.respond(
                    HttpStatusCode.NotFound,
                    ErrorResponse(
                        success = false,
                        message = "Resep dengan ID '$id' tidak ditemukan"
                    )
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    RecipeIngredientsResponse(
                        success = true,
                        message = "Berhasil mendapatkan daftar bahan resep",
                        recipeId = id,
                        total = ingredients.size,
                        data = ingredients
                    )
                )
            }
        }
    }
}
