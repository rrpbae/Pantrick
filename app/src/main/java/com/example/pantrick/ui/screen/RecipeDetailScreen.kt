package com.example.pantrick.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.Recipe
import com.example.pantrick.data.model.RecipeCatalog
import com.example.pantrick.util.PantrickConstants

@Composable
fun RecipeDetailScreen(
    recipeId: String,
    savedRecipes: List<Recipe> = emptyList(),
    pantryItems: List<PantryItem> = emptyList(),
    contentPadding: PaddingValues = PaddingValues(),
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // 1. CARI RESEP BERDASARKAN ID (Dari resep tersimpan terlebih dahulu, lalu katalog fallback)
    val recipe = remember(recipeId, savedRecipes, pantryItems) {
        savedRecipes.find { it.id == recipeId }
            ?: RecipeCatalog.findBestMatch(pantryItems)?.takeIf { it.id == recipeId }
            ?: RecipeCatalog.recipes.find { it.id == recipeId }?.let { template ->
                val readyKeywords = template.ingredientKeywords.filter { keyword ->
                    pantryItems.any { item -> item.name.contains(keyword, ignoreCase = true) }
                }
                val missingKeywords = template.ingredientKeywords.filterNot { keyword ->
                    pantryItems.any { item -> item.name.contains(keyword, ignoreCase = true) }
                }
                val matchPercent = if (template.ingredientKeywords.isNotEmpty()) {
                    (readyKeywords.size * 100) / template.ingredientKeywords.size
                } else 0
                val missingText = if (missingKeywords.isNotEmpty()) {
                    "${missingKeywords.size} bahan kurang"
                } else {
                    "Lengkap"
                }
                Recipe(
                    id = template.id,
                    title = template.title,
                    description = template.description,
                    durationMinutes = template.durationMinutes,
                    servings = template.servings,
                    matchPercent = matchPercent,
                    usesLabel = "Cocok dengan bahanmu",
                    readyCount = readyKeywords.size,
                    totalCount = template.ingredientKeywords.size,
                    missingIngredient = missingText,
                    imageRes = template.imageRes
                )
            }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSoftCream)
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. TOP APP BAR (Header lokal: Back di kiri, Logo Pantrick di kanan, tanpa notifikasi & profil)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Kembali",
                        tint = ColorDarkChocolate
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_pantrick),
                        contentDescription = stringResource(R.string.cd_logo),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(PantrickConstants.LOGO_SIZE_SMALL)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorForestGreen
                    )
                }
            }
        }

        // JIKA RESEP TIDAK DITEMUKAN
        if (recipe == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Resep tidak ditemukan.",
                        fontSize = 15.sp,
                        color = ColorTextSubtitleBrown,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // 2. HERO IMAGE & OVERLAYS
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(220.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(ColorPlaceholder.copy(alpha = 0.3f))
                ) {
                    Image(
                        painter = painterResource(id = recipe.imageRes),
                        contentDescription = recipe.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Surface(
                        color = (if (recipe.matchPercent >= 70) ColorForestGreen else ColorWarmPeach).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(12.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Eco,
                                contentDescription = null,
                                tint = if (recipe.matchPercent >= 70) ColorSurfaceWhite else ColorDarkChocolate,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val matchDetail = if (recipe.totalCount > 0) {
                                "${recipe.matchPercent}% Cocok • ${recipe.readyCount} dari ${recipe.totalCount} bahan tersedia"
                            } else {
                                "${recipe.matchPercent}% Cocok"
                            }
                            Text(
                                text = matchDetail,
                                color = if (recipe.matchPercent >= 70) ColorSurfaceWhite else ColorDarkChocolate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 3. TITLE & DESCRIPTION (HANYA DARI MODEL RESEP)
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = recipe.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate,
                        lineHeight = 28.sp
                    )
                    if (recipe.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = recipe.description,
                            fontSize = 13.sp,
                            color = ColorTextSubtitleBrown,
                            lineHeight = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 4. STATS ROW (HANYA WAKTU DAN PORSI DARI MODEL RESEP)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Timer,
                        label = "WAKTU",
                        value = "${recipe.durationMinutes} mnt"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.People,
                        label = "PORSI",
                        value = "${recipe.servings} porsi"
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 5. STATUS KECOCOKAN BAHAN (Dari pantry pengguna)
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = "Kecocokan Bahan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = ColorDarkChocolate
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (recipe.usesLabel.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Rounded.CheckCircleOutline,
                                        contentDescription = null,
                                        tint = ColorForestGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = recipe.usesLabel,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ColorForestGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = if (recipe.missingIngredient.contains("Lengkap", ignoreCase = true) || recipe.missingIngredient.startsWith("0")) ColorForestGreen else ColorUrgencyRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (recipe.missingIngredient.contains("Lengkap", ignoreCase = true) || recipe.missingIngredient.startsWith("0")) {
                                        "Semua bahan utama ada di dapurmu"
                                    } else {
                                        "Bahan kurang: ${recipe.missingIngredient}"
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (recipe.missingIngredient.contains("Lengkap", ignoreCase = true) || recipe.missingIngredient.startsWith("0")) ColorForestGreen else ColorUrgencyRed
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
fun StatCard(modifier: Modifier, icon: ImageVector, label: String, value: String) {
    Card(
        modifier = modifier.defaultMinSize(minHeight = 76.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorTextSubtitleBrown, letterSpacing = 0.5.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = ColorDarkChocolate, textAlign = TextAlign.Center, lineHeight = 16.sp)
        }
    }
}