// Repository untuk Pantry API calls ke backend
package com.example.pantrick.data.repository

import android.util.Log
import com.example.pantrick.core.network.PantrickApiConfig
import com.example.pantrick.core.network.PantrickHttpClient
import com.example.pantrick.data.model.AddPantryItemRequest
import com.example.pantrick.data.model.PantryItemDto
import com.example.pantrick.data.model.PantryListResponse
import com.example.pantrick.data.model.PantryItemResponse
import com.example.pantrick.data.model.UpdatePantryItemRequest
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

private const val TAG = "PantryApiRepository"

/**
 * Repository untuk operasi Pantry melalui Backend API.
 * Menggunakan JWT token untuk authentication dan user isolation.
 */
class PantryApiRepository {
    private val client = PantrickHttpClient.client
    private val base = PantrickApiConfig.BASE_URL

    /**
     * GET /api/pantry/items
     * Fetch pantry items dari backend untuk authenticated user.
     */
    suspend fun getItems(token: String): Result<List<PantryItemDto>> {
        return try {
            val resp = client.get("$base/api/pantry/items") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val dto = resp.body<PantryListResponse>()
            Log.d(TAG, "getItems: success, ${dto.data.size} items")
            Result.success(dto.data)
        } catch (e: Exception) {
            Log.e(TAG, "getItems failed", e)
            Result.failure(e)
        }
    }

    /**
     * POST /api/pantry/items
     * Tambahkan pantry item ke backend.
     */
    suspend fun addItem(token: String, request: AddPantryItemRequest): Result<PantryItemDto> {
        return try {
            val resp = client.post("$base/api/pantry/items") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val dto = resp.body<PantryItemResponse>()
            Log.d(TAG, "addItem: success, item=${dto.data.name}")
            Result.success(dto.data)
        } catch (e: Exception) {
            Log.e(TAG, "addItem failed", e)
            Result.failure(e)
        }
    }

    /**
     * PUT /api/pantry/items/{id}
     * Update existing pantry item.
     */
    suspend fun updateItem(token: String, itemId: String, request: UpdatePantryItemRequest): Result<PantryItemDto> {
        return try {
            val resp = client.put("$base/api/pantry/items/$itemId") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val dto = resp.body<PantryItemResponse>()
            Log.d(TAG, "updateItem: success, item=${dto.data.name}")
            Result.success(dto.data)
        } catch (e: Exception) {
            Log.e(TAG, "updateItem failed", e)
            Result.failure(e)
        }
    }

    /**
     * DELETE /api/pantry/items/{id}
     * Delete pantry item dari backend.
     */
    suspend fun deleteItem(token: String, itemId: String): Result<Unit> {
        return try {
            client.delete("$base/api/pantry/items/$itemId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            Log.d(TAG, "deleteItem: success, itemId=$itemId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "deleteItem failed", e)
            Result.failure(e)
        }
    }
}
