// [Materi: Notification Models] Model notifikasi ekspirasi dari backend
package com.example.pantrick.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ExpirationStatus {
    EXPIRING_SOON,
    EXPIRED
}

@Serializable
enum class ExpirationPriority {
    LOW,    // H-7
    MEDIUM, // 2-3 hari
    HIGH    // H-1 atau sudah expired
}

@Serializable
data class NotificationItemDto(
    val id: String,
    val userId: Int,
    val pantryItemId: String,
    val name: String,
    val message: String,
    val type: ExpirationStatus,
    val priority: ExpirationPriority,
    val daysRemaining: Long,
    val expirationDate: String,
    val isRead: Boolean = false
)

@Serializable
data class NotificationListResponse(
    val success: Boolean = true,
    val message: String = "",
    val unreadCount: Int = 0,
    val notifications: List<NotificationItemDto> = emptyList()
)

@Serializable
data class MarkNotificationReadResponse(
    val success: Boolean = true,
    val message: String = "",
    val data: NotificationItemDto? = null
)
