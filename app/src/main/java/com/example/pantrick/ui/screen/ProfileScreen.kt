package com.example.pantrick.ui.screen

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.data.model.PantryItem
import com.example.pantrick.data.model.User
import com.example.pantrick.ui.component.HomeHeader
import com.example.pantrick.ui.viewmodel.AuthViewModel

@Composable
fun ProfileScreen(
    currentUser: User?,
    items: List<PantryItem>,
    itemsCount: Int,
    authViewModel: AuthViewModel,
    onLogoutConfirmed: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ColorSoftCream)
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. HEADER (Menggunakan HomeHeader agar persis seperti Beranda)
        HomeHeader(
            userName = currentUser?.fullName ?: "Pengguna",
            onNotificationClick = { /* TODO */ },
            onProfileClick = { /* TODO */ }
        )

        // 2. KARTU INFO PENGGUNA
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ColorWarmPeach),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser?.fullName?.take(1)?.uppercase() ?: "A",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorDarkChocolate
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = currentUser?.fullName ?: "Alex Morgan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = ColorDarkChocolate
                            )
                            Text(
                                text = currentUser?.email ?: "alex.morgan@pantrick.app",
                                fontSize = 12.sp,
                                color = ColorTextSubtitleBrown
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = ColorWarmPeach.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFD68A65),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Koki Ramah Lingkungan • Level 4",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD68A65)
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier
                            .size(40.dp)
                            .background(ColorSoftCream, CircleShape)
                    ) {
                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Edit", tint = ColorTextSubtitleBrown, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Berikutnya: Master Pengawet", fontSize = 12.sp, color = ColorTextSubtitleBrown, fontWeight = FontWeight.SemiBold)
                    Text("420 / 500 XP", fontSize = 12.sp, color = ColorForestGreen, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0.84f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = ColorForestGreen,
                    trackColor = ColorIconBoxBg
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. PREFERENSI DAPUR
        SectionTitle(title = "Preferensi Dapur")
        Spacer(modifier = Modifier.height(12.dp))
        SettingsCard {
            SettingsItem(
                icon = Icons.Rounded.NoMeals,
                title = "Pantangan Makanan",
                subtitle = "",
                subtitleContent = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                        PillTag("Vegetarian")
                        PillTag("Bebas kacang")
                    }
                },
                trailingContent = { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = ColorTextSubtitleBrown) }
            )
            HorizontalDivider(color = ColorDividerLine, modifier = Modifier.padding(start = 64.dp, end = 16.dp))
            SettingsItem(
                icon = Icons.Rounded.Timer,
                title = "Pengingat Kedaluwarsa",
                subtitle = "Ringkasan harian jam 09:00",
                trailingContent = {
                    Switch(
                        checked = true,
                        onCheckedChange = { },
                        colors = SwitchDefaults.colors(checkedThumbColor = ColorSurfaceWhite, checkedTrackColor = ColorForestGreen)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. PENGATURAN & BANTUAN
        SectionTitle(title = "Pengaturan & Bantuan")
        Spacer(modifier = Modifier.height(12.dp))
        SettingsCard {
            SettingsItem(icon = Icons.Rounded.NotificationsNone, title = "Saluran Notifikasi", subtitle = "", trailingContent = { ChevronRight() })
            HorizontalDivider(color = ColorDividerLine, modifier = Modifier.padding(start = 64.dp, end = 16.dp))
            SettingsItem(icon = Icons.Rounded.SupportAgent, title = "Bantuan & Masukan", subtitle = "", trailingContent = { ChevronRight() })
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 5. TOMBOL KELUAR
        Button(
            onClick = {
                authViewModel.logout()
                onLogoutConfirmed()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEAEA))
        ) {
            Icon(imageVector = Icons.AutoMirrored.Rounded.ExitToApp, contentDescription = "Keluar", tint = ColorUrgencyRed)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Keluar", color = ColorUrgencyRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Aplikasi Pantrick v2.4.0 • Eco Living Hub",
            color = ColorTextSubtitleBrown,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun SectionTitle(title: String, trailingText: String? = null, trailingColor: Color = ColorTextSubtitleBrown) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ColorDarkChocolate)
        }
        if (trailingText != null) {
            Text(text = trailingText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = trailingColor)
        }
    }
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        content = content
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    iconBgColor: Color = ColorSoftCream,
    title: String,
    subtitle: String,
    subtitleContent: (@Composable () -> Unit)? = null,
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO */ }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iconBgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = ColorDarkChocolate, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ColorDarkChocolate)
            if (subtitle.isNotEmpty()) {
                Text(text = subtitle, fontSize = 12.sp, color = ColorTextSubtitleBrown)
            }
            subtitleContent?.invoke()
        }
        trailingContent()
    }
}

@Composable
fun PillTag(text: String) {
    Surface(color = ColorSoftCream, shape = RoundedCornerShape(8.dp)) {
        Text(text = text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = ColorDarkChocolate, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

@Composable
fun ChevronRight() {
    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = ColorTextSubtitleBrown)
}