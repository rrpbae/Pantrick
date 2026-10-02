// [Materi: Reusable Composable Component] Dua kartu ringkasan status pantry (Total Bahan & Hampir Kedaluwarsa)
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.pantrick.core.ui.theme.ColorIconBoxBg
import com.example.pantrick.core.ui.theme.ColorUrgencyRed
import com.example.pantrick.core.ui.theme.ColorWarmPeach
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.util.PantrickConstants

// [Materi: Stateless Composable] Menerima totalCount dan expiringCount dari data nyata pengguna
@Composable
fun SummaryCardsRow(
    totalCount: Int,
    expiringCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // [Materi: Card & Weight] Kartu Kiri: "Total Bahan" (Permukaan Putih/Off-White)
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(PantrickConstants.CARD_CORNER_RADIUS),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Baris Ikon dan Badge "TERKINI"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ColorIconBoxBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Kitchen,
                            contentDescription = stringResource(R.string.cd_pantry_icon),
                            tint = ColorForestGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Badge "TERKINI"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEDE8E1))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.summary_total_badge),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6B625B),
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Angka besar dan label unit "bahan"
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = totalCount.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.summary_unit_ingredient),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Judul kartu dan subteks lokasi
                Text(
                    text = stringResource(R.string.summary_total_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorDarkChocolate
                )

                Text(
                    text = stringResource(R.string.summary_total_sub),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // [Materi: Card & Weight] Kartu Kanan: "Hampir Kedaluwarsa" (Permukaan Warm Peach)
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(PantrickConstants.CARD_CORNER_RADIUS),
            colors = CardDefaults.cardColors(containerColor = ColorWarmPeach),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Baris Ikon dan Badge "SEGERA" (Merah)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.65f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = stringResource(R.string.cd_timer_icon),
                            tint = ColorUrgencyRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Badge "SEGERA"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ColorUrgencyRed)
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.summary_urgent_badge),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onError,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Angka besar dan label unit
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = expiringCount.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.summary_unit_ingredient),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = ColorDarkChocolate.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Judul kartu dan subteks durasi
                Text(
                    text = stringResource(R.string.summary_urgent_title),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorDarkChocolate
                )

                Text(
                    text = stringResource(R.string.summary_urgent_sub),
                    fontSize = 12.sp,
                    color = ColorDarkChocolate.copy(alpha = 0.7f)
                )
            }
        }
    }
}

// [Materi: Preview Light] Pratinjau SummaryCardsRow mode terang
@Preview(showBackground = true)
@Composable
fun SummaryCardsRowPreview() {
    PantrickTheme {
        SummaryCardsRow(
            totalCount = 3,
            expiringCount = 2
        )
    }
}

// [Materi: Preview Dark] Pratinjau SummaryCardsRow mode gelap
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SummaryCardsRowDarkPreview() {
    PantrickTheme(darkTheme = true) {
        SummaryCardsRow(
            totalCount = 0,
            expiringCount = 0
        )
    }
}
