// [Materi: Compose Screen & Architecture] Layar utama aplikasi Pantrick dengan data real-time per user
// Menerapkan pemisahan Stateful vs Stateless, Unidirectional Data Flow, dan LazyColumn efisien
package com.example.pantrick.ui.screen

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.Recipe
import com.example.pantrick.data.model.RecipeCatalog
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.data.model.User
import com.example.pantrick.ui.component.CategoryFilterChips
import com.example.pantrick.ui.component.EmptyPantryState
import com.example.pantrick.ui.component.GreetingCard
import com.example.pantrick.ui.component.HomeHeader
import com.example.pantrick.ui.component.NeedsAttentionSection
import com.example.pantrick.ui.component.RecipePairingSection
import com.example.pantrick.ui.component.SummaryCardsRow
import com.example.pantrick.util.PantrickConstants
import java.time.LocalDate
import java.util.Calendar

private const val TAG = "PantrickHome"

// [Materi: Stateful Composable] Mengelola state UI individual, sapaan waktu Indonesia, dan filter data nyata
@Composable
fun HomeScreen(
    currentUser: User?,
    items: List<PantryItem>,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNavigateToAdd: () -> Unit = {},
    onNavigateToPantry: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    // [Materi: rememberSaveable per-field] Menyimpan kategori aktif dan status resep favorit
    var selectedCategory by rememberSaveable { mutableStateOf("Semua") }
    var isFavorite by rememberSaveable { mutableStateOf(false) }

    // [Materi: Parsing Nama Pengguna] Mengambil nama depan pengguna aktif
    val fullName = currentUser?.fullName.orEmpty().ifBlank { "Pengguna" }
    val firstName = fullName.trim().split("\\s+".toRegex()).firstOrNull()?.ifBlank { "Pengguna" } ?: "Pengguna"

    // [Materi: Perhitungan Jam Sapaan Waktu] 04.00-10.59 pagi, 11.00-14.59 siang, 15.00-17.59 sore, selain itu malam
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = remember(currentHour, firstName) {
        when (currentHour) {
            in 4..10 -> "Selamat pagi, $firstName!"
            in 11..14 -> "Selamat siang, $firstName!"
            in 15..17 -> "Selamat sore, $firstName!"
            else -> "Selamat malam, $firstName!"
        }
    }

    // [Materi: Derived State & Data Nyata] Menghitung statistik stok dari list bahan pengguna
    val totalCount = items.size
    val expiringCount = items.count { it.daysLeft <= PantrickConstants.EXPIRING_THRESHOLD_DAYS }
    val hasAnyExpiring = expiringCount > 0

    // [Materi: Filter Pasangan Kategori (Semua, Kulkas, Freezer, Rak Kering)]
    val categoriesWithCount = remember(items) {
        listOf(
            "Semua" to items.size,
            "Kulkas" to items.count { it.location == StorageLocation.KULKAS },
            "Freezer" to items.count { it.location == StorageLocation.FREEZER },
            "Rak Kering" to items.count { it.location == StorageLocation.RAK_KERING }
        )
    }

    // [Materi: Collection Filtering] Memfilter bahan mendesak berdasarkan lokasi aktif
    val filteredExpiringItems = remember(selectedCategory, items) {
        val urgentItems = items.filter { it.daysLeft <= PantrickConstants.EXPIRING_THRESHOLD_DAYS }
        when (selectedCategory) {
            "Kulkas" -> urgentItems.filter { it.location == StorageLocation.KULKAS }
            "Freezer" -> urgentItems.filter { it.location == StorageLocation.FREEZER }
            "Rak Kering" -> urgentItems.filter { it.location == StorageLocation.RAK_KERING }
            else -> urgentItems
        }.sortedBy { it.daysLeft }
    }

    // [Materi: Algoritma Pencocokan Resep] Menghitung resep terbaik dari bahan pengguna
    val matchedRecipe = remember(items) {
        RecipeCatalog.findBestMatch(items, RecipeCatalog.MIN_RECIPE_MATCH_PERCENT)
    }

    // [Materi: State Hoisting & Unidirectional Data Flow]
    StatelessHomeContent(
        userName = fullName,
        greeting = greeting,
        totalCount = totalCount,
        expiringCount = expiringCount,
        categoriesWithCount = categoriesWithCount,
        selectedCategory = selectedCategory,
        expiringItems = filteredExpiringItems,
        hasAnyExpiringItemsOverall = hasAnyExpiring,
        recipe = matchedRecipe,
        isFavorite = isFavorite,
        isPantryEmpty = items.isEmpty(),
        onCategorySelected = { category ->
            Log.d(TAG, "Category filter changed to: $category")
            selectedCategory = category
        },
        onFavoriteToggle = {
            isFavorite = !isFavorite
            Log.d(TAG, "Recipe favorite status toggled: $isFavorite")
        },
        onNotificationClick = onNotificationClick,
        onProfileClick = onProfileClick,
        onNavigateToAdd = onNavigateToAdd,
        onViewAllAttentionClick = onNavigateToPantry,
        onFindRecipeClick = { item ->
            Log.d(TAG, "Find recipe clicked for item: ${item.name}")
        },
        onCookNowClick = {
            Log.d(TAG, "Cook now clicked for recipe: ${matchedRecipe?.title}")
        },
        onPlanMealClick = {
            Log.d(TAG, "Plan meal clicked for recipe: ${matchedRecipe?.title}")
        },
        contentPadding = contentPadding,
        modifier = modifier
    )
}

