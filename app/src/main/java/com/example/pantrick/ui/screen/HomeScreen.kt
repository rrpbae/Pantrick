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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.Recipe
import com.example.pantrick.data.model.RecipeCatalog
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.data.model.User
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
    var isFavorite by rememberSaveable { mutableStateOf(false) }

    val fullName = currentUser?.fullName.orEmpty().ifBlank { "Pengguna" }
    val firstName = fullName.trim().split("\\s+".toRegex()).firstOrNull()?.ifBlank { "Pengguna" } ?: "Pengguna"

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val greeting = remember(currentHour, firstName) {
        when (currentHour) {
            in 4..10 -> "Selamat pagi, $firstName!"
            in 11..14 -> "Selamat siang, $firstName!"
            in 15..17 -> "Selamat sore, $firstName!"
            else -> "Selamat malam, $firstName!"
        }
    }

    val totalCount = items.size
    val expiringCount = items.count { it.daysLeft <= PantrickConstants.EXPIRING_THRESHOLD_DAYS }
    val hasAnyExpiring = expiringCount > 0

    // Filter bahan mendesak secara keseluruhan tanpa chip kategori beranda
    val filteredExpiringItems = remember(items) {
        items.filter { it.daysLeft <= PantrickConstants.EXPIRING_THRESHOLD_DAYS }.sortedBy { it.daysLeft }
    }

    val matchedRecipe = remember(items) {
        RecipeCatalog.findBestMatch(items, RecipeCatalog.MIN_RECIPE_MATCH_PERCENT)
    }

    StatelessHomeContent(
        userName = fullName,
        greeting = greeting,
        totalCount = totalCount,
        expiringCount = expiringCount,
        expiringItems = filteredExpiringItems,
        hasAnyExpiringItemsOverall = hasAnyExpiring,
        recipe = matchedRecipe,
        isFavorite = isFavorite,
        isPantryEmpty = items.isEmpty(),
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

@Composable
fun StatelessHomeContent(
    userName: String,
    greeting: String,
    totalCount: Int,
    expiringCount: Int,
    expiringItems: List<PantryItem>,
    hasAnyExpiringItemsOverall: Boolean,
    recipe: Recipe?,
    isFavorite: Boolean,
    isPantryEmpty: Boolean,
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
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = contentPadding
    ) {
        // 1. Header Pantrick
        item {
            HomeHeader(
                userName = userName,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick
            )
        }

        item { Spacer(modifier = Modifier.height(6.dp)) }

        // 2. Kartu Sapaan Hijau
        item {
            GreetingCard(
                greeting = greeting,
                totalCount = totalCount,
                expiringCount = expiringCount
            )
        }

        item { Spacer(modifier = Modifier.height(14.dp)) }

        // 3. Dua Kartu Ringkasan
        item {
            SummaryCardsRow(
                totalCount = totalCount,
                expiringCount = expiringCount
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Filter kategori (Semua, Kulkas, Freezer, Rak Kering) telah dihapus sesuai permintaan.

        // [Materi: Conditional UI / Empty State vs Content]
        if (isPantryEmpty) {
            item {
                EmptyPantryState(
                    onAddIngredientClick = onNavigateToAdd
                )
            }
        } else {
            item {
                NeedsAttentionSection(
                    items = expiringItems,
                    hasAnyExpiringItemsOverall = hasAnyExpiringItemsOverall,
                    onViewAllClick = onViewAllAttentionClick,
                    onFindRecipeClick = onFindRecipeClick
                )
            }

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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    PantrickTheme {
        StatelessHomeContent(
            userName = "Budi Santoso",
            greeting = "Selamat pagi, Budi!",
            totalCount = 3,
            expiringCount = 2,
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

@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenEmptyPreview() {
    PantrickTheme(darkTheme = true) {
        StatelessHomeContent(
            userName = "Siti Rahma",
            greeting = "Selamat malam, Siti!",
            totalCount = 0,
            expiringCount = 0,
            expiringItems = emptyList(),
            hasAnyExpiringItemsOverall = false,
            recipe = null,
            isFavorite = false,
            isPantryEmpty = true,
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