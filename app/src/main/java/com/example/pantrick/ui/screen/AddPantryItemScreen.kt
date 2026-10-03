// [Materi: Add Pantry Item Form] Layar untuk menambahkan bahan baru ke inventaris pantry
package com.example.pantrick.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.data.model.FoodCategory
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.data.model.User
import com.example.pantrick.ui.viewmodel.PantryViewModel
import com.example.pantrick.util.ExpiryEstimator
import com.example.pantrick.util.PantrickConstants
import androidx.compose.material.icons.filled.CalendarToday
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    var selectedCategory by remember { mutableStateOf<FoodCategory?>(null) }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    var quantityText by remember { mutableStateOf("1") }

    val unitOptions = remember {
        listOf(
            "buah", "butir", "gram", "kg", "ml", "liter", "siung", "lembar",
            "batang", "ikat", "bungkus", "sachet", "botol", "kaleng", "pak",
            "potong", "ekor", "sdm", "sdt", "gelas", "porsi"
        )
    }
    var selectedUnit by remember { mutableStateOf("buah") }
    var isUnitDropdownExpanded by remember { mutableStateOf(false) }

    var isManualExpiry by remember { mutableStateOf(false) }
    var expiryLocalDate by remember {
        mutableStateOf(
            ExpiryEstimator.estimateExpiryDate(
                FoodCategory.LAINNYA,
                initialLocation
            )
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(selectedCategory, selectedLocation, itemName, isManualExpiry) {
        if (!isManualExpiry) {
            val cat = selectedCategory ?: FoodCategory.LAINNYA
            expiryLocalDate = ExpiryEstimator.estimateExpiryDate(cat, selectedLocation, itemName)
        }
    }

    val idLocale = remember { Locale.forLanguageTag("id-ID") }
    val dateFormatter = remember(idLocale) { DateTimeFormatter.ofPattern("d MMM yyyy", idLocale) }
    val formattedExpiryDate = remember(expiryLocalDate, dateFormatter) {
        expiryLocalDate.format(dateFormatter)
    }

    fun parseQuantity(raw: String): Double? {
        return raw.trim().replace(',', '.').toDoubleOrNull()
    }

    val parsedQty = parseQuantity(quantityText)
    val isQuantityValid = parsedQty != null && parsedQty > 0.0
    val isFormValid = itemName.isNotBlank() && selectedCategory != null && isQuantityValid

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expiryLocalDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            expiryLocalDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC)
                                .toLocalDate()
                            isManualExpiry = true
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih", color = ColorForestGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal", color = ColorDarkChocolate)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSoftCream)
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. TOP APP BAR (Back button di kiri, Logo Pantrick di kanan)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back_button),
                        tint = ColorDarkChocolate
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_pantrick),
                        contentDescription = stringResource(R.string.cd_logo),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(PantrickConstants.LOGO_SIZE_SMALL)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.app_name),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorForestGreen
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

                    // 3. INPUT NAMA ITEM (tanpa quick suggestion chip)
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

                    Spacer(modifier = Modifier.height(20.dp))

                    // 4. ZONA PENYIMPANAN (Hanya Kulkas dan Freezer)
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
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 5. KATEGORI (Dropdown fungsional berisi seluruh FoodCategory)
                    Text("Kategori", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ColorDarkChocolate)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = isCategoryDropdownExpanded,
                        onExpandedChange = { isCategoryDropdownExpanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = selectedCategory?.let { "${it.emoji}  ${it.label}" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Pilih kategori bahan", color = ColorPlaceholder, fontSize = 13.sp) },
                            leadingIcon = {
                                if (selectedCategory != null) {
                                    Text(
                                        text = selectedCategory!!.emoji,
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Category,
                                        contentDescription = null,
                                        tint = ColorTextSubtitleBrown,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded)
                            },
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
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        )

                        ExposedDropdownMenu(
                            expanded = isCategoryDropdownExpanded,
                            onDismissRequest = { isCategoryDropdownExpanded = false },
                            modifier = Modifier.background(ColorSurfaceWhite)
                        ) {
                            FoodCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(cat.emoji, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = cat.label,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = ColorDarkChocolate
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedCategory = cat
                                        isCategoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 6. JUMLAH & SATUAN (Input ketik angka / desimal + stepper + dropdown satuan)
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
                                    .height(52.dp)
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(
                                    onClick = {
                                        val current = parseQuantity(quantityText) ?: 0.0
                                        val nextVal = (current - 1.0).coerceAtLeast(0.0)
                                        quantityText = if (nextVal % 1.0 == 0.0) {
                                            nextVal.toLong().toString()
                                        } else {
                                            "%.2f".format(Locale.US, nextVal).trimEnd('0').trimEnd('.')
                                        }
                                    }
                                ) {
                                    Icon(Icons.Rounded.Remove, contentDescription = "Kurang", tint = ColorDarkChocolate)
                                }

                                BasicTextField(
                                    value = quantityText,
                                    onValueChange = { input ->
                                        if (input.all { it.isDigit() || it == '.' || it == ',' } && input.length <= 8) {
                                            quantityText = input
                                        }
                                    },
                                    textStyle = TextStyle(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = ColorDarkChocolate,
                                        textAlign = TextAlign.Center
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        val current = parseQuantity(quantityText) ?: 0.0
                                        val nextVal = current + 1.0
                                        quantityText = if (nextVal % 1.0 == 0.0) {
                                            nextVal.toLong().toString()
                                        } else {
                                            "%.2f".format(Locale.US, nextVal).trimEnd('0').trimEnd('.')
                                        }
                                    }
                                ) {
                                    Icon(Icons.Rounded.Add, contentDescription = "Tambah", tint = ColorDarkChocolate)
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Satuan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ColorDarkChocolate)
                            Spacer(modifier = Modifier.height(8.dp))
                            ExposedDropdownMenuBox(
                                expanded = isUnitDropdownExpanded,
                                onExpandedChange = { isUnitDropdownExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextField(
                                    value = selectedUnit,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isUnitDropdownExpanded)
                                    },
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
                                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                )

                                ExposedDropdownMenu(
                                    expanded = isUnitDropdownExpanded,
                                    onDismissRequest = { isUnitDropdownExpanded = false },
                                    modifier = Modifier
                                        .background(ColorSurfaceWhite)
                                        .heightIn(max = 240.dp)
                                ) {
                                    unitOptions.forEach { unit ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = unit,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = ColorDarkChocolate
                                                )
                                            },
                                            onClick = {
                                                selectedUnit = unit
                                                isUnitDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 7. TANGGAL KEDALUWARSA
                    Text("Tanggal Kedaluwarsa", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = ColorDarkChocolate)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = ColorSoftCream,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable { showDatePicker = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Pilih tanggal kedaluwarsa",
                                tint = ColorDarkChocolate,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = formattedExpiryDate,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorDarkChocolate,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    if (!isManualExpiry) {
                        Text(
                            text = "Perkiraan otomatis, ketuk untuk ubah tanggal",
                            fontSize = 12.sp,
                            color = ColorTextSubtitleBrown,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Sesuai tanggal yang kamu pilih",
                                fontSize = 12.sp,
                                color = ColorTextSubtitleBrown
                            )
                            Text(
                                text = "Pakai perkiraan",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorForestGreen,
                                modifier = Modifier
                                    .clickable { isManualExpiry = false }
                                    .padding(vertical = 4.dp)
                            )
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

                    // 8. TOMBOL SIMPAN UTAMA (hanya aktif jika valid)
                    Button(
                        onClick = {
                            val cat = selectedCategory
                            val qVal = parseQuantity(quantityText)
                            if (itemName.isNotBlank() && cat != null && qVal != null && qVal > 0.0) {
                                val cleanQty = if (qVal % 1.0 == 0.0) {
                                    qVal.toLong().toString()
                                } else {
                                    "%.2f".format(Locale.US, qVal).trimEnd('0').trimEnd('.')
                                }
                                val newItem = PantryItem(
                                    id = System.currentTimeMillis().toString(),
                                    name = itemName.trim(),
                                    location = selectedLocation,
                                    quantityLabel = "$cleanQty $selectedUnit",
                                    expiryEpochDay = expiryLocalDate.toEpochDay(),
                                    category = cat,
                                    isExpiryEstimated = !isManualExpiry
                                )
                                pantryViewModel.addItem(
                                    email = currentUser?.email ?: "guest",
                                    item = newItem
                                )
                                onSaveSuccess()
                            }
                        },
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorDarkChocolate,
                            disabledContainerColor = ColorDarkChocolate.copy(alpha = 0.4f)
                        ),
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
fun PantryStorageTabButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
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