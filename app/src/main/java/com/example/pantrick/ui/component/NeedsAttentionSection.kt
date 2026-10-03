// [Materi: LazyRow & Dynamic List] Section bahan yang butuh perhatian segera (Perlu Diperhatikan)
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.ColorDarkChocolate
import com.example.pantrick.core.ui.theme.ColorForestGreen
import com.example.pantrick.core.ui.theme.ColorUrgencyRed
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.util.ExpiryStatus
import com.example.pantrick.util.PantrickConstants

// [Materi: Stateless Composable] Menampilkan daftar kartu bahan expiring soon dengan empty-state handling
@Composable
fun NeedsAttentionSection(
    items: List<PantryItem>,
    hasAnyExpiringItemsOverall: Boolean,
    onViewAllClick: () -> Unit = {},
    onFindRecipeClick: (PantryItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // [Materi: Row & Alignment] Header Section: Titik merah + Judul bold + "Lihat Semua ›"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Titik merah penanda urgensi
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ColorUrgencyRed)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = stringResource(R.string.attention_title),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorDarkChocolate
                )
            }

            Text(
                text = stringResource(R.string.attention_view_all),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = ColorForestGreen,
                modifier = Modifier.clickable { onViewAllClick() }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // [Materi: Conditional UI / Empty State Text] Penanganan jika tidak ada bahan mendesak
        if (items.isEmpty()) {
            val emptyMessage = if (!hasAnyExpiringItemsOverall) {
                stringResource(R.string.attention_all_safe)
            } else {
                stringResource(R.string.attention_category_empty)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING, vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emptyMessage,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            // [Materi: LazyRow & items] Mengurutkan berdasarkan daysLeft terkecil
            val sortedItems = items.sortedBy { it.daysLeft }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(sortedItems, key = { it.id }) { item ->
                    ExpiringItemCard(
                        item = item,
                        onFindRecipeClick = { onFindRecipeClick(item) }
                    )
                }
            }
        }
    }
}

// [Materi: Reusable Component] Kartu individual bahan makanan dengan badge kedaluwarsa sesuai daysLeft
@Composable
fun ExpiringItemCard(
    item: PantryItem,
    onFindRecipeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // [Materi: ExpiryStatus Helper] Format badge kedaluwarsa sesuai single source of truth ExpiryStatus
    val expiryInfo = ExpiryStatus.evaluate(item.daysLeft)
    val badgeText = if (item.isExpiryEstimated) "~${expiryInfo.badgeLabel}" else expiryInfo.badgeLabel
    val badgeBgColor = if (expiryInfo.isUrgent) ColorUrgencyRed else Color(0xB34A2C21)

    // [Materi: if-else Expression] Baris detail teks kadaluarsa
    val isUrgent = expiryInfo.isUrgent
    val pillLabel = if (item.isExpiryEstimated) "~${expiryInfo.pillLabel}" else expiryInfo.pillLabel
    val detailText = "$pillLabel • ${item.quantityLabel}"
    val detailColor = if (isUrgent) ColorUrgencyRed else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = modifier.width(PantrickConstants.EXPIRING_CARD_WIDTH),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // [Materi: Box & Image] Area gambar bahan dengan overlay badge sisa hari di pojok kiri atas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(116.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_placeholder_ingredient),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(116.dp)
                )

                // Badge sisa hari
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeBgColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = badgeText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }

            // Area konten teks di bawah gambar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorDarkChocolate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = detailText,
                    fontSize = 11.sp,
                    fontWeight = if (isUrgent) FontWeight.SemiBold else FontWeight.Normal,
                    color = detailColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(10.dp))

                // [Materi: Surface Button] Tombol penuh "Cari Resep" berlatar krem halus
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF7EBE1),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFindRecipeClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = ColorDarkChocolate,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.btn_find_recipe),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorDarkChocolate
                        )
                    }
                }
            }
        }
    }
}

// [Materi: Preview Light] Pratinjau section Needs Attention First mode terang
@Preview(showBackground = true)
@Composable
fun NeedsAttentionSectionPreview() {
    PantrickTheme {
        NeedsAttentionSection(
            items = listOf(
                PantryItem(
                    id = "1",
                    name = "Susu UHT",
                    location = StorageLocation.KULKAS,
                    quantityLabel = "1 liter",
                    expiryEpochDay = java.time.LocalDate.now().plusDays(1).toEpochDay()
                )
            ),
            hasAnyExpiringItemsOverall = true
        )
    }
}
