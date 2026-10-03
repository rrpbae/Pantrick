package com.pantrick.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class NotificationItem(
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
    val message: String = "Berhasil mendapatkan daftar notifikasi",
    val unreadCount: Int,
    val notifications: List<NotificationItem>
)

@Serializable
data class MarkNotificationReadResponse(
    val success: Boolean = true,
    val message: String = "Notifikasi berhasil ditandai sebagai dibaca",
    val data: NotificationItem? = null
)
