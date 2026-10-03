// [Materi: Add Pantry Item Form] Layar untuk menambahkan bahan baru ke inventaris pantry
package com.example.pantrick.ui.screen

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.data.model.FoodCategory
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.data.model.User
import com.example.pantrick.ui.component.HomeHeader
import com.example.pantrick.ui.viewmodel.PantryViewModel
import java.time.LocalDate

@Composable
fun AddPantryItemScreen(
    currentUser: User?,
    pantryViewModel: PantryViewModel,
    initialLocation: StorageLocation = StorageLocation.KULKAS,
    onSaveSuccess: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    var itemName by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf(initialLocation) }
    var quantity by remember { mutableStateOf(1) }
    var selectedCategory by remember { mutableStateOf("Produk Susu & Telur") }

    val userNameSafe = currentUser?.fullName ?: "Pengguna"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSoftCream)
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. TOP APP BAR
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

        // 2. KARTU UTAMA FORM TAMBAH BAHAN
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(ColorWarmPeach, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.ShoppingCart, contentDescription = null, tint = ColorDarkChocolate, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Tambah Bahan Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ColorDarkChocolate)
                            Text("Catat belanjaan segar & pantau masa simpan", fontSize = 11.sp, color = ColorTextSubtitleBrown)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. INPUT NAMA ITEM
                    Text("Nama Bahan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ColorDarkChocolate)
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        placeholder = { Text("cth., Susu Segar Organik", color = ColorPlaceholder, fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Rounded.LocalDrink, contentDescription = null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(18.dp)) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = ColorSoftCream,
                            focusedContainerColor = ColorSoftCream,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item { PanggilSuggestionChip("🥛 Susu", onClick = { itemName = "Susu" }) }
                        item { PanggilSuggestionChip("🥚 Telur", onClick = { itemName = "Telur" }) }
                        item { PanggilSuggestionChip("🥬 Bayam", onClick = { itemName = "Bayam" }) }
                        item { PanggilSuggestionChip("🍗 Ayam", onClick = { itemName = "Ayam" }) }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4. ZONA PENYIMPANAN
                    Text("Zona Penyimpanan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ColorDarkChocolate)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ColorSoftCream, RoundedCornerShape(16.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PantryStorageTabButton(
                            title = "Kulkas",
                            icon = Icons.Rounded.Kitchen,
                            isSelected = selectedLocation == StorageLocation.KULKAS,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedLocation = StorageLocation.KULKAS }
                        )
                        PantryStorageTabButton(
                            title = "Freezer",
                            icon = Icons.Rounded.AcUnit,
                            isSelected = selectedLocation == StorageLocation.FREEZER,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedLocation = StorageLocation.FREEZER }
                        )
                        PantryStorageTabButton(
                            title = "Rak Kering",
                            icon = Icons.Rounded.Kitchen,
                            isSelected = selectedLocation == StorageLocation.RAK_KERING,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedLocation = StorageLocation.RAK_KERING }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 5. KATEGORI
                    Text("Kategori", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ColorDarkChocolate)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ColorSoftCream, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Egg, contentDescription = null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(selectedCategory, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorDarkChocolate)
                            }
                            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = ColorTextSubtitleBrown)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 6. JUMLAH & SATUAN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Jumlah", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ColorDarkChocolate)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ColorSoftCream, RoundedCornerShape(16.dp))
                                    .height(52.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = { if (quantity > 1) quantity-- }) {
                                    Icon(Icons.Rounded.Remove, contentDescription = "Kurang", tint = ColorDarkChocolate)
                                }
                                Text("$quantity", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ColorDarkChocolate)
                                IconButton(onClick = { quantity++ }) {
                                    Icon(Icons.Rounded.Add, contentDescription = "Tambah", tint = ColorDarkChocolate)
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Satuan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ColorDarkChocolate)
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(ColorSoftCream, RoundedCornerShape(16.dp))
                                    .height(52.dp)
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("karton", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorDarkChocolate)
                                    Icon(Icons.Rounded.UnfoldMore, contentDescription = null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = ColorSoftCream),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = ColorWarmPeach,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.Verified, contentDescription = null, tint = ColorDarkChocolate, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Jaminan Kesegaran Pantrick", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ColorDarkChocolate)
                                Text("Kami akan mengingatkanmu 24 jam sebelum kedaluwarsa.", fontSize = 11.sp, color = ColorTextSubtitleBrown)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 8. TOMBOL SIMPAN UTAMA
                    Button(
                        onClick = {
                            if (itemName.isNotBlank()) {
                                val newItem = PantryItem(
                                    id = System.currentTimeMillis().toString(),
                                    name = itemName,
                                    location = selectedLocation,
                                    quantityLabel = "$quantity karton",
                                    expiryEpochDay = LocalDate.now().plusDays(7).toEpochDay(),
                                    category = FoodCategory.SUSU_TELUR
                                )
                                pantryViewModel.addItem(
                                    email = currentUser?.email ?: "guest",
                                    item = newItem
                                )
                                onSaveSuccess()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorDarkChocolate),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = ColorSurfaceWhite, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan ke Dapur", color = ColorSurfaceWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PanggilSuggestionChip(text: String, onClick: () -> Unit) {
    Surface(
        color = ColorSoftCream,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = ColorDarkChocolate,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun PantryStorageTabButton(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isSelected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) ColorForestGreen else Color.Transparent,
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.height(40.dp).clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) ColorSurfaceWhite else ColorTextSubtitleBrown,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) ColorSurfaceWhite else ColorTextSubtitleBrown
            )
        }
    }
}