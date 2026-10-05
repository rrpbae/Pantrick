// [Materi: Android Notification & Local Storage] Helper untuk menampilkan notifikasi kedaluwarsa bahan berdasarkan data lokal
package com.example.pantrick.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.pantrick.HomeActivity
import com.example.pantrick.R
import com.example.pantrick.data.local.PantrickPreferences
import com.example.pantrick.data.model.PantryItem
import java.time.LocalDate

private const val TAG = "ExpirationNotifManager"

/**
 * [Materi: Notification Management Pattern]
 * Helper untuk mengecek bahan yang akan kedaluwarsa besok dan membuat Android system notification.
 * Menggunakan data lokal (SharedPreferences) tanpa bergantung pada backend.
 */
class ExpirationNotificationManager(private val context: Context) {

    private val preferences = PantrickPreferences(context)
    private val notificationManager = NotificationManagerCompat.from(context)

    companion object {
        private const val CHANNEL_ID = "pantrick_expiration_channel"
        private const val CHANNEL_NAME = "Kedaluwarsa Bahan"
        private const val CHANNEL_DESCRIPTION = "Notifikasi untuk bahan yang akan kedaluwarsa"
        private const val NOTIFIED_KEY_PREFIX = "expiration_notified_"
    }

    init {
        createNotificationChannel()
    }

    /**
     * [Materi: Notification Channel Creation]
     * Membuat notification channel untuk Android O+ (API 26+)
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            val systemNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            systemNotificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Notification channel '$CHANNEL_NAME' berhasil dibuat")
        }
    }

    /**
     * [Materi: Check & Send Notification Flow]
     * Fungsi utama untuk mengecek semua bahan dan mengirim notifikasi untuk bahan yang akan kedaluwarsa besok.
     */
    fun checkAndNotifyExpiringItems(email: String?) {
        if (email.isNullOrBlank()) {
            Log.d(TAG, "Email kosong, skip pengecekan expiration notification")
            return
        }

        // Cek permission untuk Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "POST_NOTIFICATIONS permission tidak diberikan, skip notification")
                return
            }
        }

        try {
            // Ambil semua bahan dari local storage
            val items = preferences.getPantryItems(email)
            Log.d(TAG, "Mengecek ${items.size} bahan untuk user: $email")

            // Hitung epochDay besok
            val today = LocalDate.now()
            val tomorrowEpochDay = today.plusDays(1).toEpochDay()
            
            Log.d(TAG, "Today: $today (epochDay: ${today.toEpochDay()})")
            Log.d(TAG, "Tomorrow epochDay: $tomorrowEpochDay")

            // Filter bahan yang akan kedaluwarsa besok
            val expiringTomorrow = items.filter { item ->
                item.expiryEpochDay == tomorrowEpochDay
            }

            Log.d(TAG, "Ditemukan ${expiringTomorrow.size} bahan yang akan kedaluwarsa besok")

            // Kirim notification untuk setiap bahan yang belum dinotifikasi
            expiringTomorrow.forEach { item ->
                sendNotificationIfNotAlreadySent(item, email)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error saat mengecek expiring items untuk $email", e)
        }
    }

    /**
     * [Materi: Duplicate Prevention Pattern]
     * Mengirim notifikasi hanya jika belum pernah dikirim untuk item+expiryDate yang sama.
     */
    private fun sendNotificationIfNotAlreadySent(item: PantryItem, email: String) {
        val notifiedKey = "$NOTIFIED_KEY_PREFIX${item.id}_${item.expiryEpochDay}_$email"
        
        // Cek apakah sudah pernah dikirim
        val alreadyNotified = context.getSharedPreferences("pantrick_local_prefs", Context.MODE_PRIVATE)
            .getBoolean(notifiedKey, false)
        
        if (alreadyNotified) {
            Log.d(TAG, "Notification untuk '${item.name}' sudah pernah dikirim, skip")
            return
        }

        // Kirim notification
        sendNotification(item)
        
        // Tandai sebagai sudah dikirim
        context.getSharedPreferences("pantrick_local_prefs", Context.MODE_PRIVATE)
            .edit()
            .putBoolean(notifiedKey, true)
            .apply()
        
        Log.d(TAG, "Notification untuk '${item.name}' berhasil dikirim dan ditandai")
    }

    /**
     * [Materi: Android System Notification Build]
     * Membuat dan menampilkan notifikasi di notification shade Android.
     */
    private fun sendNotification(item: PantryItem) {
        // Intent untuk membuka aplikasi saat notifikasi diklik
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            item.id.hashCode(), // Unique request code per item
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Akan dibuat jika belum ada
            .setContentTitle("Bahan akan kedaluwarsa besok")
            .setContentText("${item.name} akan kedaluwarsa besok. Segera gunakan sebelum terbuang.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("${item.name} akan kedaluwarsa besok. Segera gunakan sebelum terbuang.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Generate unique notification ID berdasarkan item.id dan expiryDate
        val notificationId = generateNotificationId(item)
        
        try {
            notificationManager.notify(notificationId, notification)
            Log.d(TAG, "✅ System notification ditampilkan untuk '${item.name}' (ID: $notificationId)")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Tidak bisa menampilkan notification", e)
        }
    }

    /**
     * [Materi: Unique ID Generation]
     * Generate notification ID yang unik per item dan tanggal kedaluwarsa.
     */
    private fun generateNotificationId(item: PantryItem): Int {
        // Kombinasi item.id dan expiryEpochDay untuk uniqueness
        return "${item.id}_${item.expiryEpochDay}".hashCode()
    }
}
