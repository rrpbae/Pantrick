// [Materi: Card, Alert Banner & Localization] Banner peringatan bahan kedaluwarsa dengan tombol aksi rencana masak
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.pantrick.core.ui.theme.ColorUrgencyRed
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.util.BannerInfo
import com.example.pantrick.util.PantrickConstants

// [Materi: Stateless Composable] Banner peringatan kedaluwarsa dengan teks yang dirangkai dari string resources
@Composable
fun ExpiryAlertBanner(
    bannerInfo: BannerInfo,
    onPlanCookingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (bannerInfo.totalCount <= 0) return

    val subtitle = when {
        bannerInfo.hasOverdue -> {
            stringResource(R.string.banner_overdue_warning)
        }
        bannerInfo.extraCount > 0 -> {
            stringResource(
                R.string.banner_subtitle_extra,
                bannerInfo.firstNames.joinToString(", "),
                bannerInfo.extraCount
            )
        }
        bannerInfo.firstNames.size > 1 -> {
            stringResource(
                R.string.banner_subtitle_multiple,
                bannerInfo.firstNames.joinToString(", ")
            )
        }
        else -> {
            stringResource(
                R.string.banner_subtitle_single,
                bannerInfo.firstNames.firstOrNull().orEmpty()
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = ColorUrgencyRed.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ikon lingkaran peringatan
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ColorUrgencyRed.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = ColorUrgencyRed,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.banner_title, bannerInfo.totalCount),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorUrgencyRed
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // [Materi: Action Button] Tombol Rencanakan Masak
            Button(
                onClick = onPlanCookingClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorUrgencyRed,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 0.dp)
            ) {
                Text(
                    text = stringResource(R.string.btn_plan_cooking),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// [Materi: Preview Light]
@Preview(showBackground = true)
@Composable
fun ExpiryAlertBannerLightPreview() {
    PantrickTheme {
        ExpiryAlertBanner(
            bannerInfo = BannerInfo(
                firstNames = listOf("Susu", "Telur"),
                extraCount = 1,
                hasOverdue = false,
                totalCount = 3
            ),
            onPlanCookingClick = {}
        )
    }
}

// [Materi: Preview Dark]
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ExpiryAlertBannerDarkPreview() {
    PantrickTheme {
        ExpiryAlertBanner(
            bannerInfo = BannerInfo(
                firstNames = listOf("Bayam"),
                extraCount = 0,
                hasOverdue = true,
                totalCount = 1
            ),
            onPlanCookingClick = {}
        )
    }
}
