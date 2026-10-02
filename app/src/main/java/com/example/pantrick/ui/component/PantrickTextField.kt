// [Materi: Reusable Composable & Clean Code] Komponen Text Field kustom Pantrick
// Menyatukan Label atas, Box Ikon Hijau Muda, Outline bulat 12dp, Toggle Password, dan Pesan Error
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.util.PantrickConstants

// [Materi: Reusable Composable] Kotak ikon hijau muda lembut dengan sudut membulat
@Composable
fun LeadingIconBox(
    icon: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    // [Materi: Modifier clip & background] Membuat kontainer kotak rounded berlatar hijau muda
    Box(
        modifier = modifier
            .size(PantrickConstants.ICON_BOX_SIZE)
            .clip(RoundedCornerShape(PantrickConstants.ICON_BOX_CORNER_RADIUS))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

// [Materi: Reusable & Modular UI Component] Field input yang dapat dikonfigurasi untuk teks biasa maupun password
@Composable
fun PantrickTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,               // [Materi: Higher-Order Function / Callback Lambda]
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // [Materi: Typography & Semantic Color] Label teks di atas field input
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(6.dp))

        // [Materi: OutlinedTextField & Material 3 Styling]
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(PantrickConstants.CORNER_RADIUS),
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            },
            // [Materi: Leading Icon Container] Kotak hijau muda lembut untuk ikon input
            leadingIcon = {
                Box(modifier = Modifier.padding(start = 6.dp, end = 4.dp)) {
                    LeadingIconBox(icon = leadingIcon, contentDescription = label)
                }
            },
            // [Materi: Conditional Trailing Icon] Toggle tampil/sembunyikan password
            trailingIcon = if (isPassword && onPasswordVisibilityToggle != null) {
                {
                    IconButton(onClick = onPasswordVisibilityToggle) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else null,
            // [Materi: VisualTransformation] Mengaburkan teks input menjadi bullet jika tipe password
            visualTransformation = if (isPassword && !isPasswordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            singleLine = true,
            isError = isError,
            // [Materi: Supporting Text] Menampilkan pesan error validasi di bawah field input
            supportingText = if (isError && errorMessage.isNotEmpty()) {
                {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                errorTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

// [Materi: Multi-Preview] Pratinjau komponen TextField dalam mode Terang
@Preview(showBackground = true)
@Composable
fun PantrickTextFieldPreview() {
    PantrickTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PantrickTextField(
                label = "Email address",
                value = "student@pantrick.com",
                onValueChange = {},
                placeholder = "Enter your email",
                leadingIcon = Icons.Default.Email
            )
            Spacer(modifier = Modifier.height(16.dp))
            PantrickTextField(
                label = "Password",
                value = "123456",
                onValueChange = {},
                placeholder = "Enter your password",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                isPasswordVisible = false,
                onPasswordVisibilityToggle = {}
            )
        }
    }
}

// [Materi: Multi-Preview] Pratinjau komponen TextField dalam mode Gelap
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PantrickTextFieldDarkPreview() {
    PantrickTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            PantrickTextField(
                label = "Email address",
                value = "student@pantrick.com",
                onValueChange = {},
                placeholder = "Enter your email",
                leadingIcon = Icons.Default.Email
            )
        }
    }
}
