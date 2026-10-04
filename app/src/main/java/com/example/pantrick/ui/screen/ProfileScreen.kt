package com.example.pantrick.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.StorageLocation
import com.example.pantrick.data.model.User
import com.example.pantrick.ui.component.UserAvatar
import com.example.pantrick.ui.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    currentUser: User?,
    items: List<PantryItem> = emptyList(),
    itemsCount: Int = 0,
    authViewModel: AuthViewModel,
    onEditProfileClick: () -> Unit = {},
    onChangePasswordClick: () -> Unit = {},
    onLogoutConfirmed: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    val photoPath by authViewModel.profileImagePath.collectAsState()

    var isReminderEnabled by remember(currentUser) {
        mutableStateOf(authViewModel.isExpiryReminderEnabled())
    }

    var showAboutDialog by remember { mutableStateOf(false) }

    // Hitung ringkasan pantry dari satu sumber kebenaran (items)
    val kulkasCount = remember(items) { items.count { it.location == StorageLocation.KULKAS } }
    val freezerCount = remember(items) { items.count { it.location == StorageLocation.FREEZER } }
    val totalCount = remember(items) { kulkasCount + freezerCount }
    val attentionCount = remember(items) { items.count { it.daysLeft <= 3 } }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.logo_pantrick),
                    contentDescription = "Logo Pantrick",
                    modifier = Modifier.size(56.dp)
                )
            },
            title = {
                Text(
                    text = "Pantrick",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = ColorDarkChocolate,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Versi 1.0",
                        fontSize = 13.sp,
                        color = ColorTextSubtitleBrown,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Aplikasi pengelola pantry dan rekomendasi resep.",
                        fontSize = 13.sp,
                        color = ColorDarkChocolate,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("Tutup", color = ColorForestGreen, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = ColorSurfaceWhite,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSoftCream)
            .padding(contentPadding)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 1. PROFILE CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        userName = currentUser?.fullName ?: "Pengguna",
                        photoPath = photoPath,
                        size = 64.dp,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.fullName ?: "Pengguna",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ColorDarkChocolate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = currentUser?.email ?: "-",
                            fontSize = 12.sp,
                            color = ColorTextSubtitleBrown,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                FilledTonalButton(
                    onClick = onEditProfileClick,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = ColorSoftCream,
                        contentColor = ColorDarkChocolate
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = ColorDarkChocolate
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Edit Profil",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2. PANTRY SAYA
        Text(
            text = "Pantry Saya",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = ColorDarkChocolate
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PantryStatColumn(value = totalCount.toString(), label = "Total Bahan", modifier = Modifier.weight(1f))
                    VerticalDivider(modifier = Modifier.height(36.dp), color = ColorDividerLine)
                    PantryStatColumn(value = kulkasCount.toString(), label = "Kulkas", modifier = Modifier.weight(1f))
                    VerticalDivider(modifier = Modifier.height(36.dp), color = ColorDividerLine)
                    PantryStatColumn(value = freezerCount.toString(), label = "Freezer", modifier = Modifier.weight(1f))
                }

                // Baris peringatan hanya muncul jika ada bahan sisa exp 0-3 hari atau lewat
                if (attentionCount > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = ColorDividerLine)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = ColorUrgencyRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$attentionCount bahan perlu perhatian",
                            color = ColorUrgencyRed,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3. PENGATURAN
        Text(
            text = "Pengaturan",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = ColorDarkChocolate
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(ColorSoftCream, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = ColorDarkChocolate,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pengingat Kedaluwarsa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorDarkChocolate
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ingatkan saya ketika bahan mendekati kedaluwarsa.",
                        fontSize = 12.sp,
                        color = ColorTextSubtitleBrown
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isReminderEnabled,
                    onCheckedChange = { isChecked ->
                        isReminderEnabled = isChecked
                        authViewModel.setExpiryReminderEnabled(isChecked)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ColorSurfaceWhite,
                        checkedTrackColor = ColorForestGreen
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. AKUN
        Text(
            text = "Akun",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = ColorDarkChocolate
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onChangePasswordClick() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(ColorSoftCream, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = ColorDarkChocolate,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Ubah Password",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ColorDarkChocolate,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ColorTextSubtitleBrown
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. TENTANG
        Text(
            text = "Tentang",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = ColorDarkChocolate
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAboutDialog = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(ColorSoftCream, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = ColorDarkChocolate,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Tentang Pantrick",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorDarkChocolate
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Pantrick v1.0",
                        fontSize = 12.sp,
                        color = ColorTextSubtitleBrown
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = ColorTextSubtitleBrown
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. TOMBOL KELUAR
        Button(
            onClick = {
                authViewModel.logout()
                onLogoutConfirmed()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEAEA))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ExitToApp,
                contentDescription = "Keluar",
                tint = ColorUrgencyRed
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Keluar",
                color = ColorUrgencyRed,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PantryStatColumn(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ColorDarkChocolate
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = ColorTextSubtitleBrown
        )
    }
}