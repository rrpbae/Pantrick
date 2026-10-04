// [Materi: Edit Profil Screen] Layar formulir edit profil pengguna dengan dukungan foto profil
package com.example.pantrick.ui.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.data.model.User
import com.example.pantrick.ui.component.UserAvatar
import com.example.pantrick.ui.component.loadBitmapFromFile
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    currentUser: User?,
    initialPhotoPath: String? = null,
    onSave: (newName: String, newPhotoPath: String?) -> Unit,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var nameInput by remember(currentUser) { mutableStateOf(currentUser?.fullName.orEmpty()) }
    var isNameTouched by remember { mutableStateOf(false) }

    // Path foto yang sedang aktif diedit (awal: initialPhotoPath)
    var photoPath by remember(initialPhotoPath) { mutableStateOf(initialPhotoPath) }

    val trimmedName = nameInput.trim()
    val isNameValid = trimmedName.isNotBlank() && nameInput.length <= 50
    val isError = isNameTouched && !isNameValid

    val displayInitial = trimmedName.take(1).uppercase().ifEmpty {
        currentUser?.fullName?.trim()?.take(1)?.uppercase() ?: "?"
    }

    // Photo picker launcher tanpa izin permission baru
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = saveImageToInternalStorage(context, uri, currentUser?.email)
            if (savedPath != null) {
                photoPath = savedPath
            }
        }
    }

    // Decode bitmap secara aman di remember
    val avatarBitmap = remember(photoPath) {
        loadBitmapFromFile(photoPath)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Profil",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorDarkChocolate
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Avatar Besar: foto kalau ada, inisial kalau belum
            UserAvatar(
                userName = trimmedName,
                photoPath = photoPath,
                size = 96.dp,
                fontSize = 40.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tombol Ubah Foto
            OutlinedButton(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = ColorForestGreen
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PhotoCamera,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Ubah Foto",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Card Form Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Field Nama
                    Text(
                        text = "Nama",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorDarkChocolate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            if (it.length <= 50) {
                                nameInput = it
                                isNameTouched = true
                            }
                        },
                        placeholder = {
                            Text(
                                text = "Masukkan nama lengkap",
                                color = ColorPlaceholder,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = ColorTextSubtitleBrown,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        isError = isError,
                        supportingText = {
                            if (isError) {
                                Text(
                                    text = if (trimmedName.isBlank()) "Nama tidak boleh kosong" else "Maksimal 50 karakter",
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            } else {
                                Text(
                                    text = "${nameInput.length}/50 karakter",
                                    color = ColorTextSubtitleBrown,
                                    fontSize = 11.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ColorSoftCream,
                            unfocusedContainerColor = ColorSoftCream,
                            focusedBorderColor = ColorForestGreen,
                            unfocusedBorderColor = Color.Transparent,
                            errorBorderColor = MaterialTheme.colorScheme.error,
                            focusedTextColor = ColorDarkChocolate,
                            unfocusedTextColor = ColorDarkChocolate
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Field Email (Read-only / Disabled)
                    Text(
                        text = "Email",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorDarkChocolate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = currentUser?.email.orEmpty(),
                        onValueChange = { /* Disabled */ },
                        enabled = false,
                        readOnly = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Email,
                                contentDescription = null,
                                tint = ColorTextSubtitleBrown.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = ColorSoftCream.copy(alpha = 0.6f),
                            disabledBorderColor = Color.Transparent,
                            disabledTextColor = ColorTextSubtitleBrown,
                            disabledLeadingIconColor = ColorTextSubtitleBrown.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tombol Simpan
            Button(
                onClick = {
                    if (isNameValid) {
                        onSave(trimmedName, photoPath)
                        onNavigateBack()
                    }
                },
                enabled = isNameValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorForestGreen,
                    contentColor = ColorSurfaceWhite,
                    disabledContainerColor = ColorForestGreen.copy(alpha = 0.4f),
                    disabledContentColor = ColorSurfaceWhite.copy(alpha = 0.7f)
                )
            ) {
                Text(
                    text = "Simpan",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Fungsi bantu menyimpan gambar galeri ke filesDir internal dengan resize (max 512px)
private fun saveImageToInternalStorage(context: Context, uri: Uri, email: String?): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        if (originalBitmap == null) return null

        val maxDimension = 512
        val width = originalBitmap.width
        val height = originalBitmap.height
        val scaledBitmap = if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            val newWidth: Int
            val newHeight: Int
            if (ratio > 1f) {
                newWidth = maxDimension
                newHeight = (maxDimension / ratio).toInt().coerceAtLeast(1)
            } else {
                newHeight = maxDimension
                newWidth = (maxDimension * ratio).toInt().coerceAtLeast(1)
            }
            Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        } else {
            originalBitmap
        }

        val safeEmail = (email ?: "user").replace(Regex("[^a-zA-Z0-9]"), "_")
        val timestamp = System.currentTimeMillis()
        val destinationFile = File(context.filesDir, "profile_${safeEmail}_$timestamp.jpg")

        // Hapus file foto lama pengguna agar tidak menumpuk di penyimpanan
        context.filesDir.listFiles { file ->
            file.name.startsWith("profile_${safeEmail}_") && file.name != destinationFile.name
        }?.forEach { it.delete() }

        FileOutputStream(destinationFile).use { out ->
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        destinationFile.absolutePath
    } catch (e: Exception) {
        null
    }
}
