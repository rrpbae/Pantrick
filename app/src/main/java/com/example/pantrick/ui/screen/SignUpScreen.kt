// [Materi: Compose Screen & Architecture] Halaman Sign Up Pantrick
// Menerapkan Pola Stateful vs Stateless, buildAnnotatedString, dan Multi-field Validation Indonesia
package com.example.pantrick.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.data.repository.AuthResult
import com.example.pantrick.ui.component.AuthFooterText
import com.example.pantrick.ui.component.PantrickButton
import com.example.pantrick.ui.component.PantrickLogo
import com.example.pantrick.ui.component.PantrickTextField
import com.example.pantrick.ui.viewmodel.AuthViewModel
import com.example.pantrick.util.PantrickConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// [Materi: data class] Wadah data state form Sign Up
data class SignUpViewState(
    val fullName: String = "",
    val isNameError: Boolean = false,
    val nameErrorMessage: String = "",
    val email: String = "",
    val isEmailError: Boolean = false,
    val emailErrorMessage: String = "",
    val password: String = "",
    val isPasswordError: Boolean = false,
    val passwordErrorMessage: String = "",
    val isPasswordVisible: Boolean = false,
    val repeatPassword: String = "",
    val isRepeatPasswordError: Boolean = false,
    val repeatPasswordErrorMessage: String = "",
    val isRepeatPasswordVisible: Boolean = false,
    val isTermsAccepted: Boolean = false,
    val isFormValid: Boolean = false,
    val isLoading: Boolean = false
)

