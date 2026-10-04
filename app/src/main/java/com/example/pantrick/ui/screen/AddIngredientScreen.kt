// [Materi: Recipe Matcher & Ingredient Selector] Layar pemilih bahan untuk rekomendasi resep dari backend dataset (Tombol + di tengah)
package com.example.pantrick.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pantrick.core.network.PantrickApiConfig
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.data.model.BackendRecommendation
import com.example.pantrick.data.model.User
import com.example.pantrick.ui.component.HomeHeader
import com.example.pantrick.ui.viewmodel.IngredientSearchViewModel
import com.example.pantrick.ui.viewmodel.SearchUiState
import com.example.pantrick.ui.viewmodel.PantryViewModel

@Composable
fun AddIngredientScreen(
    currentUser: User?,
    pantryViewModel: PantryViewModel? = null,
    savedRecipes: List<com.example.pantrick.data.model.Recipe> = emptyList(),
    onToggleSaveRecipe: (com.example.pantrick.data.model.Recipe) -> Unit = {},
    photoPath: String? = null,
    onNavigateBack: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNavigateToRecipeDetail: (String) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    val searchViewModel: IngredientSearchViewModel = viewModel()
    val searchState by searchViewModel.searchState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedIngredients by remember { mutableStateOf(setOf<String>()) }

    // Reset state ketika meninggalkan layar
    DisposableEffect(Unit) {
        onDispose { searchViewModel.resetSearch() }
    }

    val userNameSafe = currentUser?.fullName ?: "Pengguna"

    // Saran bahan populer (Bahasa Inggris, sesuai dataset) untuk mempermudah input
    val popularSuggestions = remember {
        listOf(
            "Chicken", "Beef", "Egg", "Onion", "Garlic", "Tomato", "Potato",
            "Rice", "Pasta", "Butter", "Milk", "Cheese", "Flour", "Sugar",
            "Salt", "Oil", "Carrot", "Spinach", "Mushroom", "Shrimp"
        ).sorted()
    }

    val filteredSuggestions = remember(searchQuery, selectedIngredients) {
        if (searchQuery.isBlank()) emptyList()
        else popularSuggestions.filter {
            it.contains(searchQuery.trim(), ignoreCase = true) &&
                    !selectedIngredients.any { sel -> sel.equals(it, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSoftCream)
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. TOP APP BAR & HEADER
        item {
            HomeHeader(
                userName = userNameSafe,
                photoPath = photoPath,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick
            )
        }

        // 2. JUDUL & SUBJUDUL & SEARCH BAHAN
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                Text(
                    text = "Ada apa di wadahmu hari ini?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = ColorDarkChocolate
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Pilih atau ketik bahan makanan yang ingin kamu masak untuk meracik rekomendasi resep instan.",
                    fontSize = 12.sp,
                    color = ColorTextSubtitleBrown,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                // KOTAK PENCARIAN BAHAN
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Ketik nama bahan (mis. Chicken, Onion, Cheese)...",
                            fontSize = 13.sp,
                            color = ColorPlaceholder
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = ColorTextSubtitleBrown)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Hapus teks",
                                    tint = ColorTextSubtitleBrown,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = ColorSurfaceWhite,
                        focusedContainerColor = ColorSurfaceWhite,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = ColorForestGreen
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                // DAFTAR SARAN BAHAN
                if (searchQuery.isNotBlank()) {
                    if (filteredSuggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column {
                                filteredSuggestions.take(5).forEach { suggestion ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedIngredients = selectedIngredients + suggestion
                                                searchQuery = ""
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = null,
                                            tint = ColorForestGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = suggestion,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = ColorDarkChocolate
                                        )
                                    }
                                }
                                // Opsi: tambah teks input langsung walau tidak ada di saran
                                if (!filteredSuggestions.any { it.equals(searchQuery.trim(), ignoreCase = true) } && searchQuery.trim().length >= 2) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedIngredients = selectedIngredients + searchQuery.trim()
                                                searchQuery = ""
                                            }
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Add,
                                            contentDescription = null,
                                            tint = ColorTextSubtitleBrown,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Tambah \"${searchQuery.trim()}\"",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = ColorTextSubtitleBrown
                                        )
                                    }
                                }
                            }
                        }
                    } else if (searchQuery.trim().length >= 2) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedIngredients = selectedIngredients + searchQuery.trim()
                                        searchQuery = ""
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = ColorForestGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Tambah \"${searchQuery.trim()}\"",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorDarkChocolate
                                )
                            }
                        }
                    }
                }

                // CHIP BAHAN YANG DIPILIH
                if (selectedIngredients.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bahan yang dipilih (${selectedIngredients.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        selectedIngredients.forEach { ingredient ->
                            Surface(
                                color = ColorSurfaceWhite,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, ColorForestGreen.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ingredient,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ColorDarkChocolate
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { selectedIngredients = selectedIngredients - ingredient },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Hapus $ingredient",
                                            tint = ColorTextSubtitleBrown,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // TOMBOL CARI RESEP
                Button(
                    onClick = {
                        searchViewModel.searchRecommendations(selectedIngredients)
                    },
                    enabled = selectedIngredients.isNotEmpty() && searchState !is SearchUiState.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorForestGreen,
                        disabledContainerColor = ColorDarkChocolate.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    if (searchState is SearchUiState.Loading) {
                        CircularProgressIndicator(
                            color = ColorSurfaceWhite,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mencari resep...",
                            color = ColorSurfaceWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = ColorSurfaceWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cari Resep",
                            color = ColorSurfaceWhite,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 3. BAGIAN HASIL REKOMENDASI DARI BACKEND
        when (val state = searchState) {
            is SearchUiState.Idle -> { /* Belum ada pencarian */ }

            is SearchUiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = ColorForestGreen)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Mencari resep dari dataset...",
                                fontSize = 13.sp,
                                color = ColorTextSubtitleBrown,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            is SearchUiState.Error -> {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Text(
                            text = "Rekomendasi Resep",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ColorDarkChocolate
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Rounded.WifiOff,
                                    contentDescription = null,
                                    tint = ColorUrgencyRed,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    fontSize = 13.sp,
                                    color = ColorTextSubtitleBrown,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            is SearchUiState.Success -> {
                item {
                    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        Text(
                            text = "Rekomendasi Resep",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = ColorDarkChocolate
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (state.recommendations.isNotEmpty()) {
                            Text(
                                text = "${state.recommendations.size} resep ditemukan dari dataset",
                                fontSize = 11.sp,
                                color = ColorTextSubtitleBrown
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (state.recommendations.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tidak ditemukan resep yang cocok dengan bahan tersebut.\nCoba ganti nama bahan ke Bahasa Inggris (mis. Chicken, Onion, Garlic).",
                                    fontSize = 13.sp,
                                    color = ColorTextSubtitleBrown,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                } else {
                    items(state.recommendations.size, key = { state.recommendations[it].recipe.id }) { index ->
                        val rec = state.recommendations[index]
                        BackendRecipeMatchCard(
                            recommendation = rec,
                            onCookNowClick = { onNavigateToRecipeDetail(rec.recipe.id) },
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

/**
 * Kartu hasil rekomendasi resep dari backend dataset.
 * Menampilkan data aktual: title, match%, bahan cocok, bahan kurang, dan gambar dari dataset.
 */
@Composable
fun BackendRecipeMatchCard(
    recommendation: BackendRecommendation,
    onCookNowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = recommendation.recipe
    val matchPct = recommendation.matchPercentage.toInt()
    val matchColor = if (matchPct >= 70) ColorForestGreen else ColorWarmPeach
    val imageUrl = if (recipe.hasImage && recipe.imageName != null) {
        "${PantrickApiConfig.BASE_URL}/api/recipes/${recipe.imageName}/image"
    } else null

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Gambar resep dari backend (atau placeholder jika tidak ada)
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorWarmPeach),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = recipe.title,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                error = null,
                                placeholder = null
                            )
                        } else {
                            Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = ColorDarkChocolate)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = recipe.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ColorDarkChocolate
                        )
                        Text(
                            text = "Menggunakan ${recommendation.matchedIngredients.take(2).joinToString(", ")}${if (recommendation.matchedIngredients.size > 2) "..." else ""}",
                            fontSize = 11.sp,
                            color = ColorTextSubtitleBrown
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = matchColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$matchPct% Cocok",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (matchColor == ColorForestGreen) ColorForestGreen else ColorDarkChocolate,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                val missingText = when {
                    recommendation.missingIngredientCount == 0 -> "Semua bahan ada!"
                    else -> "${recommendation.missingIngredientCount} bahan kurang"
                }
                Text(
                    text = missingText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (recommendation.missingIngredientCount == 0) ColorForestGreen else ColorTextSubtitleBrown
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = ColorSoftCream, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (recipe.cookingTimeMinutes != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Schedule, contentDescription = null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${recipe.cookingTimeMinutes} mnt", fontSize = 11.sp, color = ColorTextSubtitleBrown)
                        }
                    }
                    if (recipe.servings != null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.People, contentDescription = null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${recipe.servings} porsi", fontSize = 11.sp, color = ColorTextSubtitleBrown)
                        }
                    }
                    // Tampilkan jumlah total bahan
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${recommendation.matchedCount}/${recommendation.totalIngredients} bahan", fontSize = 11.sp, color = ColorTextSubtitleBrown)
                    }
                }

                Button(
                    onClick = onCookNowClick,
                    colors = ButtonDefaults.buttonColors(containerColor = ColorForestGreen),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Masak Sekarang →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorSurfaceWhite
                    )
                }
            }
        }
    }
}