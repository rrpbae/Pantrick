package com.example.pantrick.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    // 1. CARI RESEP BERDASARKAN ID
    // Prioritas: Ambil dari savedRecipes snapshot terlebih dahulu.
    // Jika dari snapshot lama yang belum punya ingredients/steps, isi dari RecipeCatalog.
    val recipe = remember(recipeId, savedRecipes, pantryItems) {
        val template = RecipeCatalog.recipes.find { it.id == recipeId }
        val saved = savedRecipes.find { it.id == recipeId }

        if (saved != null) {
            val finalIngredients = if (template != null && template.ingredients.isNotEmpty()) {
                template.ingredients
            } else {
                saved.ingredients
            }
            val finalSteps = if (template != null && template.steps.isNotEmpty()) {
                template.steps
            } else {
                saved.steps
            }
            val readyCount = if (finalIngredients.isNotEmpty()) {
                finalIngredients.count { ing ->
                    pantryItems.any { item -> item.name.contains(ing.keyword, ignoreCase = true) }
                }
            } else {
                saved.readyCount
            }
            val totalCount = if (finalIngredients.isNotEmpty()) finalIngredients.size else saved.totalCount
            val matchPercent = if (totalCount > 0) (readyCount * 100) / totalCount else saved.matchPercent

            saved.copy(
                matchPercent = matchPercent,
                readyCount = readyCount,
                totalCount = totalCount,
                ingredients = finalIngredients,
                steps = finalSteps
            )
        } else if (template != null) {
            val readyCount = if (template.ingredients.isNotEmpty()) {
                template.ingredients.count { ing ->
                    pantryItems.any { item -> item.name.contains(ing.keyword, ignoreCase = true) }
                }
            } else {
                template.ingredientKeywords.count { keyword ->
                    pantryItems.any { item -> item.name.contains(keyword, ignoreCase = true) }
                }
            }
            val totalCount = if (template.ingredients.isNotEmpty()) template.ingredients.size else template.ingredientKeywords.size
            val matchPercent = if (totalCount > 0) (readyCount * 100) / totalCount else 0
            val missingCount = totalCount - readyCount
            val missingText = if (missingCount > 0) "$missingCount bahan kurang" else "Lengkap"

            Recipe(
                id = template.id,
                title = template.title,
                description = template.description,
                durationMinutes = template.durationMinutes,
                servings = template.servings,
                matchPercent = matchPercent,
                usesLabel = "Cocok dengan bahanmu",
                readyCount = readyCount,
                totalCount = totalCount,
                missingIngredient = missingText,
                imageRes = template.imageRes,
                ingredients = template.ingredients,
                steps = template.steps
            )
        } else {
            null
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSoftCream)
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 32.dp)
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
            // 2. HERO IMAGE & BADGE
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
                            Text(
                                text = "${recipe.matchPercent}% Cocok",
                                color = if (recipe.matchPercent >= 70) ColorSurfaceWhite else ColorDarkChocolate,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 3. TITLE & DESCRIPTION
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

            // 4. STATS ROW (WAKTU DAN PORSI: HANYA TAMPIL JIKA ADA NILAINYA)
            val hasTime = recipe.durationMinutes > 0
            val hasServings = recipe.servings > 0
            if (hasTime || hasServings) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (hasTime) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Rounded.Timer,
                                label = "WAKTU",
                                value = "${recipe.durationMinutes} mnt"
                            )
                        }
                        if (hasServings) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                icon = Icons.Rounded.People,
                                label = "PORSI",
                                value = "${recipe.servings} porsi"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 5. SECTION BAHAN-BAHAN (Hanya tampil jika ingredients tidak kosong)
            if (recipe.ingredients.isNotEmpty()) {
                val availableCount = recipe.ingredients.count { ing ->
                    pantryItems.any { item -> item.name.contains(ing.keyword, ignoreCase = true) }
                }

                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Bahan-bahan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ColorDarkChocolate
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$availableCount dari ${recipe.ingredients.size} bahan tersedia",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (availableCount == recipe.ingredients.size) ColorForestGreen else ColorTextSubtitleBrown
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recipe.ingredients.forEach { ing ->
                                val isOwned = pantryItems.any { item -> item.name.contains(ing.keyword, ignoreCase = true) }
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .background(
                                                        color = if (isOwned) Color(0xFFE8F2EA) else Color(0xFFFFEAEA),
                                                        shape = CircleShape
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isOwned) Icons.Rounded.Check else Icons.Rounded.Close,
                                                    contentDescription = if (isOwned) "Tersedia di pantry" else "Belum ada di pantry",
                                                    tint = if (isOwned) ColorForestGreen else ColorUrgencyRed,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = ing.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ColorDarkChocolate
                                            )
                                        }
                                        Text(
                                            text = ing.amount,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = ColorTextSubtitleBrown
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // 6. SECTION LANGKAH MEMASAK (Hanya tampil jika steps tidak kosong)
            if (recipe.steps.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Langkah Memasak",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ColorDarkChocolate
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            recipe.steps.forEachIndexed { index, stepText ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .background(ColorForestGreen, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${index + 1}",
                                                color = ColorSurfaceWhite,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = stepText,
                                            fontSize = 13.sp,
                                            color = ColorDarkChocolate,
                                            lineHeight = 19.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
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