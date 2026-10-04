// [Materi: Stateless Composable & Top Header] Header bagian atas layar Pantry dengan avatar dan lonceng notifikasi
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.ColorDarkChocolate
import com.example.pantrick.core.ui.theme.ColorUrgencyRed
import com.example.pantrick.core.ui.theme.ColorWarmPeach
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.data.model.User
import com.example.pantrick.util.PantrickConstants

// [Materi: Stateless Composable] Menampilkan avatar nama, judul layar, dan lonceng notifikasi
@Composable
fun PantryTopHeader(
    currentUser: User?,
    photoPath: String? = null,
    hasUrgentItems: Boolean,
    onBellClick: () -> Unit,
    onAvatarClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = PantrickConstants.HOME_HORIZONTAL_PADDING, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // [Materi: Shared UserAvatar] Avatar bulat dengan foto / inisial
        UserAvatar(
            userName = currentUser?.fullName ?: "Pantry",
            photoPath = photoPath,
            size = 42.dp,
            fontSize = 18.sp,
            onClick = onAvatarClick
        )

        Spacer(modifier = Modifier.width(12.dp))

        // [Materi: Column & Typography] Judul dan subjudul pantry
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.pantry_screen_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorDarkChocolate
            )
            Text(
                text = "Kulkas & Freezer",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // [Materi: Lonceng Notifikasi & Indikator Titik Merah]
        Box(contentAlignment = Alignment.TopEnd) {
            IconButton(onClick = onBellClick) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = stringResource(R.string.cd_notifications),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            if (hasUrgentItems) {
                // Titik merah penanda ada bahan yang segera kedaluwarsa
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, end = 8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ColorUrgencyRed)
                )
            }
        }
    }
}

// [Materi: Preview Light]
@Preview(showBackground = true)
@Composable
fun PantryTopHeaderLightPreview() {
    PantrickTheme {
        PantryTopHeader(
            currentUser = User(fullName = "Budi Santoso", email = "budi@test.com", passwordHash = ""),
            hasUrgentItems = true,
            onBellClick = {}
        )
    }
}

// [Materi: Preview Dark]
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PantryTopHeaderDarkPreview() {
    PantrickTheme {
        PantryTopHeader(
            currentUser = User(fullName = "Budi Santoso", email = "budi@test.com", passwordHash = ""),
            hasUrgentItems = false,
            onBellClick = {}
        )
    }
}
