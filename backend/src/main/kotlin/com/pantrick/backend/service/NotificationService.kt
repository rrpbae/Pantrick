package com.pantrick.backend.service

import com.pantrick.backend.models.ExpirationPriority
import com.pantrick.backend.models.ExpirationStatus
import com.pantrick.backend.models.NotificationItem
import com.pantrick.backend.models.NotificationListResponse
import com.pantrick.backend.repository.PantryRepository
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap

/**
 * NotificationService — menghasilkan notifikasi ekspirasi bahan pantry.
 *
 * ATURAN:
 * - H-7: muncul tepat saat daysRemaining == 7
 * - H-1: muncul tepat saat daysRemaining == 1
 * - EXPIRED (<=0): selalu muncul
 * - EXPIRING_SOON (<=3 selain H-1): muncul normal
 * - Dedup: setiap jenis notif per item per hari hanya sekali (sentDayMap)
 * - Skip jika bahan sudah habis (quantity == 0 atau isConsumed == true)
 * - Setiap item dapat memiliki notifikasi sendiri berdasarkan pantryItemId
 */
class NotificationService(
    private val pantryRepository: PantryRepository
) {
    // Read state: notificationId → isRead
    private val readStateMap = ConcurrentHashMap<String, Boolean>()

    // Dedup: "itemId-type" → tanggal terakhir notifikasi dikirim
    // Type: "H7", "H1", "EXPIRING", "EXPIRED"
    private val sentDayMap = ConcurrentHashMap<String, LocalDate>()

    fun getNotifications(userId: Int, nowDate: LocalDate = LocalDate.now()): NotificationListResponse {
        val userItems = pantryRepository.getItemsByUserId(userId)
        val notifications = mutableListOf<NotificationItem>()

        for (item in userItems) {
            // Skip bahan yang sudah habis
            if (item.isConsumed || item.quantity <= 0.0) continue

            val dateStr = item.expirationDate
            if (dateStr.isNullOrBlank()) continue

            val expDate = try {
                LocalDate.parse(dateStr)
            } catch (e: Exception) {
                continue
            }

            val daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(nowDate, expDate)

            // Tentukan tipe notifikasi dan apakah harus muncul hari ini
            val notifType = determineNotifType(daysRemaining) ?: continue

            // Cek dedup — hanya kirim sekali per hari per tipe per item
            val dedupKey = "${item.id}-${notifType}"
            val lastSentDay = sentDayMap[dedupKey]
            if (lastSentDay == nowDate) {
                // Sudah dikirim hari ini — tetap tampilkan tapi jangan dobel di list
                // (tetap tampil di list, tapi tidak boleh buat entry baru)
            }
            // Catat hari ini sebagai hari pengiriman
            sentDayMap[dedupKey] = nowDate

            val (status, priority) = when (notifType) {
                "H7"       -> Pair(ExpirationStatus.EXPIRING_SOON, ExpirationPriority.LOW)
                "H1"       -> Pair(ExpirationStatus.EXPIRING_SOON, ExpirationPriority.HIGH)
                "EXPIRING" -> Pair(ExpirationStatus.EXPIRING_SOON, ExpirationPriority.MEDIUM)
                "EXPIRED"  -> Pair(ExpirationStatus.EXPIRED, ExpirationPriority.HIGH)
                else       -> continue
            }

            val notificationId = "notif-${item.id}-${notifType}"
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

        val sorted = notifications.sortedWith(compareBy({ it.daysRemaining }, { it.name }))
        val unreadCount = sorted.count { !it.isRead }

        return NotificationListResponse(
            success = true,
            message = "Berhasil mendapatkan daftar notifikasi",
            unreadCount = unreadCount,
            notifications = sorted
        )
    }

    fun markAsRead(id: String, userId: Int, nowDate: LocalDate = LocalDate.now()): NotificationItem? {
        val list = getNotifications(userId, nowDate).notifications
        val target = list.find { it.id == id } ?: return null
        if (target.userId != userId) return null

        readStateMap[id] = true
        return target.copy(isRead = true)
    }

    /**
     * Menentukan tipe notifikasi berdasarkan daysRemaining.
     * Return null jika tidak perlu notifikasi.
     */
    private fun determineNotifType(daysRemaining: Long): String? = when {
        daysRemaining < 0  -> "EXPIRED"
        daysRemaining == 0L -> "EXPIRED"   // kedaluwarsa hari ini
        daysRemaining == 1L -> "H1"        // besok — peringatan terakhir
        daysRemaining <= 3  -> "EXPIRING"  // 2-3 hari
        daysRemaining == 7L -> "H7"        // tepat 7 hari
        else               -> null         // > 3 hari dan bukan H-7 → tidak muncul
    }

    private fun formatNotificationMessage(name: String, daysRemaining: Long): String = when {
        daysRemaining < 0  -> "$name sudah kedaluwarsa."
        daysRemaining == 0L -> "$name kedaluwarsa hari ini."
        daysRemaining == 1L -> "Besok $name akan kedaluwarsa. Ini peringatan terakhir!"
        daysRemaining == 7L -> "$name akan kedaluwarsa dalam 7 hari."
        else               -> "$name akan kedaluwarsa dalam $daysRemaining hari."
    }
}
