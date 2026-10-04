// [Materi: Shared UI Component] Komponen avatar profil terpadu untuk semua layar
package com.example.pantrick.ui.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.core.ui.theme.ColorWarmPeach
import com.example.pantrick.ui.viewmodel.AuthViewModel
import java.io.File

// Fungsi dekode bitmap dari path lokal yang aman
fun loadBitmapFromFile(path: String?): ImageBitmap? {
    if (path.isNullOrBlank()) return null
    return try {
        val file = File(path)
        if (!file.exists()) return null
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        bitmap?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}

// Komponen avatar utama berbasis state nama dan path foto
@Composable
fun UserAvatar(
    userName: String,
    photoPath: String? = null,
    size: Dp = 36.dp,
    fontSize: TextUnit = (size.value * 0.44f).sp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val initial = userName.trim().take(1).uppercase().ifEmpty { "?" }
    val avatarBitmap = remember(photoPath) {
        loadBitmapFromFile(photoPath)
    }

    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(ColorWarmPeach)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        if (avatarBitmap != null) {
            Image(
                bitmap = avatarBitmap,
                contentDescription = "Foto Profil",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = initial,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

// Komponen avatar otomatis yang membaca langsung dari AuthViewModel
@Composable
fun UserAvatar(
    authViewModel: AuthViewModel,
    size: Dp = 36.dp,
    fontSize: TextUnit = (size.value * 0.44f).sp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val photoPath by authViewModel.profileImagePath.collectAsState()

    UserAvatar(
        userName = currentUser?.fullName.orEmpty(),
        photoPath = photoPath,
        size = size,
        fontSize = fontSize,
        onClick = onClick,
        modifier = modifier
    )
}
