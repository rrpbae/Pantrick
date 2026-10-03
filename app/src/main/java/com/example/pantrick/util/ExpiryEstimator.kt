// [Materi: Business Logic & Estimation Helper] Tabel dan kalkulator perkiraan kedaluwarsa bahan makanan
package com.example.pantrick.util

import com.example.pantrick.data.model.FoodCategory
import com.example.pantrick.data.model.StorageLocation
import java.time.LocalDate

object ExpiryEstimator {

    /**
     * Menghitung perkiraan sisa hari kedaluwarsa berdasarkan kategori, lokasi penyimpanan,
     * dan kata kunci nama bahan (opsional untuk pembedaan spesifik seperti daging vs ikan, telur vs susu).
     */
    fun estimateDays(
        category: FoodCategory,
        location: StorageLocation,
        itemName: String? = null
    ): Long {
        val lower = itemName?.lowercase() ?: ""
        return when (location) {
            StorageLocation.KULKAS -> when (category) {
                FoodCategory.DAGING_IKAN -> 2L
                FoodCategory.SUSU_TELUR -> if (lower.contains("telur")) 21L else 7L
                FoodCategory.SAYUR_BUAH -> if (lower.contains("buah") || lower.contains("apel") || lower.contains("jeruk") || lower.contains("pisang")) 7L else 5L
                FoodCategory.ROTI_KUE -> 5L
                FoodCategory.MINUMAN -> 7L
                FoodCategory.BUMBU_KERING -> 180L
                FoodCategory.LAINNYA -> 7L
            }
            StorageLocation.FREEZER -> when (category) {
                FoodCategory.DAGING_IKAN -> if (lower.contains("ikan") || lower.contains("seafood") || lower.contains("udang")) 60L else 90L
                FoodCategory.SUSU_TELUR -> 60L
                FoodCategory.SAYUR_BUAH -> 180L
                FoodCategory.ROTI_KUE -> 90L
                FoodCategory.MINUMAN -> 60L
                FoodCategory.BUMBU_KERING -> 365L
                FoodCategory.LAINNYA -> 90L
            }
        }
    }

    /**
     * Menghasilkan LocalDate perkiraan kedaluwarsa dari tanggal acuan (default hari ini).
     */
    fun estimateExpiryDate(
        category: FoodCategory,
        location: StorageLocation,
        itemName: String? = null,
        baseDate: LocalDate = LocalDate.now()
    ): LocalDate {
        return baseDate.plusDays(estimateDays(category, location, itemName))
    }
}