// [Materi: Stateful Composable] Mengelola state input individual dan integrasi registrasi ke ViewModel
@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onSignUpSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by rememberSaveable { mutableStateOf("") }
    var isNameTouched by rememberSaveable { mutableStateOf(false) }

    var email by rememberSaveable { mutableStateOf("") }
    var isEmailTouched by rememberSaveable { mutableStateOf(false) }

    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordTouched by rememberSaveable { mutableStateOf(false) }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    var repeatPassword by rememberSaveable { mutableStateOf("") }
    var isRepeatPasswordTouched by rememberSaveable { mutableStateOf(false) }
    var isRepeatPasswordVisible by rememberSaveable { mutableStateOf(false) }

    var isTermsAccepted by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // [Materi: Resource Strings untuk Validasi]
    val nameEmptyMsg = stringResource(R.string.val_name_empty)
    val emailEmptyMsg = stringResource(R.string.val_email_empty)
    val emailInvalidMsg = stringResource(R.string.val_email_invalid)
    val passwordEmptyMsg = stringResource(R.string.val_password_empty)
    val passwordShortMsg = stringResource(R.string.val_password_short)
    val repeatPasswordEmptyMsg = stringResource(R.string.val_repeat_password_empty)
    val passwordMismatchMsg = stringResource(R.string.val_password_mismatch)
    val registerSuccessMsg = stringResource(R.string.auth_success_register)

    // [Materi: Aturan Validasi Lokal]
    val isNameValid = fullName.isNotBlank()
    val isEmailValid = email.isNotBlank() && email.contains("@") && email.contains(".")
    val isPasswordValid = password.length >= PantrickConstants.MIN_PASSWORD_LENGTH
    val isRepeatPasswordValid = repeatPassword.isNotEmpty() && repeatPassword == password
    val isFormValid = isNameValid && isEmailValid && isPasswordValid && isRepeatPasswordValid && isTermsAccepted

    // [Materi: Error Messaging]
    val nameErrorMessage = if (isNameTouched && !isNameValid) nameEmptyMsg else ""
    val emailErrorMessage = if (isEmailTouched && !isEmailValid) {
        if (email.isBlank()) emailEmptyMsg else emailInvalidMsg
    } else ""
    val passwordErrorMessage = if (isPasswordTouched && !isPasswordValid) {
        if (password.isEmpty()) passwordEmptyMsg else passwordShortMsg
    } else ""
    val repeatPasswordErrorMessage = if (isRepeatPasswordTouched && !isRepeatPasswordValid) {
        if (repeatPassword.isEmpty()) repeatPasswordEmptyMsg else passwordMismatchMsg
    } else ""

    val viewState = SignUpViewState(
        fullName = fullName,
        isNameError = isNameTouched && !isNameValid,
        nameErrorMessage = nameErrorMessage,
        email = email,
        isEmailError = isEmailTouched && !isEmailValid,
        emailErrorMessage = emailErrorMessage,
        password = password,
        isPasswordError = isPasswordTouched && !isPasswordValid,
        passwordErrorMessage = passwordErrorMessage,
        isPasswordVisible = isPasswordVisible,
        repeatPassword = repeatPassword,
        isRepeatPasswordError = isRepeatPasswordTouched && !isRepeatPasswordValid,
        repeatPasswordErrorMessage = repeatPasswordErrorMessage,
        isRepeatPasswordVisible = isRepeatPasswordVisible,
        isTermsAccepted = isTermsAccepted,
        isFormValid = isFormValid,
        isLoading = isLoading
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        StatelessSignUpContent(
            viewState = viewState,
            onNameChange = {
                fullName = it
                isNameTouched = true
            },
            onEmailChange = {
                email = it
                isEmailTouched = true
            },
            onPasswordChange = {
                password = it
                isPasswordTouched = true
            },
            onPasswordVisibilityToggle = { isPasswordVisible = !isPasswordVisible },
            onRepeatPasswordChange = {
                repeatPassword = it
                isRepeatPasswordTouched = true
            },
            onRepeatPasswordVisibilityToggle = { isRepeatPasswordVisible = !isRepeatPasswordVisible },
            onTermsChange = { isTermsAccepted = it },
            onCreateAccountClick = {
                if (isFormValid && !isLoading) {
                    coroutineScope.launch {
                        isLoading = true
                        // Simulasi jeda server halus 1 detik
                        delay(PantrickConstants.ASYNC_DELAY_MS)
                        authViewModel.register(
                            fullName = fullName,
                            email = email,
                            password = password,
                            rememberMe = true // Otomatis aktifkan sesi saat mendaftar
                        ) { result ->
                            isLoading = false
                            when (result) {
                                is AuthResult.Success -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(registerSuccessMsg)
                                    }
                                    onSignUpSuccess()
                                }
                                is AuthResult.Error -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(result.message)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            onLoginClick = onNavigateToLogin,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

// [Materi: Stateless Composable] Hanya menerima data untuk ditampilkan dan memicu lambda saat interaksi
@Composable
fun StatelessSignUpContent(
    viewState: SignUpViewState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onRepeatPasswordChange: (String) -> Unit,
    onRepeatPasswordVisibilityToggle: () -> Unit,
    onTermsChange: (Boolean) -> Unit,
    onCreateAccountClick: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = PantrickConstants.SCREEN_PADDING)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // 1. Logo Pantrick
        PantrickLogo(size = PantrickConstants.LOGO_SIZE)

        Spacer(modifier = Modifier.height(18.dp))

        // 2. Judul "Buat akun kamu"
        Text(
            text = stringResource(R.string.signup_title),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Subjudul
        Text(
            text = stringResource(R.string.signup_subtitle),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Field "Nama lengkap"
        PantrickTextField(
            label = stringResource(R.string.name_label),
            value = viewState.fullName,
            onValueChange = onNameChange,
            placeholder = stringResource(R.string.name_placeholder),
            leadingIcon = Icons.Default.Person,
            isError = viewState.isNameError,
            errorMessage = viewState.nameErrorMessage
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 5. Field "Alamat email"
        PantrickTextField(
            label = stringResource(R.string.email_label),
            value = viewState.email,
            onValueChange = onEmailChange,
            placeholder = stringResource(R.string.email_placeholder),
            leadingIcon = Icons.Default.Email,
            isError = viewState.isEmailError,
            errorMessage = viewState.emailErrorMessage,
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Field "Kata sandi"
        PantrickTextField(
            label = stringResource(R.string.password_label),
            value = viewState.password,
            onValueChange = onPasswordChange,
            placeholder = stringResource(R.string.password_placeholder),
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            isPasswordVisible = viewState.isPasswordVisible,
            onPasswordVisibilityToggle = onPasswordVisibilityToggle,
            isError = viewState.isPasswordError,
            errorMessage = viewState.passwordErrorMessage,
            keyboardType = KeyboardType.Password
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 7. Field "Ulangi kata sandi"
        PantrickTextField(
            label = stringResource(R.string.repeat_password_label),
            value = viewState.repeatPassword,
            onValueChange = onRepeatPasswordChange,
            placeholder = stringResource(R.string.repeat_password_placeholder),
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            isPasswordVisible = viewState.isRepeatPasswordVisible,
            onPasswordVisibilityToggle = onRepeatPasswordVisibilityToggle,
            isError = viewState.isRepeatPasswordError,
            errorMessage = viewState.repeatPasswordErrorMessage,
            keyboardType = KeyboardType.Password
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 8. Baris Checkbox + buildAnnotatedString Syarat & Ketentuan Bahasa Indonesia
        val termsColor = MaterialTheme.colorScheme.primary
        val textColor = MaterialTheme.colorScheme.onSurfaceVariant
        val prefix = stringResource(R.string.terms_prefix)
        val tos = stringResource(R.string.terms_service)
        val andText = stringResource(R.string.terms_and)
        val privacy = stringResource(R.string.terms_privacy)

        val annotatedTermsText = remember(termsColor, textColor, prefix, tos, andText, privacy) {
            buildAnnotatedString {
                append(prefix)
                withStyle(style = SpanStyle(color = termsColor, fontWeight = FontWeight.Bold)) {
                    append(tos)
                }
                append(andText)
                withStyle(style = SpanStyle(color = termsColor, fontWeight = FontWeight.Bold)) {
                    append(privacy)
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = viewState.isTermsAccepted,
                onCheckedChange = onTermsChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
            Text(
                text = annotatedTermsText,
                fontSize = 12.sp,
                color = textColor,
                lineHeight = 16.sp,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .clickable { onTermsChange(!viewState.isTermsAccepted) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 9. Tombol Lebar Penuh "Buat akun"
        PantrickButton(
            text = stringResource(R.string.signup_button),
            onClick = onCreateAccountClick,
            enabled = viewState.isFormValid,
            isLoading = viewState.isLoading
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 10. Teks Bawah Navigasi ke Masuk
        AuthFooterText(
            questionText = stringResource(R.string.already_have_account),
            actionText = stringResource(R.string.action_login),
            onActionClick = onLoginClick
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

// [Materi: Preview Light]
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpScreenPreview() {
    PantrickTheme {
        StatelessSignUpContent(
            viewState = SignUpViewState(
                fullName = "Budi Santoso",
                email = "budi@contoh.com",
                isFormValid = true
            ),
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordVisibilityToggle = {},
            onRepeatPasswordChange = {},
            onRepeatPasswordVisibilityToggle = {},
            onTermsChange = {},
            onCreateAccountClick = {},
            onLoginClick = {}
        )
    }
}
