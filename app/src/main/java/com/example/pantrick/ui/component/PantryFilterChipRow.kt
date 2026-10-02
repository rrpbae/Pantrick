// [Materi: LazyRow, Filter Chips & DropdownMenu] Baris penyaring filter tunggal dan pengurutan bahan makanan
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.ColorUrgencyRed
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.data.model.FoodCategory
import com.example.pantrick.util.ChipFilter
import com.example.pantrick.util.PantrickConstants
import com.example.pantrick.util.SortOrder

// [Materi: Stateless Composable] Barisan filter chip tunggal dan selector pengurutan
@Composable
fun PantryFilterChipRow(
    chipFilter: ChipFilter,
    expiringCount: Int,
    availableCategories: List<Pair<FoodCategory, Int>>,
    selectedSortOrder: SortOrder,
    onChipSelected: (ChipFilter) -> Unit,
    onSortOrderSelected: (SortOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSortMenuExpanded by remember { mutableStateOf(false) }

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // [Materi: Dropdown Sort Button] Tombol pengurutan dengan ikon sort dan teks ringkas
        item(key = "chip_sort_selector") {
            Box {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable { isSortMenuExpanded = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(selectedSortOrder.labelRes),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                DropdownMenu(
                    expanded = isSortMenuExpanded,
                    onDismissRequest = { isSortMenuExpanded = false }
                ) {
                    SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(order.labelRes),
                                    fontSize = 13.sp,
                                    fontWeight = if (order == selectedSortOrder) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            trailingIcon = {
                                if (order == selectedSortOrder) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            },
                            onClick = {
                                onSortOrderSelected(order)
                                isSortMenuExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // [Materi: Expiring Chip Filter] Chip untuk memfilter bahan yang segera kedaluwarsa
        if (expiringCount > 0) {
            item(key = "chip_expiring") {
                val isExpiringSelected = chipFilter is ChipFilter.Expiring
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isExpiringSelected) ColorUrgencyRed else MaterialTheme.colorScheme.surface,
                    contentColor = if (isExpiringSelected) Color.White else ColorUrgencyRed,
                    border = if (isExpiringSelected) null else BorderStroke(1.dp, ColorUrgencyRed.copy(alpha = 0.5f)),
                    shadowElevation = if (isExpiringSelected) 2.dp else 0.dp,
                    modifier = Modifier.clickable {
                        onChipSelected(if (isExpiringSelected) ChipFilter.None else ChipFilter.Expiring)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.chip_expiring_soon, expiringCount),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // [Materi: Category Chips] Chip kategori yang tersedia pada lokasi penyimpanan saat ini
        items(availableCategories, key = { it.first.name }) { (category, count) ->
            val isCategorySelected = (chipFilter as? ChipFilter.Category)?.category == category
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isCategorySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (isCategorySelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                border = if (isCategorySelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = if (isCategorySelected) 2.dp else 0.dp,
                modifier = Modifier.clickable {
                    onChipSelected(if (isCategorySelected) ChipFilter.None else ChipFilter.Category(category))
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = category.emoji, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${category.label} ($count)",
                        fontSize = 12.sp,
                        fontWeight = if (isCategorySelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

// [Materi: Preview Light]
@Preview(showBackground = true)
@Composable
fun PantryFilterChipRowLightPreview() {
    PantrickTheme {
        PantryFilterChipRow(
            chipFilter = ChipFilter.Expiring,
            expiringCount = 2,
            availableCategories = listOf(FoodCategory.SUSU_TELUR to 2, FoodCategory.SAYUR_BUAH to 1),
            selectedSortOrder = SortOrder.EXPIRING_SOON,
            onChipSelected = {},
            onSortOrderSelected = {}
        )
    }
}

// [Materi: Preview Dark]
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PantryFilterChipRowDarkPreview() {
    PantrickTheme {
        PantryFilterChipRow(
            chipFilter = ChipFilter.Category(FoodCategory.SUSU_TELUR),
            expiringCount = 0,
            availableCategories = listOf(FoodCategory.SUSU_TELUR to 1, FoodCategory.ROTI_KUE to 3),
            selectedSortOrder = SortOrder.NAME_ASC,
            onChipSelected = {},
            onSortOrderSelected = {}
        )
    }
}

