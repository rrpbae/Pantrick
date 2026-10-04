// [Materi: Ubah Password Screen] Layar formulir perubahan kata sandi akun pengguna
package com.example.pantrick.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.core.ui.theme.*
import com.example.pantrick.core.util.ValidationResult
import com.example.pantrick.core.util.Validators
import com.example.pantrick.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isOldPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    var isSubmitted by remember { mutableStateOf(false) }
    var oldPasswordBackendError by remember { mutableStateOf<String?>(null) }

    // Validasi aturan password (sama dengan Register)
    val newPasswordValidation = remember(newPassword) {
        Validators.validatePassword(newPassword)
    }

    val isNewPasswordSameAsOld = oldPassword.isNotEmpty() && newPassword.isNotEmpty() && oldPassword == newPassword
    val isConfirmPasswordMatch = newPassword.isNotEmpty() && newPassword == confirmPassword

    val isFormValid = oldPassword.isNotBlank() &&
            newPasswordValidation is ValidationResult.Success &&
            !isNewPasswordSameAsOld &&
            isConfirmPasswordMatch

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ubah Password",
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = ColorSurfaceWhite),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // 1. Password Lama
                    Text(
                        text = "Password Lama",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorDarkChocolate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = {
                            oldPassword = it
                            oldPasswordBackendError = null
                        },
                        placeholder = { Text("Masukkan password lama", color = ColorPlaceholder, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { isOldPasswordVisible = !isOldPasswordVisible }) {
                                Icon(
                                    imageVector = if (isOldPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = ColorTextSubtitleBrown
                                )
                            }
                        },
                        visualTransformation = if (isOldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = (isSubmitted && oldPassword.isBlank()) || oldPasswordBackendError != null,
                        supportingText = {
                            if (oldPasswordBackendError != null) {
                                Text(text = oldPasswordBackendError ?: "", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                            } else if (isSubmitted && oldPassword.isBlank()) {
                                Text(text = "Password lama tidak boleh kosong", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
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

                    // 2. Password Baru
                    Text(
                        text = "Password Baru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorDarkChocolate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val newPasswordHasError = isSubmitted && (newPasswordValidation !is ValidationResult.Success || isNewPasswordSameAsOld)
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        placeholder = { Text("Minimal 6 karakter", color = ColorPlaceholder, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                                Icon(
                                    imageVector = if (isNewPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = ColorTextSubtitleBrown
                                )
                            }
                        },
                        visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = newPasswordHasError,
                        supportingText = {
                            if (isSubmitted) {
                                if (isNewPasswordSameAsOld) {
                                    Text(text = "Password baru tidak boleh sama dengan password lama", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                } else if (newPasswordValidation is ValidationResult.Error) {
                                    Text(text = newPasswordValidation.message, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                }
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

                    // 3. Konfirmasi Password Baru
                    Text(
                        text = "Konfirmasi Password Baru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorDarkChocolate
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val confirmHasError = isSubmitted && (!isConfirmPasswordMatch || confirmPassword.isBlank())
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = { Text("Ulangi password baru", color = ColorPlaceholder, fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = ColorTextSubtitleBrown, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = ColorTextSubtitleBrown
                                )
                            }
                        },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = confirmHasError,
                        supportingText = {
                            if (isSubmitted) {
                                if (confirmPassword.isBlank()) {
                                    Text(text = "Konfirmasi password tidak boleh kosong", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                } else if (!isConfirmPasswordMatch) {
                                    Text(text = "Konfirmasi password tidak cocok", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                                }
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
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tombol Simpan
            Button(
                onClick = {
                    isSubmitted = true
                    if (isFormValid) {
                        val error = authViewModel.changePassword(oldPassword, newPassword)
                        if (error != null) {
                            oldPasswordBackendError = error
                        } else {
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorForestGreen,
                    contentColor = ColorSurfaceWhite
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
