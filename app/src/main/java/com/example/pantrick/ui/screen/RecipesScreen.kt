package com.example.pantrick.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.data.model.Recipe
import com.example.pantrick.data.model.User
import com.example.pantrick.ui.component.HomeHeader

@Composable
fun RecipesScreen(
    currentUser: User? = null,
    savedRecipes: List<Recipe> = emptyList(),
    onToggleSaveRecipe: (Recipe) -> Unit = {},
    photoPath: String? = null,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    onNavigateToDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredRecipes = remember(savedRecipes, searchQuery) {
        if (searchQuery.isBlank()) {
            savedRecipes
        } else {
            savedRecipes.filter { it.title.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSoftCream)
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. HEADER
        item {
            HomeHeader(
                userName = currentUser?.fullName ?: "Pengguna",
                photoPath = photoPath,
                onNotificationClick = onNotificationClick,
                onProfileClick = onProfileClick
            )
        }

        // 2. SEARCH BAR (Tanpa mikrofon, berfungsi memfilter resep tersimpan berdasarkan nama)
        item {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari resep tersimpan...", color = ColorPlaceholder, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = ColorDarkChocolate) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Hapus pencarian", tint = ColorTextSubtitleBrown)
                        }
                    }
                },
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = ColorSurfaceWhite,
                    focusedContainerColor = ColorSurfaceWhite,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. KOLEKSI RESEPMU (Card ringkasan resep tersimpan dinamis)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = ColorWarmPeach),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = ColorDarkChocolate,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "KOLEKSI RESEPMU",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorDarkChocolate,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (savedRecipes.isNotEmpty()) {
                            "Kamu punya ${savedRecipes.size} resep tersimpan."
                        } else {
                            "Belum ada resep tersimpan."
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (savedRecipes.isNotEmpty()) {
                            "Pilih salah satu untuk mulai memasak."
                        } else {
                            "Cari resep lewat tombol + di tengah, lalu simpan dengan ikon hati."
                        },
                        fontSize = 12.sp,
                        color = ColorTextSubtitleBrown
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 4. RECIPES LIST HEADER ("Resep Tersimpan" tanpa subteks dan tanpa tombol "Urutkan")
        item {
            Text(
                text = "Resep Tersimpan",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = ColorDarkChocolate,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 5. RECIPE CARDS (HANYA resep tersimpan pengguna)
        if (filteredRecipes.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada resep tersimpan.",
                        fontSize = 14.sp,
                        color = ColorTextSubtitleBrown,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(filteredRecipes.size, key = { filteredRecipes[it].id }) { index ->
                val recipe = filteredRecipes[index]
                RecipeCard(
                    recipe = recipe,
                    onFavoriteClick = { onToggleSaveRecipe(recipe) },
                    onClick = { onNavigateToDetail(recipe.id) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: Recipe,
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    val isComplete = recipe.readyCount >= recipe.totalCount && recipe.totalCount > 0
    RecipeCard(
        title = recipe.title,
        matchText = if (recipe.totalCount > 0) "${recipe.matchPercent}% Cocok • ${recipe.readyCount}/${recipe.totalCount} Bahan" else "${recipe.matchPercent}% Cocok",
        matchBgColor = if (recipe.matchPercent >= 70) ColorForestGreen else ColorWarmPeach,
        badgeText = if (isComplete) "Siap dimasak" else "",
        badgeBgColor = Color(0xFFE8F2EA),
        badgeTextColor = ColorForestGreen,
        prep = recipe.usesLabel.ifBlank { "Bahan Dapur" },
        prepIcon = Icons.Rounded.Restaurant,
        prepColor = ColorTextSubtitleBrown,
        statusText = if (isComplete) "Semua bahan ada di dapur" else recipe.missingIngredient,
        statusColor = if (isComplete) ColorTextSubtitleBrown else ColorUrgencyRed,
        statusDot = if (isComplete) ColorForestGreen else ColorUrgencyRed,
        buttonText = "Lihat Resep >",
        buttonBgColor = ColorForestGreen,
        buttonTextColor = ColorSurfaceWhite,
        recipeId = recipe.id,
        imageName = recipe.imageName,
        isFavorite = true,
        onFavoriteClick = onFavoriteClick,
        onClick = onClick
    )
}

@Composable
fun RecipeCard(
    title: String,
    matchText: String,
    matchBgColor: Color = ColorForestGreen,
    badgeText: String,
    badgeBgColor: Color = ColorSurfaceWhite,
    badgeTextColor: Color = ColorDarkChocolate,
    prep: String,
    prepIcon: ImageVector,
    prepColor: Color = ColorTextSubtitleBrown,
    statusText: String,
    statusColor: Color,
    statusDot: Color = Color.Transparent,
    buttonText: String,
    buttonIcon: ImageVector? = null,
    buttonBgColor: Color,
    buttonTextColor: Color,
    recipeId: String,
    imageName: String? = null,
    isFavorite: Boolean = true,
    onFavoriteClick: () -> Unit = {},
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Gambar resep dari backend dataset
            val imageUrl = if (!imageName.isNullOrBlank()) {
                "${com.example.pantrick.core.network.PantrickApiConfig.BASE_URL}/api/recipes/${imageName}/image"
            } else null

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorPlaceholder.copy(alpha = 0.3f))
            ) {
                if (imageUrl != null) {
                    coil.compose.AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = painterResource(id = R.drawable.ic_placeholder_pasta),
                        placeholder = painterResource(id = R.drawable.ic_placeholder_pasta)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_placeholder_pasta),
                        contentDescription = title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Surface(
                    color = matchBgColor.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(percent = 50),
                    modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Icon(Icons.Rounded.CheckCircleOutline, contentDescription = null, tint = ColorSurfaceWhite, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(matchText, color = ColorSurfaceWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Tombol Batal Simpan (Ikon Hati) di pojok kanan atas kartu
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(36.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(ColorSurfaceWhite.copy(alpha = 0.9f))
                        .clickable { onFavoriteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Batal simpan resep",
                        tint = if (isFavorite) ColorUrgencyRed else ColorDarkChocolate,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (badgeText.isNotEmpty()) {
                    Surface(
                        color = badgeBgColor.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(8.dp).align(Alignment.BottomEnd)
                    ) {
                        Text(badgeText, color = badgeTextColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ColorDarkChocolate,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Hanya tampilkan prep/kategori bahan
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(prepIcon, contentDescription = null, tint = prepColor, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(prep, fontSize = 11.sp, color = prepColor, fontWeight = if (prepColor == ColorForestGreen) FontWeight.Bold else FontWeight.Normal)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (statusDot != Color.Transparent) {
                        Box(modifier = Modifier.size(6.dp).background(statusDot, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        color = statusColor,
                        fontWeight = if (statusColor == ColorUrgencyRed) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Button(
                    onClick = { onClick() },
                    colors = ButtonDefaults.buttonColors(containerColor = buttonBgColor),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    if (buttonIcon != null) {
                        Icon(buttonIcon, contentDescription = null, tint = buttonTextColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(buttonText, color = buttonTextColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}