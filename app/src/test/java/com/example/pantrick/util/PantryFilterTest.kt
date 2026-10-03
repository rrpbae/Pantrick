// [Materi: Unit Testing & JUnit 4] Pengujian komprehensif logika murni penyaringan dan kalkulasi bahan pantry
package com.example.pantrick.util

import com.example.pantrick.data.model.FoodCategory
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.StorageLocation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PantryFilterTest {

    private val todayEpochDay: Long = 20000L

    private fun createItem(
        id: String,
        name: String,
        location: StorageLocation = StorageLocation.KULKAS,
        category: FoodCategory = FoodCategory.LAINNYA,
        daysOffset: Long = 5L
    ): PantryItem {
        return PantryItem(
            id = id,
            name = name,
            location = location,
            category = category,
            quantityLabel = "1 pcs",
            expiryEpochDay = todayEpochDay + daysOffset
        )
    }

    // ==================== 1. PENGUJIAN LIST KOSONG (USER BARU) ====================

    @Test
    fun countByLocation_emptyList_returnsZero() {
        val count = PantryFilter.countByLocation(emptyList(), StorageLocation.KULKAS)
        assertEquals(0, count)
    }

    @Test
    fun expiringItems_emptyList_returnsEmptyList() {
        val expiring = PantryFilter.expiringItems(emptyList(), StorageLocation.KULKAS, todayEpochDay)
        assertTrue(expiring.isEmpty())
    }

    @Test
    fun availableCategories_emptyList_returnsEmptyList() {
        val categories = PantryFilter.availableCategories(emptyList(), StorageLocation.KULKAS)
        assertTrue(categories.isEmpty())
    }

    @Test
    fun resolveChip_emptyList_returnsNone() {
        val resolvedExpiring = PantryFilter.resolveChip(
            emptyList(),
            StorageLocation.KULKAS,
            ChipFilter.Expiring,
            todayEpochDay
        )
        assertEquals(ChipFilter.None, resolvedExpiring)

        val resolvedCategory = PantryFilter.resolveChip(
            emptyList(),
            StorageLocation.KULKAS,
            ChipFilter.Category(FoodCategory.SUSU_TELUR),
            todayEpochDay
        )
        assertEquals(ChipFilter.None, resolvedCategory)
    }

    @Test
    fun buildBannerInfo_emptyList_returnsEmptyBanner() {
        val info = PantryFilter.buildBannerInfo(emptyList(), todayEpochDay)
        assertEquals(0, info.totalCount)
        assertTrue(info.firstNames.isEmpty())
        assertEquals(0, info.extraCount)
        assertFalse(info.hasOverdue)
    }

    @Test
    fun apply_emptyList_returnsEmptyList() {
        val result = PantryFilter.apply(
            items = emptyList(),
            location = StorageLocation.KULKAS,
            query = "",
            chip = ChipFilter.None,
            sortOrder = SortOrder.EXPIRING_SOON,
            todayEpochDay = todayEpochDay
        )
        assertTrue(result.isEmpty())
    }

    // ==================== 2. HITUNGAN LOKASI ====================

    @Test
    fun countByLocation_countsAccurately() {
        val items = listOf(
            createItem("1", "Susu", location = StorageLocation.KULKAS),
            createItem("2", "Keju", location = StorageLocation.KULKAS),
            createItem("3", "Daging", location = StorageLocation.FREEZER),
            createItem("4", "Ikan", location = StorageLocation.FREEZER)
        )

        assertEquals(2, PantryFilter.countByLocation(items, StorageLocation.KULKAS))
        assertEquals(2, PantryFilter.countByLocation(items, StorageLocation.FREEZER))
    }

    // ==================== 3. AMBANG BATAS HARI KEDALUWARSA ====================

    @Test
    fun expiringItems_includesNegativeZeroOneTwo_excludesThree() {
        val items = listOf(
            createItem("overdue", "Susu Basi", daysOffset = -2), // lewat 2 hari
            createItem("today", "Ikan Segar", daysOffset = 0),   // hari ini
            createItem("tomorrow", "Bayam", daysOffset = 1),     // besok
            createItem("in2days", "Tahu", daysOffset = 2),       // lusa
            createItem("in3days", "Apel", daysOffset = 3),       // 3 hari lagi (tidak mendesak)
            createItem("in10days", "Telur", daysOffset = 10)     // aman
        )

        val result = PantryFilter.expiringItems(items, StorageLocation.KULKAS, todayEpochDay)
        assertEquals(4, result.size)
        val ids = result.map { it.id }
        assertTrue(ids.contains("overdue"))
        assertTrue(ids.contains("today"))
        assertTrue(ids.contains("tomorrow"))
        assertTrue(ids.contains("in2days"))
        assertFalse(ids.contains("in3days"))
        assertFalse(ids.contains("in10days"))
    }

    // ==================== 4. KATEGORI TERSEDIA HANYA YANG BERISI ====================

    @Test
    fun availableCategories_onlyReturnsCategoriesWithItemsInLocation() {
        val items = listOf(
            createItem("1", "Susu", category = FoodCategory.SUSU_TELUR, location = StorageLocation.KULKAS),
            createItem("2", "Telur", category = FoodCategory.SUSU_TELUR, location = StorageLocation.KULKAS),
            createItem("3", "Bayam", category = FoodCategory.SAYUR_BUAH, location = StorageLocation.KULKAS),
            createItem("4", "Daging", category = FoodCategory.DAGING_IKAN, location = StorageLocation.FREEZER)
        )

        val kulkasCategories = PantryFilter.availableCategories(items, StorageLocation.KULKAS)
        assertEquals(2, kulkasCategories.size)
        assertEquals(FoodCategory.SUSU_TELUR to 2, kulkasCategories.find { it.first == FoodCategory.SUSU_TELUR })
        assertEquals(FoodCategory.SAYUR_BUAH to 1, kulkasCategories.find { it.first == FoodCategory.SAYUR_BUAH })
        // Daging_Ikan tidak ada di Kulkas
        assertTrue(kulkasCategories.none { it.first == FoodCategory.DAGING_IKAN })
    }

    // ==================== 5. RESOLVE CHIP (TIDAK NYANGKUT) ====================

    @Test
    fun resolveChip_resetsToNoneIfItemsNoLongerMatch() {
        val items = listOf(
            createItem("1", "Apel", daysOffset = 10, category = FoodCategory.SAYUR_BUAH, location = StorageLocation.KULKAS)
        )

        // Tidak ada item expiring di kulkas
        val resolvedExpiring = PantryFilter.resolveChip(items, StorageLocation.KULKAS, ChipFilter.Expiring, todayEpochDay)
        assertEquals(ChipFilter.None, resolvedExpiring)

        // Tidak ada item Susu & Telur di kulkas
        val resolvedCategory = PantryFilter.resolveChip(
            items,
            StorageLocation.KULKAS,
            ChipFilter.Category(FoodCategory.SUSU_TELUR),
            todayEpochDay
        )
        assertEquals(ChipFilter.None, resolvedCategory)

        // Sayur & Buah ada -> tetap Category
        val resolvedValidCategory = PantryFilter.resolveChip(
            items,
            StorageLocation.KULKAS,
            ChipFilter.Category(FoodCategory.SAYUR_BUAH),
            todayEpochDay
        )
        assertEquals(ChipFilter.Category(FoodCategory.SAYUR_BUAH), resolvedValidCategory)
    }

    // ==================== 6. BANNER INFO (1, 2, 3+ ITEM & OVERDUE) ====================

    @Test
    fun buildBannerInfo_singleItem() {
        val items = listOf(createItem("1", "Susu", daysOffset = 1))
        val info = PantryFilter.buildBannerInfo(items, todayEpochDay)

        assertEquals(1, info.totalCount)
        assertEquals(listOf("Susu"), info.firstNames)
        assertEquals(0, info.extraCount)
        assertFalse(info.hasOverdue)
    }

    @Test
    fun buildBannerInfo_twoItems() {
        val items = listOf(
            createItem("1", "Susu", daysOffset = 1),
            createItem("2", "Bayam", daysOffset = 2)
        )
        val info = PantryFilter.buildBannerInfo(items, todayEpochDay)

        assertEquals(2, info.totalCount)
        assertEquals(listOf("Susu", "Bayam"), info.firstNames)
        assertEquals(0, info.extraCount)
        assertFalse(info.hasOverdue)
    }

    @Test
    fun buildBannerInfo_threeOrMoreItems_withOverdue() {
        val items = listOf(
            createItem("1", "Susu", daysOffset = -1), // Lewat
            createItem("2", "Bayam", daysOffset = 0),
            createItem("3", "Tahu", daysOffset = 2),
            createItem("4", "Tempe", daysOffset = 1)
        )
        val info = PantryFilter.buildBannerInfo(items, todayEpochDay)

        assertEquals(4, info.totalCount)
        assertEquals(listOf("Susu", "Bayam"), info.firstNames)
        assertEquals(2, info.extraCount)
        assertTrue(info.hasOverdue)
    }

    // ==================== 7. PENCARIAN CASE-INSENSITIVE & TRIM ====================

    @Test
    fun apply_searchCaseInsensitiveAndTrim() {
        val items = listOf(
            createItem("1", "Susu UHT Segar", location = StorageLocation.KULKAS),
            createItem("2", "Daging Ayam", location = StorageLocation.KULKAS)
        )

        val searchLower = PantryFilter.apply(items, StorageLocation.KULKAS, "susu", ChipFilter.None, SortOrder.NAME_ASC, todayEpochDay)
        assertEquals(1, searchLower.size)
        assertEquals("Susu UHT Segar", searchLower.first().name)

        val searchPaddedUpper = PantryFilter.apply(items, StorageLocation.KULKAS, "  SUSU  ", ChipFilter.None, SortOrder.NAME_ASC, todayEpochDay)
        assertEquals(1, searchPaddedUpper.size)
        assertEquals("Susu UHT Segar", searchPaddedUpper.first().name)
    }

    @Test
    fun apply_searchMatchesCategoryLabel() {
        val items = listOf(
            createItem("1", "Wortel", category = FoodCategory.SAYUR_BUAH, location = StorageLocation.KULKAS),
            createItem("2", "Keju", category = FoodCategory.SUSU_TELUR, location = StorageLocation.KULKAS)
        )

        // Query "Sayur" harus mencocokkan Wortel karena label kategorinya "Sayur & Buah"
        val result = PantryFilter.apply(items, StorageLocation.KULKAS, "sayur", ChipFilter.None, SortOrder.NAME_ASC, todayEpochDay)
        assertEquals(1, result.size)
        assertEquals("Wortel", result.first().name)
    }

    // ==================== 8. PENGURUTAN (SORT ORDER) ====================

    @Test
    fun apply_sortOrders() {
        val items = listOf(
            createItem("1", "Pisang", daysOffset = 5, location = StorageLocation.KULKAS),
            createItem("2", "Apel", daysOffset = 1, location = StorageLocation.KULKAS),
            createItem("3", "Ceri", daysOffset = 10, location = StorageLocation.KULKAS)
        )

        val soon = PantryFilter.apply(items, StorageLocation.KULKAS, "", ChipFilter.None, SortOrder.EXPIRING_SOON, todayEpochDay)
        assertEquals(listOf("Apel", "Pisang", "Ceri"), soon.map { it.name })

        val later = PantryFilter.apply(items, StorageLocation.KULKAS, "", ChipFilter.None, SortOrder.EXPIRING_LATER, todayEpochDay)
        assertEquals(listOf("Ceri", "Pisang", "Apel"), later.map { it.name })

        val nameAsc = PantryFilter.apply(items, StorageLocation.KULKAS, "", ChipFilter.None, SortOrder.NAME_ASC, todayEpochDay)
        assertEquals(listOf("Apel", "Ceri", "Pisang"), nameAsc.map { it.name })
    }

    // ==================== 9. KOMBINASI LOKASI + CHIP + QUERY + SORT ====================

    @Test
    fun apply_combinedFilters() {
        val items = listOf(
            createItem("1", "Susu Cokelat", location = StorageLocation.KULKAS, category = FoodCategory.SUSU_TELUR, daysOffset = 1),
            createItem("2", "Susu Putih", location = StorageLocation.KULKAS, category = FoodCategory.SUSU_TELUR, daysOffset = 10),
            createItem("3", "Bayam Hijau", location = StorageLocation.KULKAS, category = FoodCategory.SAYUR_BUAH, daysOffset = 1),
            createItem("4", "Susu Bubuk", location = StorageLocation.FREEZER, category = FoodCategory.SUSU_TELUR, daysOffset = 1)
        )

        // Lokasi Kulkas + Chip Expiring + Query "Susu"
        val result = PantryFilter.apply(
            items = items,
            location = StorageLocation.KULKAS,
            query = "susu",
            chip = ChipFilter.Expiring,
            sortOrder = SortOrder.EXPIRING_SOON,
            todayEpochDay = todayEpochDay
        )

        assertEquals(1, result.size)
        assertEquals("Susu Cokelat", result.first().name)
    }
}
