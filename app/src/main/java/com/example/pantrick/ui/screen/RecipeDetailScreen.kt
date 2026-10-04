package com.example.pantrick.ui.screen

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.pantrick.core.network.PantrickApiConfig
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.data.model.BackendRecipeIngredient
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.Recipe
import com.example.pantrick.ui.viewmodel.CookingSessionViewModel
import com.example.pantrick.ui.viewmodel.CookingUiState
import com.example.pantrick.ui.viewmodel.IngredientSearchViewModel
import com.example.pantrick.ui.viewmodel.RecipeDetailUiState
import com.example.pantrick.ui.viewmodel.RecipeViewModel
import com.example.pantrick.util.IngredientNormalizer

private const val TAG_DETAIL = "RecipeDetail"

// =====================================================================
// MAIN SCREEN
// =====================================================================
@Composable
fun RecipeDetailScreen(
    recipeId: String,
    savedRecipes: List<Recipe> = emptyList(),
    pantryItems: List<PantryItem> = emptyList(),
    jwtToken: String = "",
    viewModel: IngredientSearchViewModel = viewModel(),
    recipeViewModel: RecipeViewModel = viewModel(),
    cookingViewModel: CookingSessionViewModel = viewModel(),
    onPantryRefresh: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val detailUiState by viewModel.detailState.collectAsState()
    val savedList by recipeViewModel.savedRecipes.collectAsState()
    val isSaved = savedList.any { it.id == recipeId }
    val cookingState by cookingViewModel.cookingState.collectAsState()

    LaunchedEffect(recipeId, jwtToken) {
        Log.d(TAG_DETAIL, "LaunchedEffect: loadRecipeDetail($recipeId)")
        viewModel.loadRecipeDetail(recipeId)
        
        // Check if there's an active cooking session for this recipe
        if (jwtToken.isNotBlank()) {
            cookingViewModel.resumeActiveSession(recipeId, jwtToken)
        }
    }
    
    // Reset cooking state when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            cookingViewModel.reset()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSoftCream)
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── HEADER ──
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Kembali",
                        tint = ColorDarkChocolate
                    )
                }
                Text(
                    text = "Detail Resep",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ColorDarkChocolate
                )
            }
        }

        // ── KONTEN BERDASARKAN STATE ──
        when (val state = detailUiState) {

            // -- LOADING --
            is RecipeDetailUiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ColorForestGreen)
                    }
                }
            }

            // -- ERROR --
            is RecipeDetailUiState.Error -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = ColorUrgencyRed,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // -- NOT FOUND / IDLE --
            is RecipeDetailUiState.NotFound,
            is RecipeDetailUiState.Idle -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Resep tidak ditemukan.",
                            color = ColorTextSubtitleBrown,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // -- SUCCESS: TAMPILKAN DATA ASLI DARI BACKEND --
            is RecipeDetailUiState.Success -> {
                val recipe = state.recipe
                Log.d(TAG_DETAIL, "SUCCESS — recipe.id=${recipe.id} title=${recipe.title} ingredients=${recipe.ingredients.size}")
                recipe.ingredients.forEachIndexed { i, ing ->
                    Log.d(TAG_DETAIL, "  [$i] raw=[${ing.raw}] qty=[${ing.quantity}] unit=[${ing.unit}]")
                }

                // -- Gambar --
                item {
                    val imageUrl = if (!recipe.imageName.isNullOrEmpty()) {
                        "${PantrickApiConfig.BASE_URL}/api/recipes/${recipe.imageName}/image"
                    } else null

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .height(200.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(ColorWarmPeach.copy(alpha = 0.4f))
                    ) {
                        if (imageUrl != null) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = recipe.title,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Rounded.Restaurant,
                                    contentDescription = null,
                                    tint = ColorDarkChocolate.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }
                }

                // -- Judul --
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        Text(
                            text = recipe.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = ColorDarkChocolate,
                            lineHeight = 28.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Tombol simpan
                        val saveBg by animateColorAsState(
                            if (isSaved) ColorForestGreen else ColorSurfaceWhite,
                            animationSpec = tween(250), label = "saveBg"
                        )
                        val saveFg by animateColorAsState(
                            if (isSaved) ColorSurfaceWhite else ColorForestGreen,
                            animationSpec = tween(250), label = "saveFg"
                        )
                        OutlinedButton(
                            onClick = {
                                recipeViewModel.toggleSaveRecipe(
                                    Recipe(
                                        id = recipe.id,
                                        title = recipe.title,
                                        savedAtEpochMillis = System.currentTimeMillis()
                                    )
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = saveBg,
                                contentColor = saveFg
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorForestGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isSaved) "Tersimpan" else "Simpan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // -- Statistik waktu/porsi --
                val hasDuration = (recipe.cookingTimeMinutes ?: 0) > 0
                val hasServings  = (recipe.servings ?: 0) > 0
                if (hasDuration || hasServings) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (hasDuration) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Rounded.Timer, null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.height(4.dp))
                                        Text("WAKTU", fontSize = 10.sp, color = ColorTextSubtitleBrown, fontWeight = FontWeight.Bold)
                                        Text("${recipe.cookingTimeMinutes} mnt", fontSize = 13.sp, color = ColorDarkChocolate, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            if (hasServings) {
                                Card(
                                    modifier = Modifier.weight(1f),
                                    colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Rounded.People, null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.height(4.dp))
                                        Text("PORSI", fontSize = 10.sp, color = ColorTextSubtitleBrown, fontWeight = FontWeight.Bold)
                                        Text("${recipe.servings} porsi", fontSize = 13.sp, color = ColorDarkChocolate, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // ── BAHAN-BAHAN ──
                val ings = recipe.ingredients
                if (ings.isNotEmpty()) {
                    // Header section
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bahan-bahan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = ColorDarkChocolate
                            )
                            
                            // Status ketersediaan dari API check
                            when (val state = cookingState) {
                                is CookingUiState.ReadinessResult -> {
                                    val readiness = state.readiness
                                    val missingCount = readiness.ingredients.count { 
                                        !it.isNameMatched || !it.isSufficientQty 
                                    }
                                    Text(
                                        text = when {
                                            missingCount == 0 -> "Semua bahan tersedia"
                                            missingCount == readiness.ingredients.size -> "$missingCount bahan belum ada"
                                            else -> "$missingCount bahan masih kurang"
                                        },
                                        fontSize = 12.sp,
                                        color = if (missingCount == 0) ColorForestGreen else ColorUrgencyRed,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "Cek ketersediaan",
                                        fontSize = 12.sp,
                                        color = ColorTextSubtitleBrown,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Display ingredients WITHOUT availability until API check completes
                    when (val state = cookingState) {
                        is CookingUiState.ReadinessResult -> {
                            // Show ingredients WITH availability from API
                            val readiness = state.readiness
                            items(
                                items = readiness.ingredients,
                                key = { it.ingredientRaw }
                            ) { ingAvail ->
                                val isAvailable = ingAvail.isNameMatched && ingAvail.isSufficientQty
                                val displayName = ingAvail.ingredientRaw.ifBlank { "Bahan tidak diketahui" }
                                
                                val qtyLabel: String? = when {
                                    ingAvail.recipeQtyStr != null && ingAvail.recipeUnit != null -> 
                                        "${ingAvail.recipeQtyStr} ${ingAvail.recipeUnit}"
                                    ingAvail.recipeQtyStr != null -> ingAvail.recipeQtyStr
                                    ingAvail.recipeUnit != null -> ingAvail.recipeUnit
                                    else -> null
                                }

                                val statusText = when {
                                    !ingAvail.isNameMatched -> "Belum tersedia"
                                    !ingAvail.isSufficientQty && ingAvail.isQuantityComparable -> 
                                        "Kurang ${ingAvail.recipeQtyStr ?: ""} ${ingAvail.recipeUnit ?: ""}".trim()
                                    !ingAvail.isSufficientQty -> "Kurang"
                                    else -> "Tersedia"
                                }

                                val bgColor by animateColorAsState(
                                    targetValue = if (isAvailable) Color(0xFFF2FBF4) else Color(0xFFFFF8F8),
                                    animationSpec = tween(250),
                                    label = "ing_bg_${ingAvail.ingredientRaw}"
                                )

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = bgColor),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(
                                                    color = if (isAvailable) Color(0xFFD4EDDA) else Color(0xFFFFE0E0),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isAvailable) Icons.Rounded.Check else Icons.Rounded.Close,
                                                contentDescription = null,
                                                tint = if (isAvailable) ColorForestGreen else ColorUrgencyRed,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = displayName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = ColorDarkChocolate
                                            )
                                            if (!qtyLabel.isNullOrBlank()) {
                                                Text(
                                                    text = qtyLabel,
                                                    fontSize = 12.sp,
                                                    color = ColorTextSubtitleBrown
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = statusText,
                                            fontSize = 12.sp,
                                            color = if (isAvailable) ColorForestGreen else ColorUrgencyRed,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        else -> {
                            // Show ingredients WITHOUT availability status before API check
                            items(
                                items = ings,
                                key = { ing -> ing.raw + ing.normalizedName }
                            ) { ing ->
                                val displayName = ing.raw.trim().ifBlank { 
                                    ing.normalizedName.trim() 
                                }.ifBlank { "Bahan tidak diketahui" }
                                
                                val qtyLabel: String? = when {
                                    ing.quantity != null && ing.unit != null -> "${ing.quantity} ${ing.unit}"
                                    ing.quantity != null -> ing.quantity
                                    ing.unit != null -> ing.unit
                                    else -> null
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
                                    shape = RoundedCornerShape(14.dp),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(
                                                    color = ColorTextSubtitleBrown.copy(alpha = 0.2f),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Restaurant,
                                                contentDescription = null,
                                                tint = ColorTextSubtitleBrown,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = displayName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = ColorDarkChocolate
                                            )
                                            if (!qtyLabel.isNullOrBlank()) {
                                                Text(
                                                    text = qtyLabel,
                                                    fontSize = 12.sp,
                                                    color = ColorTextSubtitleBrown
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Text(
                            text = "Daftar bahan tidak tersedia untuk resep ini.",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 13.sp,
                            color = ColorTextSubtitleBrown
                        )
                    }
                }

                // ── TOMBOL MEMASAK ──
                item {
                    CookingActionSection(
                        recipeId = recipeId,
                        recipe = (detailUiState as? RecipeDetailUiState.Success)?.recipe,
                        jwtToken = jwtToken,
                        cookingState = cookingState,
                        cookingViewModel = cookingViewModel,
                        onPantryRefresh = onPantryRefresh,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // ── LANGKAH MEMASAK ──
                if (!recipe.instructions.isNullOrEmpty()) {
                    val steps = recipe.instructions
                        .split("\n")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                        .let { if (it.size > 1) it else recipe.instructions.split(". ").map { s -> s.trim() }.filter { s -> s.isNotBlank() } }

                    if (steps.isNotEmpty()) {
                        item {
                            Text(
                                text = "Langkah Memasak",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = ColorDarkChocolate,
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(steps.size) { i ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
                                shape = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .background(ColorForestGreen, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${i + 1}",
                                            color = ColorSurfaceWhite,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = steps[i],
                                        fontSize = 13.sp,
                                        color = ColorDarkChocolate,
                                        lineHeight = 19.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// =====================================================================
// COOKING ACTION SECTION
// =====================================================================

@Composable
private fun CookingActionSection(
    recipeId: String,
    recipe: com.example.pantrick.data.model.BackendRecipe?,
    jwtToken: String,
    cookingState: CookingUiState,
    cookingViewModel: CookingSessionViewModel,
    onPantryRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (recipe == null || jwtToken.isBlank()) return

    Column(modifier = modifier.fillMaxWidth()) {
        when (cookingState) {
            is CookingUiState.Idle -> {
                // Tombol: Cek Ketersediaan Bahan
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Info, contentDescription = null, tint = ColorWarmPeach, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Cek ketersediaan bahan untuk resep ini",
                                fontSize = 12.sp,
                                color = ColorTextSubtitleBrown
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        cookingViewModel.checkReadiness(recipeId, jwtToken)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorForestGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Cek Ketersediaan Bahan", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            is CookingUiState.CheckingReadiness -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ColorForestGreen, modifier = Modifier.size(32.dp))
                }
            }

            is CookingUiState.ReadinessResult -> {
                val readiness = cookingState.readiness
                if (readiness.canCook) {
                    // Semua bahan tersedia — tombol MEMASAK aktif
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2FBF4)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = ColorForestGreen, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Semua bahan tersedia!",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorForestGreen
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                cookingViewModel.startCooking(recipeId, jwtToken)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorForestGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Restaurant, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Mulai Memasak", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Ada bahan yang kurang — tombol disabled
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F8)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Rounded.Close, contentDescription = null, tint = ColorUrgencyRed, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = "Masih ada bahan yang belum tersedia",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ColorUrgencyRed
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = readiness.message,
                                    fontSize = 12.sp,
                                    color = ColorTextSubtitleBrown
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { },
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ColorTextSubtitleBrown.copy(alpha = 0.3f),
                                disabledContainerColor = ColorTextSubtitleBrown.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Restaurant, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Tidak Bisa Memasak", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is CookingUiState.Starting -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ColorForestGreen, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Memulai sesi memasak...", fontSize = 12.sp, color = ColorTextSubtitleBrown)
                    }
                }
            }

            is CookingUiState.Active -> {
                val session = cookingState.session
                Column(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E6)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = ColorWarmPeach, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Sedang Memasak",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorDarkChocolate
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = session.recipeTitle,
                                fontSize = 12.sp,
                                color = ColorTextSubtitleBrown
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                cookingViewModel.cancelCooking(jwtToken) {
                                    onPantryRefresh()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ColorUrgencyRed),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorUrgencyRed),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Batalkan", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                cookingViewModel.completeCooking(jwtToken) {
                                    onPantryRefresh()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorForestGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Selesai", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is CookingUiState.Cancelling -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ColorUrgencyRed, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Membatalkan sesi...", fontSize = 12.sp, color = ColorTextSubtitleBrown)
                    }
                }
            }

            is CookingUiState.Cancelled -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F8)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = null, tint = ColorUrgencyRed, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Sesi dibatalkan",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorUrgencyRed
                            )
                            Text(
                                text = "Bahan pantry telah dikembalikan",
                                fontSize = 11.sp,
                                color = ColorTextSubtitleBrown
                            )
                        }
                    }
                }
            }

            is CookingUiState.Completing -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = ColorForestGreen, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Menyelesaikan sesi...", fontSize = 12.sp, color = ColorTextSubtitleBrown)
                    }
                }
            }

            is CookingUiState.Completed -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF2FBF4)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = ColorForestGreen, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Selamat! Masakan selesai",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorForestGreen
                            )
                            Text(
                                text = "Bahan pantry telah dikurangi",
                                fontSize = 11.sp,
                                color = ColorTextSubtitleBrown
                            )
                        }
                    }
                }
            }

            is CookingUiState.Error -> {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8F8)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Close, contentDescription = null, tint = ColorUrgencyRed, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Terjadi Kesalahan",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorUrgencyRed
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Gagal menghubungi server. Silakan coba lagi.",
                                fontSize = 12.sp,
                                color = ColorTextSubtitleBrown
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            cookingViewModel.reset()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorForestGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Coba Lagi", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
