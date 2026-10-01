// [Materi: Pure Function & Single Source of Truth] Helper tunggal status kedaluwarsa bahan makanan
package com.example.pantrick.util

import kotlin.math.abs

// [Materi: Data Class] Informasi visual status kedaluwarsa untuk badge kartu dan pill waktu
data class ExpiryInfo(
    val badgeLabel: String,
    val pillLabel: String,
    val isUrgent: Boolean,
    val isOverdue: Boolean
)

// [Materi: Singleton Object] Helper murni penghitung status kedaluwarsa tanpa dependensi Android SDK
object ExpiryStatus {

    // [Materi: Pure Function] Menentukan label dan status urgensi berdasarkan sisa hari
    fun evaluate(daysLeft: Long): ExpiryInfo {
        return when {
            daysLeft < 0 -> {
                val overdueDays = abs(daysLeft)
                ExpiryInfo(
                    badgeLabel = "Lewat $overdueDays hari",
                    pillLabel = "Lewat $overdueDays hari",
                    isUrgent = true,
                    isOverdue = true
                )
            }
            daysLeft == 0L -> {
                ExpiryInfo(
                    badgeLabel = "Hari ini",
                    pillLabel = "Exp. hari ini",
                    isUrgent = true,
                    isOverdue = false
                )
            }
            daysLeft == 1L -> {
                ExpiryInfo(
                    badgeLabel = "1 hr lagi",
                    pillLabel = "Exp. besok",
                    isUrgent = true,
                    isOverdue = false
                )
            }
            daysLeft == 2L -> {
                ExpiryInfo(
                    badgeLabel = "2 hr lagi",
                    pillLabel = "Exp. 2 hari lagi",
                    isUrgent = true,
                    isOverdue = false
                )
            }
            else -> {
                ExpiryInfo(
                    badgeLabel = "Segar",
                    pillLabel = "Exp. $daysLeft hari lagi",
                    isUrgent = false,
                    isOverdue = false
                )
            }
        }
    }
}
