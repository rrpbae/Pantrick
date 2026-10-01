// [Materi: LazyRow & Filter Chips] Barisan chip penyaring lokasi penyimpanan yang dapat digulir horizontal
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.util.PantrickConstants

// [Materi: Stateless Composable] Menerima daftar kategori ("Semua", "Kulkas", "Freezer") beserta jumlah item terhitung
@Composable
fun CategoryFilterChips(
    categoriesWithCount: List<Pair<String, Int>>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // [Materi: LazyRow] Layout baris berkinerja tinggi untuk daftar chip horizontal
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categoriesWithCount, key = { it.first }) { (category, count) ->
            val isSelected = category == selectedCategory
            val label = "$category ($count)"

            // [Materi: Custom Chip Surface] Chip aktif: Hijau tua + ikon centang + teks putih; Chip nonaktif: Putih + border
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                shadowElevation = if (isSelected) 2.dp else 0.dp,
                modifier = Modifier.clickable { onCategorySelected(category) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Terpilih",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

// [Materi: Preview Light] Pratinjau filter chips mode terang
@Preview(showBackground = true)
@Composable
fun CategoryFilterChipsPreview() {
    PantrickTheme {
        CategoryFilterChips(
            categoriesWithCount = listOf(
                "Semua" to 3,
                "Kulkas" to 2,
                "Freezer" to 1
            ),
            selectedCategory = "Semua",
            onCategorySelected = {}
        )
    }
}

// [Materi: Preview Dark] Pratinjau filter chips mode gelap
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CategoryFilterChipsDarkPreview() {
    PantrickTheme(darkTheme = true) {
        CategoryFilterChips(
            categoriesWithCount = listOf(
                "Semua" to 0,
                "Kulkas" to 0,
                "Freezer" to 0
            ),
            selectedCategory = "Semua",
            onCategorySelected = {}
        )
    }
}
