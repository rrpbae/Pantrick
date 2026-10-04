// [Materi: Repository Pattern] Repository untuk API cooking session
package com.example.pantrick.data.repository

import android.util.Log
import com.example.pantrick.core.network.PantrickApiConfig
import com.example.pantrick.core.network.PantrickHttpClient
import com.example.pantrick.data.model.CookingReadinessDto
import com.example.pantrick.data.model.CookingSessionResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders

private const val TAG = "CookingSessionRepo"

class CookingSessionRepository {
    private val client = PantrickHttpClient.client
    private val base = PantrickApiConfig.BASE_URL

    /** Cek apakah semua bahan recipe tersedia (nama + quantity) */
    suspend fun checkReadiness(recipeId: String, token: String): Result<CookingReadinessDto> {
        return try {
            val resp = client.get("$base/api/recipes/$recipeId/cook/readiness") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val dto = resp.body<CookingReadinessDto>()
            Log.d(TAG, "checkReadiness($recipeId): canCook=${dto.canCook}")
            Result.success(dto)
        } catch (e: Exception) {
            Log.e(TAG, "checkReadiness failed", e)
            Result.failure(e)
        }
    }

    /** Mulai memasak — deduct pantry, buat session STARTED */
    suspend fun startCooking(recipeId: String, token: String): Result<CookingSessionResponse> {
        return try {
            val resp = client.post("$base/api/recipes/$recipeId/cook") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val dto = resp.body<CookingSessionResponse>()
            Log.d(TAG, "startCooking($recipeId): success=${dto.success} session=${dto.data?.id}")
            Result.success(dto)
        } catch (e: Exception) {
            Log.e(TAG, "startCooking failed", e)
            Result.failure(e)
        }
    }

    /** Batalkan sesi — rollback pantry */
    suspend fun cancelCooking(sessionId: String, token: String): Result<CookingSessionResponse> {
        return try {
            val resp = client.post("$base/api/cooking-sessions/$sessionId/cancel") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val dto = resp.body<CookingSessionResponse>()
            Log.d(TAG, "cancelCooking($sessionId): success=${dto.success}")
            Result.success(dto)
        } catch (e: Exception) {
            Log.e(TAG, "cancelCooking failed", e)
            Result.failure(e)
        }
    }

    /** Selesaikan sesi — pantry tetap berkurang */
    suspend fun completeCooking(sessionId: String, token: String): Result<CookingSessionResponse> {
        return try {
            val resp = client.post("$base/api/cooking-sessions/$sessionId/complete") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val dto = resp.body<CookingSessionResponse>()
            Log.d(TAG, "completeCooking($sessionId): success=${dto.success}")
            Result.success(dto)
        } catch (e: Exception) {
            Log.e(TAG, "completeCooking failed", e)
            Result.failure(e)
        }
    }

    /** Cek sesi aktif untuk recipe tertentu */
    suspend fun getActiveSession(recipeId: String, token: String): Result<CookingSessionResponse> {
        return try {
            val resp = client.get("$base/api/cooking-sessions/active?recipeId=$recipeId") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val dto = resp.body<CookingSessionResponse>()
            Result.success(dto)
        } catch (e: Exception) {
            Log.e(TAG, "getActiveSession failed", e)
            Result.failure(e)
        }
    }
}
