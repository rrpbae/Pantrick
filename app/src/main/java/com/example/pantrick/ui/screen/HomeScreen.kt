// [Materi: Compose Screen & Architecture] Layar utama aplikasi Pantrick dengan data real-time per user
// Menerapkan pemisahan Stateful vs Stateless, Unidirectional Data Flow, dan LazyColumn efisien
package com.example.pantrick.ui.screen

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Build
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.ui.draw.blur
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.ColorDarkChocolate
import com.example.pantrick.core.ui.theme.ColorForestGreen
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.Recipe
import com.example.pantrick.data.model.RecipeCatalog
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.data.model.User
import com.example.pantrick.ui.component.EmptyPantryState
import com.example.pantrick.ui.component.ExpiringItemCard
import com.example.pantrick.ui.component.GreetingCard
import com.example.pantrick.ui.component.HomeHeader
import com.example.pantrick.ui.component.IngredientDetailSheet
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
    photoPath: String? = null,
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

    // Bahan terbaru yang diinput user (maksimal 5 item terakhir, urut dari yang paling baru ditambahkan)
    val recentItems = remember(items) {
        items.sortedByDescending { it.id.toLongOrNull() ?: 0L }.take(5)
    }

    // Rekomendasi resep dihitung dari bahan yang ada di pantry pengguna
    val matchedRecipe = remember(items) {
        RecipeCatalog.findBestMatch(items, RecipeCatalog.MIN_RECIPE_MATCH_PERCENT)
    }

    var selectedItemId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedItem = remember(selectedItemId, items) { items.find { it.id == selectedItemId } }

    val blurRadius by animateDpAsState(
        targetValue = if (selectedItem != null) 14.dp else 0.dp,
        label = "HomeBlurAnimation"
    )

    StatelessHomeContent(
        userName = fullName,
        greeting = greeting,
        totalCount = totalCount,
        expiringCount = expiringCount,
        recentItems = recentItems,
        matchedRecipe = matchedRecipe,
        isFavorite = isFavorite,
        isPantryEmpty = items.isEmpty(),
        selectedItem = selectedItem,
        blurRadius = blurRadius,
        photoPath = photoPath,
        onFavoriteToggle = {
            isFavorite = !isFavorite
            Log.d(TAG, "Recipe favorite status toggled: $isFavorite")
        },
        onNotificationClick = onNotificationClick,
        onProfileClick = onProfileClick,
        onNavigateToAdd = onNavigateToAdd,
        onViewAllAttentionClick = onNavigateToPantry,
        onDetailClick = { item ->
            selectedItemId = item.id
        },
        onDismissDetail = {
            selectedItemId = null
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
    recentItems: List<PantryItem>,
    matchedRecipe: Recipe?,
    isFavorite: Boolean,
    isPantryEmpty: Boolean,
    selectedItem: PantryItem? = null,
    blurRadius: Dp = 0.dp,
    photoPath: String? = null,
    onFavoriteToggle: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onViewAllAttentionClick: () -> Unit,
    onDetailClick: (PantryItem) -> Unit = {},
    onFindRecipeClick: (PantryItem) -> Unit = onDetailClick,
    onDismissDetail: () -> Unit = {},
    onCookNowClick: () -> Unit,
    onPlanMealClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val blurModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && blurRadius > 0.dp) {
        Modifier.blur(blurRadius)
    } else {
        Modifier
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .then(blurModifier)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = contentPadding
        ) {
            // 1. Header Pantrick
            item {
                HomeHeader(
                    userName = userName,
                    photoPath = photoPath,
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

            // [Materi: Conditional UI / Empty State vs Content]
            if (isPantryEmpty) {
                item {
                    EmptyPantryState(
                        onAddIngredientClick = onNavigateToAdd
                    )
                }
            } else {
                // 4. Section Bahan Terbaru (scroll horizontal, max 5)
                item {
                    RecentIngredientsSection(
                        items = recentItems,
                        onViewAllClick = onViewAllAttentionClick,
                        onDetailClick = onDetailClick
                    )
                }

            // 5. Section "Smart Recipe Pairings" (hanya jika ada resep yang cocok)
            if (matchedRecipe != null) {
                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    RecipePairingSection(
                        recipe = matchedRecipe,
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

    // Pop up Bottom Sheet Detail Bahan
    selectedItem?.let { item ->
        IngredientDetailSheet(
            item = item,
            onDismissRequest = onDismissDetail
        )
    }
}
}

// Komponen deretan kartu bahan terbaru secara horizontal
@Composable
fun RecentIngredientsSection(
    items: List<PantryItem>,
    onViewAllClick: () -> Unit,
    onDetailClick: (PantryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ColorForestGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bahan Terbaru",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorDarkChocolate
                )
            }

            Text(
                text = "Lihat Semua ›",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorForestGreen,
                modifier = Modifier.clickable { onViewAllClick() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada bahan di pantry.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    ExpiringItemCard(
                        item = item,
                        onFindRecipeClick = { onDetailClick(item) },
                        buttonText = stringResource(R.string.btn_ingredient_detail),
                        buttonIcon = Icons.Rounded.Info
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    val sampleItems = listOf(
        PantryItem("1", "Susu Segar", StorageLocation.KULKAS, "500 ml", LocalDate.now().plusDays(1).toEpochDay()),
        PantryItem("2", "Daging Ayam", StorageLocation.FREEZER, "500 gr", LocalDate.now().plusDays(2).toEpochDay()),
        PantryItem("3", "Telur Ayam", StorageLocation.KULKAS, "6 butir", LocalDate.now().plusDays(10).toEpochDay())
    )
    val sampleRecipe = RecipeCatalog.recipes.firstOrNull()?.let {
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
    }
    PantrickTheme {
        StatelessHomeContent(
            userName = "Budi Santoso",
            greeting = "Selamat pagi, Budi!",
            totalCount = sampleItems.size,
            expiringCount = 2,
            recentItems = sampleItems,
            matchedRecipe = sampleRecipe,
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
            recentItems = emptyList(),
            matchedRecipe = null,
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