// [Materi: Recipe Matcher & Ingredient Selector] Layar pemilih bahan untuk racik rekomendasi resep instan (Tombol + di tengah)
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
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.data.model.Recipe
import com.example.pantrick.data.model.RecipeCatalog
import com.example.pantrick.data.model.User
import com.example.pantrick.ui.component.HomeHeader
import com.example.pantrick.ui.viewmodel.PantryViewModel
import java.util.Locale

// [Materi: Recipe Matching Helper] Fungsi pemanggilan pencocokan resep yang terpusat agar mudah diganti jika dataset diperbarui kelak
fun findMatchingRecipes(selected: Set<String>): List<Recipe> {
    if (selected.isEmpty()) return emptyList()

    return RecipeCatalog.recipes.mapNotNull { template ->
        val readyKeywords = template.ingredientKeywords.filter { keyword ->
            selected.any { sel -> sel.contains(keyword, ignoreCase = true) || keyword.contains(sel, ignoreCase = true) }
        }
        val missingKeywords = template.ingredientKeywords.filterNot { keyword ->
            selected.any { sel -> sel.contains(keyword, ignoreCase = true) || keyword.contains(sel, ignoreCase = true) }
        }
        val matchPercent = if (template.ingredientKeywords.isNotEmpty()) {
            (readyKeywords.size * 100) / template.ingredientKeywords.size
        } else 0

        if (matchPercent > 0) {
            val missingText = if (missingKeywords.isNotEmpty()) {
                "${missingKeywords.size} bahan kurang"
            } else {
                "0 bahan kurang (Lengkap)"
            }

            Recipe(
                id = template.id,
                title = template.title,
                description = template.description,
                durationMinutes = template.durationMinutes,
                servings = template.servings,
                matchPercent = matchPercent,
                usesLabel = "Menggunakan ${readyKeywords.joinToString { kw -> kw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } }}",
                readyCount = readyKeywords.size,
                totalCount = template.ingredientKeywords.size,
                missingIngredient = missingText,
                imageRes = template.imageRes
            )
        } else null
    }.sortedByDescending { it.matchPercent }
}

@Composable
fun AddIngredientScreen(
    currentUser: User?,
    pantryViewModel: PantryViewModel? = null,
    photoPath: String? = null,
    onNavigateBack: () -> Unit = {},
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onNavigateToRecipeDetail: (String) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedIngredients by remember { mutableStateOf(setOf<String>()) }
    var matchedRecipes by remember { mutableStateOf<List<Recipe>?>(null) }
    var hasSearched by remember { mutableStateOf(false) }

    val userNameSafe = currentUser?.fullName ?: "Pengguna"

    // [Materi: Static Ingredient Catalog] Sumber daftar saran bahan yang diambil dari kata kunci RecipeCatalog
    val availableIngredients = remember {
        RecipeCatalog.recipes
            .flatMap { it.ingredientKeywords }
            .distinct()
            .map { keyword ->
                keyword.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                }
            }
            .sorted()
    }

    val filteredSuggestions = remember(searchQuery, selectedIngredients) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            availableIngredients.filter {
                it.contains(searchQuery.trim(), ignoreCase = true) && !selectedIngredients.contains(it)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSoftCream)
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. TOP APP BAR & HEADER (Logo di pojok kiri, tidak ada tombol back, notifikasi & profil di kanan)
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

                // KOTAK PENCARIAN BAHAN DENGAN SARAN
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Ketik nama bahan (mis. Ayam, Bawang, Telur)...",
                            fontSize = 13.sp,
                            color = ColorPlaceholder
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Search,
                            contentDescription = null,
                            tint = ColorTextSubtitleBrown
                        )
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

                // DAFTAR SARAN BAHAN YANG COCOK
                if (searchQuery.isNotBlank()) {
                    if (filteredSuggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
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
                            }
                        }
                    } else {
                        Text(
                            text = "Bahan '$searchQuery' tidak ditemukan dalam daftar saran.",
                            fontSize = 12.sp,
                            color = ColorTextSubtitleBrown,
                            modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                        )
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
                                    modifier = Modifier.padding(
                                        start = 12.dp,
                                        end = 6.dp,
                                        top = 4.dp,
                                        bottom = 4.dp
                                    ),
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
                                        onClick = {
                                            selectedIngredients = selectedIngredients - ingredient
                                        },
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

                // TOMBOL CARI RESEP (Aktif jika minimal 1 bahan dipilih)
                Button(
                    onClick = {
                        hasSearched = true
                        matchedRecipes = findMatchingRecipes(selectedIngredients)
                    },
                    enabled = selectedIngredients.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorForestGreen,
                        disabledContainerColor = ColorDarkChocolate.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
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

        // 3. BAGIAN HASIL KECOCOKAN RESEP
        if (hasSearched) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = "Rekomendasi Resep",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ColorDarkChocolate
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val recipes = matchedRecipes
                    if (recipes.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Tidak ada resep yang cocok dengan bahan yang kamu pilih.",
                                    fontSize = 13.sp,
                                    color = ColorTextSubtitleBrown,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            recipes.forEach { recipe ->
                                RecipeMatchCard(
                                    title = recipe.title,
                                    subtitle = recipe.usesLabel,
                                    matchPercentage = "${recipe.matchPercent}% Cocok",
                                    missingInfo = recipe.missingIngredient,
                                    timeText = "${recipe.durationMinutes} mnt",
                                    calorieText = "${recipe.servings} porsi",
                                    matchColor = if (recipe.matchPercent >= 70) ColorForestGreen else ColorWarmPeach,
                                    onCookNowClick = { onNavigateToRecipeDetail(recipe.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RecipeMatchCard(
    title: String,
    subtitle: String,
    matchPercentage: String,
    missingInfo: String,
    timeText: String,
    calorieText: String,
    matchColor: Color,
    isAltButton: Boolean = false,
    isQuickBlend: Boolean = false,
    onCookNowClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(ColorWarmPeach),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = ColorDarkChocolate)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = ColorDarkChocolate
                        )
                        Text(
                            text = subtitle,
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
                        text = matchPercentage,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (matchColor == ColorForestGreen) ColorForestGreen else ColorDarkChocolate,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = missingInfo,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ColorTextSubtitleBrown
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = ColorTextSubtitleBrown,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(timeText, fontSize = 11.sp, color = ColorTextSubtitleBrown)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.People,
                            contentDescription = null,
                            tint = ColorTextSubtitleBrown,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(calorieText, fontSize = 11.sp, color = ColorTextSubtitleBrown)
                    }
                }

                Button(
                    onClick = onCookNowClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isQuickBlend) ColorWarmPeach else if (isAltButton) ColorWarmPeach else ColorForestGreen
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (isQuickBlend) "⚡ Blender Cepat" else if (isAltButton) "Lihat Resep" else "Masak Sekarang →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorSurfaceWhite
                    )
                }
            }
        }
    }
}