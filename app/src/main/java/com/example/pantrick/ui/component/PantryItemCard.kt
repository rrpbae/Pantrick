// [Materi: Card, DropdownMenu & Item Layout] Kartu bahan makanan pada grid pantry dengan aksi centang dan menu titik tiga
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.example.pantrick.data.model.FoodCategory
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.util.ExpiryStatus

// [Materi: Stateless Composable] Kartu bahan individual dengan centang selesai olah, pill status kedaluwarsa, dan menu opsi
@Composable
fun PantryItemCard(
    item: PantryItem,
    onMarkUsed: (PantryItem) -> Unit,
    onEditClick: (PantryItem) -> Unit,
    onMoveClick: (PantryItem) -> Unit,
    onDeleteClick: (PantryItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val expiryInfo = ExpiryStatus.evaluate(item.daysLeft)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (expiryInfo.isUrgent) ColorUrgencyRed.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // [Materi: Baris Atas] Emoji Kategori + Tombol Centang Selesai + Menu Titik Tiga
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji Kategori
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = item.category.emoji, fontSize = 18.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Tombol Centang Selesai Olah
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .clickable { onMarkUsed(item) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Tandai terpakai",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Menu Titik Tiga
                    Box {
                        IconButton(
                            onClick = { isMenuExpanded = true },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Menu opsi bahan",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.action_edit)) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    onEditClick(item)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Pindahkan ke...") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.DriveFileMove,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    onMoveClick(item)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = stringResource(R.string.action_delete),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                onClick = {
                                    isMenuExpanded = false
                                    onDeleteClick(item)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Nama Bahan
            Text(
                text = item.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = ColorDarkChocolate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Jumlah Bahan
            Text(
                text = item.quantityLabel,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // [Materi: Pill Status Kedaluwarsa dari ExpiryStatus]
            val pillBgColor = if (expiryInfo.isUrgent) {
                ColorUrgencyRed.copy(alpha = 0.12f)
            } else {
                ColorForestGreen.copy(alpha = 0.12f)
            }
            val pillTextColor = if (expiryInfo.isUrgent) ColorUrgencyRed else ColorForestGreen

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = pillBgColor
            ) {
                Text(
                    text = if (item.isExpiryEstimated) "~${expiryInfo.pillLabel}" else expiryInfo.pillLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = pillTextColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// [Materi: Preview Light]
@Preview(showBackground = true)
@Composable
fun PantryItemCardLightPreview() {
    PantrickTheme {
        Box(modifier = Modifier.padding(16.dp).width(180.dp)) {
            PantryItemCard(
                item = PantryItem(
                    id = "1",
                    name = "Susu UHT",
                    location = StorageLocation.KULKAS,
                    quantityLabel = "1 Liter",
                    expiryEpochDay = java.time.LocalDate.now().plusDays(1).toEpochDay(),
                    category = FoodCategory.SUSU_TELUR
                ),
                onMarkUsed = {},
                onEditClick = {},
                onMoveClick = {},
                onDeleteClick = {}
            )
        }
    }
}

// [Materi: Preview Dark]
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PantryItemCardDarkPreview() {
    PantrickTheme {
        Box(modifier = Modifier.padding(16.dp).width(180.dp)) {
            PantryItemCard(
                item = PantryItem(
                    id = "2",
                    name = "Beras Putih",
                    location = StorageLocation.KULKAS,
                    quantityLabel = "5 kg",
                    expiryEpochDay = java.time.LocalDate.now().plusDays(30).toEpochDay(),
                    category = FoodCategory.BUMBU_KERING
                ),
                onMarkUsed = {},
                onEditClick = {},
                onMoveClick = {},
                onDeleteClick = {}
            )
        }
    }
}
