// [Materi: TabRow & Navigation Tabs] Baris tab lokasi penyimpanan (Kulkas, Freezer, Rak Kering) dengan jumlah item
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.ColorForestGreen
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.util.PantrickConstants

// [Materi: Stateless Composable] Baris tab dengan 2 lokasi: Kulkas dan Freezer
@Composable
fun LocationTabRow(
    selectedLocation: StorageLocation,
    kulkasCount: Int,
    freezerCount: Int,
    onLocationSelected: (StorageLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    val locations = listOf(
        StorageLocation.KULKAS to kulkasCount,
        StorageLocation.FREEZER to freezerCount
    )

    // [Materi: Custom Segmented Container] Wadah rounded untuk kedua tab lokasi
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        locations.forEach { (location, count) ->
            val isSelected = location == selectedLocation
            val label = when (location) {
                StorageLocation.KULKAS -> stringResource(R.string.loc_fridge)
                StorageLocation.FREEZER -> stringResource(R.string.loc_freezer)
            }

            // [Materi: Tab Pill Item] Pill aktif berlatar Forest Green dengan teks tebal putih
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (isSelected) ColorForestGreen else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f)
                    )
                    .clickable { onLocationSelected(location) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$label ($count)",
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// [Materi: Preview Light]
@Preview(showBackground = true)
@Composable
fun LocationTabRowLightPreview() {
    PantrickTheme {
        LocationTabRow(
            selectedLocation = StorageLocation.KULKAS,
            kulkasCount = 5,
            freezerCount = 2,
            onLocationSelected = {}
        )
    }
}

// [Materi: Preview Dark]
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LocationTabRowDarkPreview() {
    PantrickTheme {
        LocationTabRow(
            selectedLocation = StorageLocation.FREEZER,
            kulkasCount = 5,
            freezerCount = 2,
            onLocationSelected = {}
        )
    }
}
