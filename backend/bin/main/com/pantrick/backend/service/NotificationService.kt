package com.pantrick.backend.service

import com.pantrick.backend.models.ExpirationPriority
import com.pantrick.backend.models.ExpirationStatus
import com.pantrick.backend.models.NotificationItem
import com.pantrick.backend.models.NotificationListResponse
import com.pantrick.backend.repository.PantryRepository
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

class NotificationService(
    private val pantryRepository: PantryRepository
) {
    // Stores notification read state by notification id -> isRead boolean
    private val readStateMap = ConcurrentHashMap<String, Boolean>()

    fun getNotifications(userId: Int, nowDate: LocalDate = LocalDate.now()): NotificationListResponse {
        val userItems = pantryRepository.getItemsByUserId(userId)
        val notifications = mutableListOf<NotificationItem>()

        for (item in userItems) {
            val dateStr = item.expirationDate
            if (dateStr.isNullOrBlank()) continue

            val expDate = try {
                LocalDate.parse(dateStr)
            } catch (e: Exception) {
                continue
            }

            val daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(nowDate, expDate)

            val (status, priority) = when {
                daysRemaining < 0 -> Pair(ExpirationStatus.EXPIRED, ExpirationPriority.HIGH)
                daysRemaining <= 3 -> Pair(ExpirationStatus.EXPIRING_SOON, if (daysRemaining <= 0) ExpirationPriority.HIGH else ExpirationPriority.MEDIUM)
                else -> continue
            }

            val notificationId = "notif-${item.id}"
            val isRead = readStateMap[notificationId] ?: false
            val message = formatNotificationMessage(item.name, daysRemaining)

            notifications.add(
                NotificationItem(
                    id = notificationId,
                    userId = userId,
                    pantryItemId = item.id,
                    name = item.name,
                    message = message,
                    type = status,
                    priority = priority,
                    daysRemaining = daysRemaining,
                    expirationDate = dateStr,
                    isRead = isRead
                )
            )
        }

        // Sort: 1. Expired/smallest daysRemaining first, 2. Name
        val sortedNotifications = notifications.sortedWith(compareBy({ it.daysRemaining }, { it.name }))
        val unreadCount = sortedNotifications.count { !it.isRead }

        return NotificationListResponse(
            success = true,
            message = "Berhasil mendapatkan daftar notifikasi",
            unreadCount = unreadCount,
            notifications = sortedNotifications
        )
    }

    fun markAsRead(id: String, userId: Int, nowDate: LocalDate = LocalDate.now()): NotificationItem? {
        val list = getNotifications(userId, nowDate).notifications
        val target = list.find { it.id == id } ?: return null
        if (target.userId != userId) return null

        readStateMap[id] = true
        return target.copy(isRead = true)
    }

    private fun formatNotificationMessage(name: String, daysRemaining: Long): String {
        return when {
            daysRemaining < 0 -> "$name sudah kedaluwarsa."
            daysRemaining == 0L -> "$name kedaluwarsa hari ini."
            daysRemaining == 1L -> "$name akan kedaluwarsa besok."
            else -> "$name akan kedaluwarsa dalam $daysRemaining hari."
        }
    }
}
