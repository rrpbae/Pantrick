// [Materi: Pure Functions & Testability] Logika murni penyaringan, pencarian, dan pengurutan bahan pantry
package com.example.pantrick.util

import androidx.annotation.StringRes
import com.example.pantrick.R
import com.example.pantrick.data.model.FoodCategory
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.StorageLocation

// [Materi: Sealed Interface] Pemodelan status chip filter aktif (Hanya satu filter yang dapat aktif)
sealed interface ChipFilter {
    data object None : ChipFilter
    data object Expiring : ChipFilter
    data class Category(val category: FoodCategory) : ChipFilter
}

// [Materi: Enum Class dengan String Resource] Opsi pengurutan bahan makanan
enum class SortOrder(@get:StringRes val labelRes: Int) {
    EXPIRING_SOON(R.string.sort_expiring_soon),
    EXPIRING_LATER(R.string.sort_expiring_later),
    NAME_ASC(R.string.sort_name_asc)
}

// [Materi: Data Class] Informasi terstruktur untuk komponen banner peringatan
data class BannerInfo(
    val firstNames: List<String>,
    val extraCount: Int,
    val hasOverdue: Boolean,
    val totalCount: Int
)

// [Materi: Singleton Object] Fungsi-fungsi murni kalkulasi data pantry tanpa ketergantungan Android Framework
object PantryFilter {

    // [Materi: Pure Function] Menghitung total bahan pada lokasi penyimpanan tertentu
    fun countByLocation(items: List<PantryItem>, location: StorageLocation): Int {
        return items.count { it.location == location }
    }

    // [Materi: Pure Function] Mengambil daftar bahan yang hampir kedaluwarsa (<= 2 hari atau sudah lewat) di suatu lokasi
    fun expiringItems(items: List<PantryItem>, location: StorageLocation, todayEpochDay: Long): List<PantryItem> {
        return items.filter {
            it.location == location && it.daysLeft(todayEpochDay) <= PantrickConstants.EXPIRING_THRESHOLD_DAYS
        }.sortedBy { it.daysLeft(todayEpochDay) }
    }

    // [Materi: Pure Function] Mengambil seluruh bahan yang hampir kedaluwarsa di semua lokasi (untuk lonceng notifikasi)
    fun expiringItems(items: List<PantryItem>, todayEpochDay: Long): List<PantryItem> {
        return items.filter {
            it.daysLeft(todayEpochDay) <= PantrickConstants.EXPIRING_THRESHOLD_DAYS
        }.sortedBy { it.daysLeft(todayEpochDay) }
    }

    // [Materi: Pure Function] Mengambil kategori yang memiliki bahan di lokasi aktif beserta jumlahnya
    fun availableCategories(items: List<PantryItem>, location: StorageLocation): List<Pair<FoodCategory, Int>> {
        val locationItems = items.filter { it.location == location }
        return FoodCategory.entries.mapNotNull { category ->
            val count = locationItems.count { it.category == category }
            if (count > 0) category to count else null
        }
    }

    // [Materi: Pure Function] Validasi chip terpilih agar tidak "nyangkut" jika item terakhirnya dihapus/dipindah
    fun resolveChip(
        items: List<PantryItem>,
        location: StorageLocation,
        chip: ChipFilter,
        todayEpochDay: Long
    ): ChipFilter {
        return when (chip) {
            is ChipFilter.None -> ChipFilter.None
            is ChipFilter.Expiring -> {
                val hasExpiring = items.any {
                    it.location == location && it.daysLeft(todayEpochDay) <= PantrickConstants.EXPIRING_THRESHOLD_DAYS
                }
                if (hasExpiring) ChipFilter.Expiring else ChipFilter.None
            }
            is ChipFilter.Category -> {
                val hasCategory = items.any { it.location == location && it.category == chip.category }
                if (hasCategory) chip else ChipFilter.None
            }
        }
    }

    // [Materi: Pure Function] Merangkai data nama bahan untuk banner peringatan
    fun buildBannerInfo(expiringItems: List<PantryItem>, todayEpochDay: Long): BannerInfo {
        val total = expiringItems.size
        if (total == 0) {
            return BannerInfo(
                firstNames = emptyList(),
                extraCount = 0,
                hasOverdue = false,
                totalCount = 0
            )
        }
        val hasOverdue = expiringItems.any { it.daysLeft(todayEpochDay) < 0 }
        return if (total <= 2) {
            BannerInfo(
                firstNames = expiringItems.map { it.name },
                extraCount = 0,
                hasOverdue = hasOverdue,
                totalCount = total
            )
        } else {
            BannerInfo(
                firstNames = expiringItems.take(2).map { it.name },
                extraCount = total - 2,
                hasOverdue = hasOverdue,
                totalCount = total
            )
        }
    }

    // [Materi: Pure Function] Menerapkan gabungan filter lokasi, chip, pencarian, dan pengurutan
    fun apply(
        items: List<PantryItem>,
        location: StorageLocation,
        query: String,
        chip: ChipFilter,
        sortOrder: SortOrder,
        todayEpochDay: Long
    ): List<PantryItem> {
        val trimmedQuery = query.trim()

        return items
            // 1. Filter Lokasi
            .filter { it.location == location }
            // 2. Filter Chip
            .filter { item ->
                when (chip) {
                    is ChipFilter.None -> true
                    is ChipFilter.Expiring -> item.daysLeft(todayEpochDay) <= PantrickConstants.EXPIRING_THRESHOLD_DAYS
                    is ChipFilter.Category -> item.category == chip.category
                }
            }
            // 3. Filter Pencarian (Nama atau Label Kategori, Case-Insensitive)
            .filter { item ->
                if (trimmedQuery.isEmpty()) {
                    true
                } else {
                    item.name.contains(trimmedQuery, ignoreCase = true) ||
                        item.category.label.contains(trimmedQuery, ignoreCase = true)
                }
            }
            // 4. Pengurutan Data
            .let { list ->
                when (sortOrder) {
                    SortOrder.EXPIRING_SOON -> list.sortedWith(
                        compareBy<PantryItem> { it.daysLeft(todayEpochDay) }.thenBy { it.name.lowercase() }
                    )
                    SortOrder.EXPIRING_LATER -> list.sortedWith(
                        compareByDescending<PantryItem> { it.daysLeft(todayEpochDay) }.thenBy { it.name.lowercase() }
                    )
                    SortOrder.NAME_ASC -> list.sortedBy { it.name.lowercase() }
                }
            }
    }
}
