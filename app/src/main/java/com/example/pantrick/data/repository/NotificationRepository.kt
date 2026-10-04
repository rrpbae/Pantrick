// [Materi: Repository Pattern] Repository untuk notifikasi ekspirasi dari backend
package com.example.pantrick.data.repository

import android.util.Log
import com.example.pantrick.core.network.PantrickApiConfig
import com.example.pantrick.core.network.PantrickHttpClient
import com.example.pantrick.data.model.NotificationListResponse
import com.example.pantrick.data.model.MarkNotificationReadResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.http.HttpHeaders

private const val TAG = "NotificationRepo"

class NotificationRepository {
    private val client = PantrickHttpClient.client
    private val base = PantrickApiConfig.BASE_URL

    /** Ambil semua notifikasi ekspirasi bahan untuk user yang sedang login */
    suspend fun getNotifications(token: String): Result<NotificationListResponse> {
        return try {
            val resp = client.get("$base/api/notifications") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val dto = resp.body<NotificationListResponse>()
            Log.d(TAG, "getNotifications: unreadCount=${dto.unreadCount} total=${dto.notifications.size}")
            Result.success(dto)
        } catch (e: Exception) {
            Log.e(TAG, "getNotifications failed", e)
            Result.failure(e)
        }
    }

    /** Tandai notifikasi sebagai sudah dibaca */
    suspend fun markAsRead(notificationId: String, token: String): Result<MarkNotificationReadResponse> {
        return try {
            val resp = client.patch("$base/api/notifications/$notificationId/read") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val dto = resp.body<MarkNotificationReadResponse>()
            Log.d(TAG, "markAsRead($notificationId): success=${dto.success}")
            Result.success(dto)
        } catch (e: Exception) {
            Log.e(TAG, "markAsRead failed", e)
            Result.failure(e)
        }
    }
}
