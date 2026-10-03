// [Materi: Notification & Expiry Tracking] Layar notifikasi pengingat kedaluwarsa bahan pantry
package com.example.pantrick.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.data.model.PantryItem
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    items: List<PantryItem>,
    onNavigateBack: () -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    val todayEpochDay = remember { LocalDate.now().toEpochDay() }

    // Hitung sisa hari dari field exp asli yang ada di PantryItem
    val expiringItems = remember(items, todayEpochDay) {
        items.map { item ->
            val days = item.daysLeft(todayEpochDay)
            Triple(item, days, item.isExpiryEstimated)
        }.filter { it.second <= 7 } // Bahan lebih dari 7 hari tidak ditampilkan
    }

    val expiredTodayOrPast = remember(expiringItems) {
        expiringItems.filter { it.second <= 0 }
    }
    val threeDaysLeft = remember(expiringItems) {
        expiringItems.filter { it.second in 1..3 }
    }
    val sevenDaysLeft = remember(expiringItems) {
        expiringItems.filter { it.second in 4..7 }
    }

    val hasAnyExpiring = expiringItems.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notifikasi Kedaluwarsa",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate
                    )
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
        if (!hasAnyExpiring) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
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
                        text = "Tidak ada bahan yang mendekati kedaluwarsa.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = ColorTextSubtitleBrown
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (expiredTodayOrPast.isNotEmpty()) {
                    item {
                        NotificationGroupHeader(
                            title = "Kedaluwarsa hari ini",
                            badgeColor = ColorUrgencyRed
                        )
                    }
                    items(expiredTodayOrPast) { (item, days, isEstimated) ->
                        NotificationItemRow(
                            item = item,
                            days = days,
                            isEstimated = isEstimated,
                            textColor = ColorUrgencyRed
                        )
                    }
                }

                if (threeDaysLeft.isNotEmpty()) {
                    item {
                        NotificationGroupHeader(
                            title = "3 hari lagi",
                            badgeColor = ColorWarmPeach
                        )
                    }
                    items(threeDaysLeft) { (item, days, isEstimated) ->
                        NotificationItemRow(
                            item = item,
                            days = days,
                            isEstimated = isEstimated,
                            textColor = ColorDarkChocolate
                        )
                    }
                }

                if (sevenDaysLeft.isNotEmpty()) {
                    item {
                        NotificationGroupHeader(
                            title = "7 hari lagi",
                            badgeColor = ColorForestGreen.copy(alpha = 0.2f),
                            labelColor = ColorForestGreen
                        )
                    }
                    items(sevenDaysLeft) { (item, days, isEstimated) ->
                        NotificationItemRow(
                            item = item,
                            days = days,
                            isEstimated = isEstimated,
                            textColor = ColorDarkChocolate
                        )
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
private fun NotificationItemRow(
    item: PantryItem,
    days: Long,
    isEstimated: Boolean,
    textColor: Color
) {
    val daysLabel = when {
        days < 0 -> "${-days} hari lewat"
        days == 0L -> "Hari ini"
        else -> "$days hari lagi"
    }
    val finalDaysText = if (isEstimated) "~$daysLabel" else daysLabel

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorDarkChocolate
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.quantityLabel,
                    fontSize = 12.sp,
                    color = ColorTextSubtitleBrown
                )
            }
            Surface(
                color = if (days <= 0) ColorUrgencyRed.copy(alpha = 0.15f) else ColorSoftCream,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = finalDaysText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
