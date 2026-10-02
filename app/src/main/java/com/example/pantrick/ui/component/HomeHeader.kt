// [Materi: Reusable Composable Component] Header layar utama Pantrick dengan inisial dinamis pengguna
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.ColorForestGreen
import com.example.pantrick.core.ui.theme.ColorUrgencyRed
import com.example.pantrick.core.ui.theme.ColorWarmPeach
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.util.PantrickConstants

// [Materi: Stateless Composable] Menampilkan logo, nama brand, lonceng notifikasi, dan avatar inisial pengguna
@Composable
fun HomeHeader(
    userName: String,
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // [Materi: Dynamic String Processing] Mengambil inisial huruf pertama nama pengguna
    val initial = userName.trim().take(1).uppercase().ifEmpty { "?" }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // [Materi: Row & Image Resource] Logo kecil Pantrick dan wordmark brand
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

        // [Materi: Row & Badge Notification] Ikon lonceng dengan titik merah urgensi + avatar inisial pengguna
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = stringResource(R.string.cd_notifications),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // [Materi: Box Decoration] Titik merah indikator notifikasi
                Box(
                    modifier = Modifier
                        .padding(top = 7.dp, end = 7.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ColorUrgencyRed)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // [Materi: Circle Avatar] Lingkaran avatar warna peach berisi inisial nama pengguna login
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ColorWarmPeach)
                    .clickable { onProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

// [Materi: Preview Light] Pratinjau komponen HomeHeader dalam mode terang
@Preview(showBackground = true)
@Composable
fun HomeHeaderPreview() {
    PantrickTheme {
        HomeHeader(userName = "Budi Santoso")
    }
}

// [Materi: Preview Dark] Pratinjau komponen HomeHeader dalam mode gelap
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeHeaderDarkPreview() {
    PantrickTheme(darkTheme = true) {
        HomeHeader(userName = "Budi Santoso")
    }
}
