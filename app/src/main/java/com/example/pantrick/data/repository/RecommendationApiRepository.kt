// [Materi: Repository Pattern] Repositori untuk mengambil rekomendasi resep dari backend Pantrick
package com.example.pantrick.data.repository

import android.util.Log
import com.example.pantrick.core.network.PantrickApiConfig
import com.example.pantrick.core.network.PantrickHttpClient
import com.example.pantrick.data.model.BackendRecipe
import com.example.pantrick.data.model.BackendRecommendation
import com.example.pantrick.data.model.IngredientSearchApiRequest
import com.example.pantrick.data.model.IngredientSearchApiResponse
import com.example.pantrick.data.model.RecipeDetailApiResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

private const val TAG = "RecommendationRepository"

/**
 * Repositori untuk mengambil rekomendasi resep dari backend berdasarkan bahan input user.
 * Menggunakan endpoint POST /api/recipes/search-by-ingredients (tanpa autentikasi).
 */
class RecommendationApiRepository {

    private val client = PantrickHttpClient.client
    private val baseUrl = PantrickApiConfig.BASE_URL

    /**
     * Mencari rekomendasi resep berdasarkan daftar nama bahan.
     * Mengembalikan Result yang berisi list rekomendasi atau exception jika gagal.
     */
    suspend fun searchByIngredients(
        ingredients: List<String>,
        limit: Int = 10
    ): Result<List<BackendRecommendation>> {
        return try {
            val requestBody = IngredientSearchApiRequest(ingredients = ingredients)
            val response = client.post("$baseUrl/api/recipes/search-by-ingredients?limit=$limit") {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            val apiResponse = response.body<IngredientSearchApiResponse>()
            if (apiResponse.success) {
                Log.d(TAG, "searchByIngredients berhasil: ${apiResponse.total} resep ditemukan")
                Result.success(apiResponse.recommendations)
            } else {
                Log.w(TAG, "searchByIngredients: backend mengembalikan success=false: ${apiResponse.message}")
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "searchByIngredients gagal", e)
            Result.failure(e)
        }
    }

    /**
     * Mengambil detail satu resep berdasarkan ID dari dataset.
     */
    suspend fun getRecipeById(recipeId: String): Result<BackendRecipe?> {
        return try {
            val response = client.get("$baseUrl/api/recipes/$recipeId")
            val apiResponse = response.body<RecipeDetailApiResponse>()
            if (apiResponse.success) {
                Log.d(TAG, "getRecipeById ($recipeId) berhasil")
                Result.success(apiResponse.data)
            } else {
                Log.w(TAG, "getRecipeById ($recipeId): resep tidak ditemukan")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "getRecipeById ($recipeId) gagal", e)
            Result.failure(e)
        }
    }
}
