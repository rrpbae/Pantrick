// [Materi: Compose Screen & Architecture] Halaman Login Pantrick
// Menerapkan pemisahan Stateful vs Stateless, Unidirectional Data Flow, dan Validasi Lokal Indonesia
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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

// [Materi: data class] Pengelompokan data state untuk dikirim ke composable Stateless (UDF)
data class LoginViewState(
    val email: String = "",
    val isEmailError: Boolean = false,
    val emailErrorMessage: String = "",
    val password: String = "",
    val isPasswordError: Boolean = false,
    val passwordErrorMessage: String = "",
    val isPasswordVisible: Boolean = false,
    val rememberMe: Boolean = false,
    val isFormValid: Boolean = false,
    val isLoading: Boolean = false
)

// [Materi: Stateful Composable] Composable yang memegang state, validasi, integrasi ViewModel, dan feedback Snackbar
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    // [Materi: rememberSaveable & Delegation] State form dipertahankan saat terjadi konfigurasi ulang
    var email by rememberSaveable { mutableStateOf("") }
    var isEmailTouched by rememberSaveable { mutableStateOf(false) }

    var password by rememberSaveable { mutableStateOf("") }
    var isPasswordTouched by rememberSaveable { mutableStateOf(false) }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }

    var rememberMe by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // [Materi: Resource Strings untuk Validasi]
    val emailEmptyMsg = stringResource(R.string.val_email_empty)
    val emailInvalidMsg = stringResource(R.string.val_email_invalid)
    val passwordEmptyMsg = stringResource(R.string.val_password_empty)
    val passwordShortMsg = stringResource(R.string.val_password_short)
    val comingSoonMsg = stringResource(R.string.feature_coming_soon)
    val loginSuccessMsg = stringResource(R.string.auth_success_login)

    // [Materi: Aturan Validasi Lokal]
    val isEmailValid = email.isNotBlank() && email.contains("@") && email.contains(".")
    val isPasswordValid = password.length >= PantrickConstants.MIN_PASSWORD_LENGTH
    val isFormValid = isEmailValid && isPasswordValid

    // [Materi: Error Messaging] Pesan error aktif hanya setelah field disentuh
    val emailErrorMessage = if (isEmailTouched && !isEmailValid) {
        if (email.isBlank()) emailEmptyMsg else emailInvalidMsg
    } else ""

    val passwordErrorMessage = if (isPasswordTouched && !isPasswordValid) {
        if (password.isEmpty()) passwordEmptyMsg else passwordShortMsg
    } else ""

    val viewState = LoginViewState(
        email = email,
        isEmailError = isEmailTouched && !isEmailValid,
        emailErrorMessage = emailErrorMessage,
        password = password,
        isPasswordError = isPasswordTouched && !isPasswordValid,
        passwordErrorMessage = passwordErrorMessage,
        isPasswordVisible = isPasswordVisible,
        rememberMe = rememberMe,
        isFormValid = isFormValid,
        isLoading = isLoading
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        StatelessLoginContent(
            viewState = viewState,
            onEmailChange = {
                email = it
                isEmailTouched = true
            },
            onPasswordChange = {
                password = it
                isPasswordTouched = true
            },
            onPasswordVisibilityToggle = { isPasswordVisible = !isPasswordVisible },
            onRememberMeChange = { rememberMe = it },
            onLoginClick = {
                if (isFormValid && !isLoading) {
                    coroutineScope.launch {
                        isLoading = true
                        // Simulasi jeda server halus 1 detik
                        delay(PantrickConstants.ASYNC_DELAY_MS)
                        authViewModel.login(
                            email = email,
                            password = password,
                            rememberMe = rememberMe
                        ) { result ->
                            isLoading = false
                            when (result) {
                                is AuthResult.Success -> {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(loginSuccessMsg)
                                    }
                                    onLoginSuccess()
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
            onForgotPasswordClick = {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(comingSoonMsg)
                }
            },
            onSignUpClick = onNavigateToSignUp,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

// [Materi: Stateless Composable] Hanya fokus merender antarmuka berbahasa Indonesia
@Composable
fun StatelessLoginContent(
    viewState: LoginViewState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
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
        Spacer(modifier = Modifier.height(24.dp))

        // 1. Logo Pantrick
        PantrickLogo(size = PantrickConstants.LOGO_SIZE)

        Spacer(modifier = Modifier.height(20.dp))

        // 2. Judul "Selamat datang kembali!"
        Text(
            text = stringResource(R.string.login_title),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Subjudul
        Text(
            text = stringResource(R.string.login_subtitle),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 4. Field Alamat Email
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

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Field Kata Sandi
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

        Spacer(modifier = Modifier.height(8.dp))

        // 6. Baris "Ingat saya" & "Lupa kata sandi?"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = viewState.rememberMe,
                    onCheckedChange = onRememberMeChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )
                Text(
                    text = stringResource(R.string.remember_me),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = stringResource(R.string.forgot_password),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onForgotPasswordClick() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 7. Tombol Utama "Masuk"
        PantrickButton(
            text = stringResource(R.string.login_button),
            onClick = onLoginClick,
            enabled = viewState.isFormValid,
            isLoading = viewState.isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 8. Teks Footer Navigasi ke Daftar
        AuthFooterText(
            questionText = stringResource(R.string.dont_have_account),
            actionText = stringResource(R.string.action_sign_up),
            onActionClick = onSignUpClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// [Materi: Preview Light]
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    PantrickTheme {
        StatelessLoginContent(
            viewState = LoginViewState(
                email = "budi@contoh.com",
                password = "password123",
                isFormValid = true
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordVisibilityToggle = {},
            onRememberMeChange = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onSignUpClick = {}
        )
    }
}

// [Materi: Preview Dark]
@Preview(showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun LoginScreenDarkPreview() {
    PantrickTheme(darkTheme = true) {
        StatelessLoginContent(
            viewState = LoginViewState(
                email = "budi@contoh.com",
                password = "password123",
                isFormValid = true
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onPasswordVisibilityToggle = {},
            onRememberMeChange = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onSignUpClick = {}
        )
    }
}
