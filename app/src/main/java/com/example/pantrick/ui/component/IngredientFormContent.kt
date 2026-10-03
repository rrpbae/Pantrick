// [Materi: Shared Composable & Form Design System] Komponen form input bahan makanan yang dapat digunakan bersama oleh layar Tambah dan Edit
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.ColorDarkChocolate
import com.example.pantrick.core.ui.theme.ColorForestGreen
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.data.model.FoodCategory
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.util.PantrickConstants

// [Materi: Stateless Composable] Komponen form bahan makanan yang dipakai bersama untuk Tambah & Edit
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientFormContent(
    title: String,
    subtitle: String,
    name: String,
    onNameChange: (String) -> Unit,
    isNameError: Boolean,
    category: FoodCategory,
    onCategoryChange: (FoodCategory) -> Unit,
    location: StorageLocation,
    onLocationChange: (StorageLocation) -> Unit,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    isQuantityError: Boolean,
    formattedExpiryDate: String,
    isExpiryEstimated: Boolean = true,
    onResetToEstimated: () -> Unit = {},
    onSelectDateClick: () -> Unit,
    isFormValid: Boolean,
    isSaving: Boolean,
    submitButtonText: String,
    onSaveClick: () -> Unit,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PantrickConstants.SCREEN_PADDING)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // [Materi: Header Layar & Tombol Kembali]
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back_button),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorDarkChocolate
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Nama Bahan
        Text(
            text = stringResource(R.string.item_name_label),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = ColorDarkChocolate
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text(stringResource(R.string.item_name_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ShoppingBag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            isError = isNameError,
            supportingText = if (isNameError) {
                { Text("Nama bahan wajib diisi", color = MaterialTheme.colorScheme.error) }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(PantrickConstants.CORNER_RADIUS),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Kategori Bahan (Dropdown)
        Text(
            text = stringResource(R.string.category_label),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = ColorDarkChocolate
        )
        Spacer(modifier = Modifier.height(6.dp))
        ExposedDropdownMenuBox(
            expanded = isCategoryDropdownExpanded,
            onExpandedChange = { isCategoryDropdownExpanded = !isCategoryDropdownExpanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = "${category.emoji}  ${category.label}",
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded)
                },
                shape = RoundedCornerShape(PantrickConstants.CORNER_RADIUS),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = isCategoryDropdownExpanded,
                onDismissRequest = { isCategoryDropdownExpanded = false }
            ) {
                FoodCategory.entries.forEach { itemCategory ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = itemCategory.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = itemCategory.label, fontSize = 14.sp)
                            }
                        },
                        onClick = {
                            onCategoryChange(itemCategory)
                            isCategoryDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Lokasi Penyimpanan (3 Segmented Buttons: Kulkas, Freezer, Rak Kering)
        Text(
            text = stringResource(R.string.storage_location_label),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = ColorDarkChocolate
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(PantrickConstants.CORNER_RADIUS))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StorageLocation.entries.forEach { loc ->
                val isSelected = loc == location
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) ColorForestGreen
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f)
                        )
                        .clickable { onLocationChange(loc) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = loc.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Jumlah / Satuan
        Text(
            text = stringResource(R.string.quantity_label),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = ColorDarkChocolate
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = quantity,
            onValueChange = onQuantityChange,
            placeholder = { Text(stringResource(R.string.quantity_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Scale,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            isError = isQuantityError,
            supportingText = if (isQuantityError) {
                { Text("Jumlah bahan wajib diisi", color = MaterialTheme.colorScheme.error) }
            } else null,
            singleLine = true,
            shape = RoundedCornerShape(PantrickConstants.CORNER_RADIUS),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Tanggal Kedaluwarsa
        Text(
            text = stringResource(R.string.expiry_date_label),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = ColorDarkChocolate
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectDateClick() }
        ) {
            OutlinedTextField(
                value = formattedExpiryDate,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text(stringResource(R.string.expiry_date_placeholder)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = stringResource(R.string.cd_select_date),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                enabled = false,
                shape = RoundedCornerShape(PantrickConstants.CORNER_RADIUS),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        if (isExpiryEstimated) {
            Text(
                text = "Perkiraan otomatis, ketuk untuk ubah tanggal",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Pakai perkiraan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorForestGreen,
                    modifier = Modifier
                        .clickable { onResetToEstimated() }
                        .padding(vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 6. Tombol Aksi Simpan
        Button(
            onClick = onSaveClick,
            enabled = isFormValid && !isSaving,
            shape = RoundedCornerShape(PantrickConstants.CORNER_RADIUS),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorForestGreen,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(PantrickConstants.BUTTON_HEIGHT)
        ) {
            Text(
                text = submitButtonText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// [Materi: Preview Light]
@Preview(showBackground = true)
@Composable
fun IngredientFormContentLightPreview() {
    PantrickTheme {
        IngredientFormContent(
            title = "Tambah Bahan",
            subtitle = "Catat bahan makanan baru ke dalam pantry kamu",
            name = "Susu Kotak",
            onNameChange = {},
            isNameError = false,
            category = FoodCategory.SUSU_TELUR,
            onCategoryChange = {},
            location = StorageLocation.KULKAS,
            onLocationChange = {},
            quantity = "1 Liter",
            onQuantityChange = {},
            isQuantityError = false,
            formattedExpiryDate = "12 Okt 2026",
            onSelectDateClick = {},
            isFormValid = true,
            isSaving = false,
            submitButtonText = "Simpan Bahan",
            onSaveClick = {},
            onNavigateBack = {}
        )
    }
}
