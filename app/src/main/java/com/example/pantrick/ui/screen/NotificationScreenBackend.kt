// [Materi: Notification & Backend Integration] Layar notifikasi menggunakan backend API
package com.example.pantrick.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.data.model.ExpirationPriority
import com.example.pantrick.data.model.ExpirationStatus
import com.example.pantrick.data.model.NotificationItemDto
import com.example.pantrick.ui.viewmodel.NotificationUiState
import com.example.pantrick.ui.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreenBackend(
    jwtToken: String,
    onNavigateBack: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
    viewModel: NotificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(jwtToken) {
        if (jwtToken.isNotBlank()) {
            viewModel.loadNotifications(jwtToken)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Notifikasi Kedaluwarsa",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorDarkChocolate
                        )
                        if (uiState is NotificationUiState.Success) {
                            val unreadCount = (uiState as NotificationUiState.Success).unreadCount
                            if (unreadCount > 0) {
                                Spacer(Modifier.width(8.dp))
                                Surface(
                                    color = ColorUrgencyRed,
                                    shape = CircleShape,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = unreadCount.toString(),
                                            color = ColorSurfaceWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back_button),
                            tint = ColorDarkChocolate
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorSoftCream
                )
            )
        },
        containerColor = ColorSoftCream,
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is NotificationUiState.Idle -> {
                    // Tidak tampilkan apa-apa, tunggu load
                }

                is NotificationUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ColorForestGreen)
                    }
                }

                is NotificationUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = ColorTextSubtitleBrown.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Gagal memuat notifikasi",
                                fontSize = 15.sp,
                                color = ColorDarkChocolate,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Silakan coba lagi",
                                fontSize = 13.sp,
                                color = ColorTextSubtitleBrown
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (jwtToken.isNotBlank()) {
                                        viewModel.loadNotifications(jwtToken)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ColorForestGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Coba Lagi")
                            }
                        }
                    }
                }

                is NotificationUiState.Success -> {
                    val notifications = state.notifications

                    if (notifications.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(ColorSurfaceWhite),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = ColorForestGreen,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Belum ada notifikasi kedaluwarsa",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ColorTextSubtitleBrown
                                )
                            }
                        }
                    } else {
                        // Grup notifikasi berdasarkan priority
                        val highPriority = notifications.filter { it.priority == ExpirationPriority.HIGH }
                        val mediumPriority = notifications.filter { it.priority == ExpirationPriority.MEDIUM }
                        val lowPriority = notifications.filter { it.priority == ExpirationPriority.LOW }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (highPriority.isNotEmpty()) {
                                item {
                                    NotificationGroupHeader(
                                        title = "Peringatan Penting",
                                        badgeColor = ColorUrgencyRed
                                    )
                                }
                                items(highPriority) { notif ->
                                    NotificationItemCard(
                                        notification = notif,
                                        jwtToken = jwtToken,
                                        viewModel = viewModel
                                    )
                                }
                            }

                            if (mediumPriority.isNotEmpty()) {
                                item {
                                    NotificationGroupHeader(
                                        title = "Segera Kedaluwarsa",
                                        badgeColor = ColorWarmPeach
                                    )
                                }
                                items(mediumPriority) { notif ->
                                    NotificationItemCard(
                                        notification = notif,
                                        jwtToken = jwtToken,
                                        viewModel = viewModel
                                    )
                                }
                            }

                            if (lowPriority.isNotEmpty()) {
                                item {
                                    NotificationGroupHeader(
                                        title = "Pengingat 7 Hari",
                                        badgeColor = ColorForestGreen.copy(alpha = 0.2f),
                                        labelColor = ColorForestGreen
                                    )
                                }
                                items(lowPriority) { notif ->
                                    NotificationItemCard(
                                        notification = notif,
                                        jwtToken = jwtToken,
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationGroupHeader(
    title: String,
    badgeColor: Color,
    labelColor: Color = ColorDarkChocolate
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(badgeColor)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = labelColor
        )
    }
}

@Composable
private fun NotificationItemCard(
    notification: NotificationItemDto,
    jwtToken: String,
    viewModel: NotificationViewModel
) {
    val days = notification.daysRemaining
    val daysLabel = when {
        days < 0 -> "${-days} hari lewat"
        days == 0L -> "Hari ini"
        days == 1L -> "Besok"
        days == 7L -> "7 hari lagi"
        else -> "$days hari lagi"
    }

    val textColor = when {
        notification.type == ExpirationStatus.EXPIRED -> ColorUrgencyRed
        notification.priority == ExpirationPriority.HIGH -> ColorUrgencyRed
        else -> ColorDarkChocolate
    }

    val bgColor = if (notification.isRead) {
        ColorSurfaceWhite.copy(alpha = 0.6f)
    } else {
        ColorSurfaceWhite
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (!notification.isRead && jwtToken.isNotBlank()) {
                    viewModel.markAsRead(notification.id, jwtToken)
                }
            },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    color = textColor,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.name,
                    fontSize = 11.sp,
                    color = ColorTextSubtitleBrown
                )
            }
            Spacer(Modifier.width(12.dp))
            Surface(
                color = if (days <= 1) ColorUrgencyRed.copy(alpha = 0.15f) else ColorSoftCream,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = daysLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
