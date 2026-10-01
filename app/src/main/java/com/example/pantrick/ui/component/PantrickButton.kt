// [Materi: Reusable Composable] Komponen Tombol Utama Pantrick dengan Material 3 Button
// Menyediakan state loading asinkron, elevasi/shadow, dan warna tema konsisten
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.util.PantrickConstants

// [Materi: Higher-Order Function & Default Parameter] Komponen tombol dengan callback onClick dan status loading/enabled
@Composable
fun PantrickButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    // [Materi: Button & Elevation] Tombol Material 3 dengan warna cokelat tua dan elevasi halus
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(PantrickConstants.BUTTON_HEIGHT),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(PantrickConstants.CORNER_RADIUS),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp,
            disabledElevation = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary,
            disabledContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f),
            disabledContentColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.7f)
        )
    ) {
        // [Materi: Conditional UI Rendering] Menampilkan CircularProgressIndicator saat proses asinkron aktif
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = MaterialTheme.colorScheme.onTertiary,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiary
            )
        }
    }
}

// [Materi: Multi-Preview] Pratinjau komponen tombol dalam mode Terang
@Preview(showBackground = true)
@Composable
fun PantrickButtonPreview() {
    PantrickTheme {
        PantrickButton(
            text = "Log in",
            onClick = {}
        )
    }
}

// [Materi: Multi-Preview] Pratinjau komponen tombol dalam mode Gelap
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PantrickButtonDarkPreview() {
    PantrickTheme(darkTheme = true) {
        PantrickButton(
            text = "Create account",
            onClick = {}
        )
    }
}
