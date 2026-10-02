// [Materi: Kotlin Serialization & Data Class] Model representasi data bahan di dalam pantry pengguna
package com.example.pantrick.data.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

// [Materi: Enum Class] Lokasi penyimpanan bahan makanan (Kulkas, Freezer, dan Rak Kering)
@Serializable
enum class StorageLocation(val label: String) {
    KULKAS("Kulkas"),
    FREEZER("Freezer"),
    RAK_KERING("Rak Kering")
}

// [Materi: Enum Class] Kategori bahan makanan untuk filter visual dan pengelompokan bahan
@Serializable
enum class FoodCategory(val label: String, val emoji: String) {
    SUSU_TELUR("Susu & Telur", "🥛"),
    SAYUR_BUAH("Sayur & Buah", "🥬"),
    DAGING_IKAN("Daging & Ikan", "🥩"),
    ROTI_KUE("Roti & Kue", "🍞"),
    MINUMAN("Minuman", "🧃"),
    BUMBU_KERING("Bumbu & Kering", "🧂"),
    LAINNYA("Lainnya", "📦")
}

// [Materi: @Serializable Entity] Model bahan pantry per pengguna yang tersimpan dalam format JSON
@Serializable
data class PantryItem(
    val id: String,
    val name: String,
    val location: StorageLocation,
    val quantityLabel: String,
    val expiryEpochDay: Long,
    val category: FoodCategory = FoodCategory.LAINNYA // [Materi: Default Parameter] Kompatibilitas mundur dengan JSON dan pemanggilan lama
) {
    // [Materi: Pure Function & Testability] Menghitung sisa hari kedaluwarsa berdasarkan epoch day yang dioper
    fun daysLeft(todayEpochDay: Long): Long = expiryEpochDay - todayEpochDay

    // [Materi: Computed / Derived Property] Sisa hari dari tanggal hari ini perangkat
    val daysLeft: Long
        get() = daysLeft(LocalDate.now().toEpochDay())
}
