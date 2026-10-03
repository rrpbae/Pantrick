// [Materi: Kotlin Serialization & Data Class] Model representasi data bahan di dalam pantry pengguna
package com.example.pantrick.data.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate

// [Materi: Enum Class & Custom Serializer] Lokasi penyimpanan bahan makanan (hanya Kulkas dan Freezer, data lama Rak Kering otomatis migrasi ke Kulkas)
@Serializable(with = StorageLocationSerializer::class)
enum class StorageLocation(val label: String) {
    KULKAS("Kulkas"),
    FREEZER("Freezer")
}

object StorageLocationSerializer : KSerializer<StorageLocation> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StorageLocation", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: StorageLocation) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): StorageLocation {
        val name = decoder.decodeString()
        return when (name.uppercase()) {
            "FREEZER" -> StorageLocation.FREEZER
            else -> StorageLocation.KULKAS
        }
    }
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
    val category: FoodCategory = FoodCategory.LAINNYA, // [Materi: Default Parameter] Kompatibilitas mundur dengan JSON dan pemanggilan lama
    val isExpiryEstimated: Boolean = true // [Materi: Field Penanda Perkiraan] Menandakan apakah tanggal kedaluwarsa dihitung otomatis atau manual
) {
    // [Materi: Pure Function & Testability] Menghitung sisa hari kedaluwarsa berdasarkan epoch day yang dioper
    fun daysLeft(todayEpochDay: Long): Long = expiryEpochDay - todayEpochDay

    // [Materi: Computed / Derived Property] Sisa hari dari tanggal hari ini perangkat
    val daysLeft: Long
        get() = daysLeft(LocalDate.now().toEpochDay())
}
