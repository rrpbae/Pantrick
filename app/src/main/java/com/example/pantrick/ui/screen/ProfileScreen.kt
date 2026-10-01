// [Materi: Compose Screen & Architecture] Layar Profil Pengguna dan Manajemen Akun
// Menerapkan AlertDialog konfirmasi keluar aplikasi, avatar dinamis, dan pembersihan sesi
package com.example.pantrick.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Kitchen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.ColorDarkChocolate
import com.example.pantrick.core.ui.theme.ColorForestGreen
import com.example.pantrick.core.ui.theme.ColorWarmPeach
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.data.model.User
import com.example.pantrick.ui.viewmodel.AuthViewModel
import com.example.pantrick.util.PantrickConstants

import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.StorageLocation

// [Materi: Stateful Composable] Mengelola state dialog konfirmasi logout dan integrasi AuthViewModel
@Composable
fun ProfileScreen(
    currentUser: User?,
    items: List<PantryItem> = emptyList(),
    itemsCount: Int = items.size,
    authViewModel: AuthViewModel,
    onLogoutConfirmed: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    val kulkasCount = remember(items) { items.count { it.location == StorageLocation.KULKAS } }
    val freezerCount = remember(items) { items.count { it.location == StorageLocation.FREEZER } }
    val rakKeringCount = remember(items) { items.count { it.location == StorageLocation.RAK_KERING } }

    // [Materi: AlertDialog Konfirmasi Logout]
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.logout_dialog_title),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.logout_dialog_desc),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        onLogoutConfirmed()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.action_logout),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }

    StatelessProfileContent(
        fullName = currentUser?.fullName.orEmpty().ifBlank { "Pengguna Pantrick" },
        email = currentUser?.email.orEmpty().ifBlank { "-" },
        itemsCount = if (items.isNotEmpty()) items.size else itemsCount,
        kulkasCount = kulkasCount,
        freezerCount = freezerCount,
        rakKeringCount = rakKeringCount,
        onLogoutClick = { showLogoutDialog = true },
        contentPadding = contentPadding,
        modifier = modifier
    )
}

// [Materi: Stateless Composable] UI murni layar profil pengguna
@Composable
fun StatelessProfileContent(
    fullName: String,
    email: String,
    itemsCount: Int,
    kulkasCount: Int = 0,
    freezerCount: Int = 0,
    rakKeringCount: Int = 0,
    onLogoutClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val initial = fullName.trim().take(1).uppercase().ifEmpty { "?" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(contentPadding)
            .padding(horizontal = PantrickConstants.SCREEN_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(28.dp))

        // 1. Avatar Inisial Besar
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(ColorWarmPeach),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Nama Lengkap Pengguna
        Text(
            text = fullName,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = ColorDarkChocolate,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Email Pengguna
        Text(
            text = email,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 4. Kartu Informasi Statistik Pantry
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(PantrickConstants.CARD_CORNER_RADIUS),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ColorForestGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Kitchen,
                        contentDescription = null,
                        tint = ColorForestGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.profile_stored_items),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$itemsCount bahan",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate
                    )
                }
            }

            // [Materi: Breakdown per Lokasi Termasuk Rak Kering]
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Kulkas",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$kulkasCount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Freezer",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$freezerCount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Rak Kering",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$rakKeringCount",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 5. Tombol Keluar (Logout)
        Button(
            onClick = onLogoutClick,
            shape = RoundedCornerShape(PantrickConstants.CORNER_RADIUS),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(PantrickConstants.BUTTON_HEIGHT)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Logout,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.btn_logout),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// [Materi: Preview Light]
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    PantrickTheme {
        StatelessProfileContent(
            fullName = "Budi Santoso",
            email = "budi@contoh.com",
            itemsCount = 3,
            onLogoutClick = {},
            contentPadding = PaddingValues(0.dp)
        )
    }
}
