// [Materi: Material 3 ModalBottomSheet & Detail View] Pop up detail bahan makanan ringkas dan rapi dari inventaris pantry
package com.example.pantrick.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.data.model.FoodCategory
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.StorageLocation
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientDetailSheet(
    item: PantryItem,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = ColorSoftCream,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(ColorTextSubtitleBrown.copy(alpha = 0.35f))
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = modifier
    ) {
        IngredientDetailContent(
            item = item,
            onCloseClick = onDismissRequest
        )
    }
}

@Composable
fun IngredientDetailContent(
    item: PantryItem,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val idLocale = remember { Locale.forLanguageTag("id-ID") }
    val dateFormatter = remember(idLocale) {
        DateTimeFormatter.ofPattern("d MMMM yyyy", idLocale)
    }

    val daysLeft = item.daysLeft
    val formattedExpiryDate = remember(item.expiryEpochDay, dateFormatter) {
        LocalDate.ofEpochDay(item.expiryEpochDay).format(dateFormatter)
    }
    val displayExpiryDate = if (item.isExpiryEstimated) "~$formattedExpiryDate" else formattedExpiryDate

    // Keterangan sisa hari di samping tanggal
    val daysDescription = when {
        daysLeft < -1 -> "Sudah lewat ${-daysLeft} hari"
        daysLeft == -1L -> "Sudah lewat 1 hari"
        daysLeft == 0L -> "Kurang dari 24 jam"
        daysLeft == 1L -> "1 hari lagi"
        else -> "$daysLeft hari lagi"
    }

    // Status chip: "Sudah Kedaluwarsa" (<0), "Perlu Perhatian" (0..3), "Aman" (>3)
    val (statusLabel, statusColor) = when {
        daysLeft < 0 -> Pair(
            stringResource(R.string.status_expired),
            ColorUrgencyRed
        )
        daysLeft in 0..3 -> Pair(
            stringResource(R.string.status_needs_attention),
            ColorUrgencyRed
        )
        else -> Pair(
            stringResource(R.string.status_safe),
            ColorForestGreen
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 12.dp)
    ) {
        // 2. HEADER: ikon + judul "Detail Bahan Makanan" di kiri, tombol X bulat di kanan
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = ColorWarmPeach,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = ColorDarkChocolate,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.detail_ingredient_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorDarkChocolate
                )
            }

            // Tombol X bulat di kanan
            Surface(
                color = ColorSurfaceWhite,
                shape = CircleShape,
                shadowElevation = 1.dp,
                modifier = Modifier.size(30.dp)
            ) {
                IconButton(onClick = onCloseClick) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup",
                        tint = ColorDarkChocolate,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = ColorDarkChocolate.copy(alpha = 0.08f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // 3. CARD UTAMA: ikon/emoji kategori di kiri, badge sisa hari, nama bahan, label kategori
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ikon/Emoji kategori bahan
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorWarmPeach),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.category.emoji,
                        fontSize = 24.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Badge sisa hari
                    val badgePillText = if (item.isExpiryEstimated) "~$daysDescription" else daysDescription
                    Surface(
                        color = statusColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = badgePillText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = item.category.label,
                        fontSize = 12.sp,
                        color = ColorTextSubtitleBrown,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 4. DUA CARD SEJAJAR: Jumlah / Takaran & Lokasi Simpan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card Jumlah / Takaran
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = stringResource(R.string.detail_quantity_title),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorTextSubtitleBrown
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.quantityLabel,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Card Lokasi Simpan
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Text(
                        text = stringResource(R.string.detail_storage_title),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorTextSubtitleBrown
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.location.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 5. CARD TANGGAL KEDALUWARSA: Seragam putih, 1 lapis tanpa kotak ganda, padding rapi
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.detail_expiry_title),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorTextSubtitleBrown
                    )

                    // Chip status (Sudah Kedaluwarsa / Perlu Perhatian / Aman)
                    Surface(
                        color = statusColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorSurfaceWhite,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = displayExpiryDate,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate
                    )
                    Text(
                        text = "•  $daysDescription",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }

                if (item.isExpiryEstimated) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = stringResource(R.string.estimated_expiry_desc),
                        fontSize = 10.sp,
                        color = ColorTextSubtitleBrown
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = ColorDarkChocolate.copy(alpha = 0.08f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // 6. TOMBOL "TUTUP" DI TENGAH BAWAH
        Button(
            onClick = onCloseClick,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorDarkChocolate),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text(
                text = stringResource(R.string.detail_btn_close),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ColorSurfaceWhite
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IngredientDetailSheetPreview() {
    val sampleItem = PantryItem(
        id = "1",
        name = "Daging Ayam Segar",
        location = StorageLocation.KULKAS,
        quantityLabel = "500 gr",
        expiryEpochDay = LocalDate.now().plusDays(1).toEpochDay(),
        category = FoodCategory.DAGING_IKAN,
        isExpiryEstimated = true
    )
    Surface(color = ColorSoftCream) {
        IngredientDetailContent(
            item = sampleItem,
            onCloseClick = {}
        )
    }
}
