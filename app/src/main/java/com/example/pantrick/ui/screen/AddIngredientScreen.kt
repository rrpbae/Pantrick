// [Materi: Recipe Matcher & Ingredient Selector] Layar pemilih bahan pantry untuk generate resep instan sesuai desain Figma Haydar
package com.example.pantrick.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.User
import com.example.pantrick.ui.component.HomeHeader
import com.example.pantrick.ui.viewmodel.PantryViewModel

@Composable
fun AddIngredientScreen(
    currentUser: User?,
    pantryViewModel: PantryViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToRecipeDetail: (String) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedItemIds by remember { mutableStateOf(setOf<String>()) }

    val pantryItemsState by pantryViewModel.items.collectAsState()
    val userNameSafe = currentUser?.fullName ?: "Pengguna"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSoftCream)
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. TOP APP BAR & HEADER
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali", tint = ColorDarkChocolate)
                }
                Box(modifier = Modifier.weight(1f)) {
                    HomeHeader(
                        userName = userNameSafe,
                        onNotificationClick = { /* TODO */ },
                        onProfileClick = { /* TODO */ }
                    )
                }
            }
        }

        // 2. JUDUL & SUBJUDUL
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
                    text = "Pilih bahan makanan yang ada di dapurmu untuk meracik hidangan instan bebas sisa.",
                    fontSize = 12.sp,
                    color = ColorTextSubtitleBrown,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                // KOTAK PENCARIAN
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari dari ${pantryItemsState.size} bahan di pantry...", fontSize = 13.sp, color = ColorPlaceholder) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = ColorTextSubtitleBrown) },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = ColorSurfaceWhite,
                        focusedContainerColor = ColorSurfaceWhite,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = ColorForestGreen
                    ),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // TOMBOL FILTER CHIPS (Ditambahkan Rak Kering)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChipItem(text = "Semua (${pantryItemsState.size})", isSelected = true, onClick = {}) }
                    item { FilterChipItem(text = "Segera Kedaluwarsa (3)", isSelected = false, onClick = {}) }
                    item { FilterChipItem(text = "Kulkas", isSelected = false, onClick = {}) }
                    item { FilterChipItem(text = "Freezer", isSelected = false, onClick = {}) }
                    item { FilterChipItem(text = "Rak Kering", isSelected = false, onClick = {}) }
                }
            }
        }

        // 3. BAGIAN PEMILIHAN PANTRY
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pilihan Bahan Pantry",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ColorDarkChocolate
                    )
                    // Tombol "Pilih Otomatis (Segera Habis)" sudah dihapus sesuai permintaan
                    Surface(
                        color = ColorWarmPeach,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${selectedItemIds.size} dipilih",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorDarkChocolate,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (pantryItemsState.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ColorSurfaceWhite, RoundedCornerShape(16.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada bahan di Pantry. Tambahkan melalui menu Pantry!", fontSize = 12.sp, color = ColorTextSubtitleBrown)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        pantryItemsState.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { item ->
                                    val isSelected = selectedItemIds.contains(item.id)
                                    PantrySelectionCard(
                                        item = item,
                                        isSelected = isSelected,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            selectedItemIds = if (isSelected) {
                                                selectedItemIds - item.id
                                            } else {
                                                selectedItemIds + item.id
                                            }
                                        }
                                    )
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. BAGIAN KECOCOKAN RESEP INSTAN
        item {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Kecocokan Resep Instan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ColorDarkChocolate
                        )
                        Text(
                            text = "3 hidangan siap dimasak sekarang",
                            fontSize = 11.sp,
                            color = ColorTextSubtitleBrown
                        )
                    }
                    // Badge "Siap" di kanan atas sudah dihapus sesuai permintaan
                }

                Spacer(modifier = Modifier.height(12.dp))

                RecipeMatchCard(
                    title = "Pasta Krim Bawang Putih & Herbal",
                    subtitle = "Menggunakan Susu, Bayam, Pasta &...",
                    matchPercentage = "100% Cocok",
                    missingInfo = "0 bahan kurang",
                    timeText = "20 mnt",
                    calorieText = "420 kkal",
                    matchColor = ColorForestGreen,
                    onCookNowClick = { onNavigateToRecipeDetail("recipe_1") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                RecipeMatchCard(
                    title = "Frittata Klasik Bayam & Telur",
                    subtitle = "Menggunakan Telur & Bayam...",
                    matchPercentage = "90% Cocok",
                    missingInfo = "1 bahan opsional kurang",
                    timeText = "15 mnt",
                    calorieText = "Tinggi Protein",
                    matchColor = ColorWarmPeach,
                    isAltButton = true,
                    onCookNowClick = { onNavigateToRecipeDetail("recipe_2") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                RecipeMatchCard(
                    title = "Smoothie Segar Apel & Susu",
                    subtitle = "Memadukan Susu dengan Apel...",
                    matchPercentage = "Minuman • 100%",
                    missingInfo = "Siap dalam 5m",
                    timeText = "5 mnt persiapan",
                    calorieText = "Blender Cepat",
                    matchColor = ColorForestGreen,
                    isQuickBlend = true,
                    onCookNowClick = { onNavigateToRecipeDetail("recipe_3") }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToRecipeDetail("generated_batch") },
                    colors = CardDefaults.cardColors(containerColor = ColorDarkChocolate),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = ColorForestGreen,
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.RestaurantMenu, contentDescription = null, tint = ColorSurfaceWhite, modifier = Modifier.size(18.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Buat 5 Resep Lainnya", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ColorSurfaceWhite)
                                Text("Dicocokkan dengan ${selectedItemIds.size.coerceAtLeast(1)} bahan terpilih", fontSize = 11.sp, color = ColorSurfaceWhite.copy(alpha = 0.7f))
                            }
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = ColorSurfaceWhite)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) ColorForestGreen else ColorSurfaceWhite,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) ColorSurfaceWhite else ColorDarkChocolate,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun PantrySelectionCard(
    item: PantryItem,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) BorderStroke(2.dp, ColorForestGreen) else null,
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    color = ColorSoftCream,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Kitchen, contentDescription = null, tint = ColorDarkChocolate, modifier = Modifier.size(14.dp))
                    }
                }
                Surface(
                    color = ColorWarmPeach.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "1hr lagi",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ColorDarkChocolate,
                maxLines = 1
            )
            Text(
                text = "${item.quantityLabel} di ${item.location.label}",
                fontSize = 10.sp,
                color = ColorTextSubtitleBrown,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isSelected) Icons.Rounded.CheckCircle else Icons.Rounded.AddCircleOutline,
                    contentDescription = null,
                    tint = if (isSelected) ColorForestGreen else ColorTextSubtitleBrown,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSelected) "Dalam wadah" else "Ketuk untuk pilih",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) ColorForestGreen else ColorTextSubtitleBrown
                )
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
            Divider(color = ColorSoftCream, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Schedule, contentDescription = null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(timeText, fontSize = 11.sp, color = ColorTextSubtitleBrown)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(14.dp))
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
                        color = ColorDarkChocolate
                    )
                }
            }
        }
    }
}