// [Materi: Stateless Composable] Murni merender UI berdasarkan state dan event callback
@Composable
fun StatelessHomeContent(
    userName: String,
    greeting: String,
    totalCount: Int,
    expiringCount: Int,
    categoriesWithCount: List<Pair<String, Int>>,
    selectedCategory: String,
    expiringItems: List<PantryItem>,
    hasAnyExpiringItemsOverall: Boolean,
    recipe: Recipe?,
    isFavorite: Boolean,
    isPantryEmpty: Boolean,
    onCategorySelected: (String) -> Unit,
    onFavoriteToggle: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onViewAllAttentionClick: () -> Unit,
    onFindRecipeClick: (PantryItem) -> Unit,
    onCookNowClick: () -> Unit,
    onPlanMealClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    // [Materi: LazyColumn] Kontainer gulir vertikal utama
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = contentPadding
    ) {
        // 1. Header Pantrick (Logo, Wordmark, Lonceng, Avatar Inisial)
        item {
            HomeHeader(
                userName = userName,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick
            )
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        // 2. Kartu Sapaan Hijau (Greeting Card)
        item {
            GreetingCard(
                greeting = greeting,
                totalCount = totalCount,
                expiringCount = expiringCount
            )
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // 3. Dua Kartu Ringkasan (Total Bahan & Hampir Kedaluwarsa)
        item {
            SummaryCardsRow(
                totalCount = totalCount,
                expiringCount = expiringCount
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // 4. Barisan Chip Filter Kategori (Semua, Kulkas, Freezer)
        item {
            CategoryFilterChips(
                categoriesWithCount = categoriesWithCount,
                selectedCategory = selectedCategory,
                onCategorySelected = onCategorySelected
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // [Materi: Conditional UI / Empty State vs Content]
        if (isPantryEmpty) {
            // Kondisi Pantry Kosong: tampilkan Empty State ramah di bawah filter
            item {
                EmptyPantryState(
                    onAddIngredientClick = onNavigateToAdd
                )
            }
        } else {
            // Kondisi Pantry Berisi: tampilkan Section Perlu Diperhatikan
            item {
                NeedsAttentionSection(
                    items = expiringItems,
                    hasAnyExpiringItemsOverall = hasAnyExpiringItemsOverall,
                    onViewAllClick = onViewAllAttentionClick,
                    onFindRecipeClick = onFindRecipeClick
                )
            }

            // Section "Pasangan Resep Pintar" hanya muncul jika ada resep yang lolos ambang batas
            if (recipe != null) {
                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    RecipePairingSection(
                        recipe = recipe,
                        isFavorite = isFavorite,
                        onFavoriteToggle = onFavoriteToggle,
                        onViewAllClick = onViewAllAttentionClick,
                        onCookNowClick = onCookNowClick,
                        onPlanMealClick = onPlanMealClick
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

// [Materi: Preview Light] Pratinjau Home Screen mode terang dengan data user
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    PantrickTheme {
        StatelessHomeContent(
            userName = "Budi Santoso",
            greeting = "Selamat pagi, Budi!",
            totalCount = 3,
            expiringCount = 2,
            categoriesWithCount = listOf(
                "Semua" to 3,
                "Kulkas" to 2,
                "Freezer" to 1
            ),
            selectedCategory = "Semua",
            expiringItems = listOf(
                PantryItem("1", "Susu Segar", StorageLocation.KULKAS, "500 ml", LocalDate.now().plusDays(1).toEpochDay()),
                PantryItem("2", "Daging Ayam", StorageLocation.FREEZER, "500 gr", LocalDate.now().plusDays(2).toEpochDay())
            ),
            hasAnyExpiringItemsOverall = true,
            recipe = RecipeCatalog.recipes.firstOrNull()?.let {
                Recipe(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    durationMinutes = it.durationMinutes,
                    servings = it.servings,
                    matchPercent = 50,
                    usesLabel = "Cocok dengan bahanmu",
                    readyCount = 2,
                    totalCount = 4,
                    missingIngredient = "Keju",
                    imageRes = it.imageRes
                )
            },
            isFavorite = false,
            isPantryEmpty = false,
            onCategorySelected = {},
            onFavoriteToggle = {},
            onNotificationClick = {},
            onProfileClick = {},
            onNavigateToAdd = {},
            onViewAllAttentionClick = {},
            onFindRecipeClick = {},
            onCookNowClick = {},
            onPlanMealClick = {},
            contentPadding = PaddingValues(0.dp)
        )
    }
}

// [Materi: Preview Empty State] Pratinjau Home Screen saat kondisi user baru / pantry kosong
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenEmptyPreview() {
    PantrickTheme(darkTheme = true) {
        StatelessHomeContent(
            userName = "Siti Rahma",
            greeting = "Selamat malam, Siti!",
            totalCount = 0,
            expiringCount = 0,
            categoriesWithCount = listOf(
                "Semua" to 0,
                "Kulkas" to 0,
                "Freezer" to 0
            ),
            selectedCategory = "Semua",
            expiringItems = emptyList(),
            hasAnyExpiringItemsOverall = false,
            recipe = null,
            isFavorite = false,
            isPantryEmpty = true,
            onCategorySelected = {},
            onFavoriteToggle = {},
            onNotificationClick = {},
            onProfileClick = {},
            onNavigateToAdd = {},
            onViewAllAttentionClick = {},
            onFindRecipeClick = {},
            onCookNowClick = {},
            onPlanMealClick = {},
            contentPadding = PaddingValues(0.dp)
        )
    }
}
