package com.example.pantrick.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.ui.component.HomeHeader

@Composable
fun RecipesScreen(
    contentPadding: PaddingValues = PaddingValues(),
    onNavigateToDetail: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSoftCream)
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. HEADER (Menggunakan HomeHeader agar persis seperti Beranda)
        item {
            HomeHeader(
                userName = "Alex Morgan",
                onNotificationClick = { /* TODO */ },
                onProfileClick = { /* TODO */ }
            )
        }

        // 2. SEARCH BAR
        item {
            var searchQuery by remember { mutableStateOf("") }
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari resep berdasarkan bahan...", color = ColorPlaceholder, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = ColorDarkChocolate) },
                trailingIcon = { Icon(Icons.Rounded.Mic, contentDescription = null, tint = ColorForestGreen) },
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

        // 3. FILTER CHIPS (Tanpa Tinggi Protein)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item { FilterChipItem(text = "Semua", icon = Icons.Rounded.Check, isSelected = true) }
                item { FilterChipItem(text = "100% Siap", icon = Icons.Rounded.Eco, isSelected = false) }
                item { FilterChipItem(text = "< 20m Cepat", icon = Icons.Rounded.Timer, isSelected = false) }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 4. SMART MEAL GENERATOR
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = ColorDarkChocolate, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PEMBUAT MENU PINTAR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorDarkChocolate, letterSpacing = 0.5.sp)
                        }
                        Surface(color = ColorSurfaceWhite.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp)) {
                            Text("Diperbarui 10m lalu", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ColorDarkChocolate, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Kamu bisa memasak 4 resep dengan 0 bahan kurang hari ini!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Berdasarkan bahan segar di kulkas & stok dapurmu.",
                        fontSize = 12.sp,
                        color = ColorTextSubtitleBrown
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { /* TODO */ },
                            colors = ButtonDefaults.buttonColors(containerColor = ColorDarkChocolate),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Icon(Icons.Rounded.RoomService, contentDescription = null, tint = ColorSurfaceWhite, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buat Makan Malam Instan", color = ColorSurfaceWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(
                            onClick = { /* TODO */ },
                            modifier = Modifier
                                .size(40.dp)
                                .background(ColorSurfaceWhite.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Rounded.Tune, contentDescription = "Pengaturan", tint = ColorDarkChocolate, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 5. RECIPES LIST HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Resep Sesuai Bahan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ColorDarkChocolate)
                    Text("Diurutkan dari bahan di dapurmu", fontSize = 11.sp, color = ColorTextSubtitleBrown)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Urutkan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorForestGreen)
                    Icon(Icons.Rounded.SwapVert, contentDescription = null, tint = ColorForestGreen, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 6. RECIPE CARDS
        item {
            RecipeCard(
                title = "Creamy Garlic Herb Pasta",
                matchText = "Cocok 90% • 5/6 Bahan",
                badgeText = "Siap dimasak",
                time = "25 mnt",
                cals = "420 kkal",
                prep = "Persiapan mudah",
                prepIcon = Icons.Rounded.Restaurant,
                statusText = "Butuh: Krim kental",
                statusColor = ColorDarkChocolate,
                statusDot = ColorUrgencyRed,
                buttonText = "Lihat Resep >",
                buttonBgColor = ColorForestGreen,
                buttonTextColor = ColorSurfaceWhite,
                onClick = { onNavigateToDetail("RCP-01") }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            RecipeCard(
                title = "Fresh Spinach & Egg Frittata",
                matchText = "Cocok 100% • 4/4 Bahan",
                badgeText = "Tanpa Sisa",
                badgeBgColor = Color(0xFFE8F2EA),
                badgeTextColor = ColorForestGreen,
                time = "15 mnt",
                cals = "310 kkal",
                prep = "Siap Sekarang",
                prepIcon = Icons.Rounded.Bolt,
                prepColor = ColorForestGreen,
                statusText = "Semua bahan ada di dapur",
                statusColor = ColorTextSubtitleBrown,
                statusDot = ColorForestGreen,
                buttonText = "Masak Sekarang",
                buttonIcon = Icons.Rounded.PlayCircleOutline,
                buttonBgColor = ColorForestGreen,
                buttonTextColor = ColorSurfaceWhite,
                onClick = { onNavigateToDetail("RCP-02") }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            RecipeCard(
                title = "Honey Mustard Glazed Chicken",
                matchText = "Cocok 75% • 3/4 Bahan",
                matchBgColor = Color(0xFF7A685F),
                badgeText = "",
                time = "35 mnt",
                cals = "480 kkal",
                prep = "38g Protein",
                prepIcon = Icons.Rounded.FitnessCenter,
                statusText = "Kurang: Mustard Dijon",
                statusColor = ColorUrgencyRed,
                statusDot = Color.Transparent,
                buttonText = "+ Ke Keranjang",
                buttonBgColor = ColorWarmPeach.copy(alpha = 0.5f),
                buttonTextColor = ColorDarkChocolate,
                onClick = { onNavigateToDetail("RCP-03") }
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // 7. SAVED COLLECTIONS HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Koleksi Tersimpan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ColorDarkChocolate)
                    Text("Daftar putar resep pribadimu", fontSize = 11.sp, color = ColorTextSubtitleBrown)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CreateNewFolder, contentDescription = null, tint = ColorForestGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Folder Baru", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorForestGreen)
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(text: String, icon: ImageVector?, isSelected: Boolean) {
    Surface(
        color = if (isSelected) ColorForestGreen else ColorSurfaceWhite,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.height(36.dp).clickable { /* TODO */ }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) ColorSurfaceWhite else ColorForestGreen,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) ColorSurfaceWhite else ColorDarkChocolate
            )
        }
    }
}

@Composable
fun RecipeCard(
    title: String,
    matchText: String,
    matchBgColor: Color = ColorForestGreen,
    badgeText: String,
    badgeBgColor: Color = ColorSurfaceWhite,
    badgeTextColor: Color = ColorDarkChocolate,
    time: String,
    cals: String,
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
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(16.dp)).background(ColorPlaceholder.copy(alpha = 0.3f))
            ) {
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

                Box(
                    modifier = Modifier.padding(8.dp).size(28.dp).align(Alignment.TopEnd).background(ColorSurfaceWhite, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Bookmark, contentDescription = null, tint = ColorForestGreen, modifier = Modifier.size(16.dp))
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Timer, contentDescription = null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(time, fontSize = 11.sp, color = ColorTextSubtitleBrown)

                Spacer(modifier = Modifier.width(12.dp))

                Icon(Icons.Rounded.LocalFireDepartment, contentDescription = null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(cals, fontSize = 11.sp, color = ColorTextSubtitleBrown)

                Spacer(modifier = Modifier.width(12.dp))

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