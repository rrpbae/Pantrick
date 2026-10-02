// [Materi: Reusable Composable] Komponen Footer Navigasi Auth
// Menghubungkan alur Login ↔ Sign Up dengan teks dan aksi clickable
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pantrick.core.ui.theme.PantrickTheme

// [Materi: Higher-Order Function] Menerima event onActionClick untuk navigasi ke layar terkait
@Composable
fun AuthFooterText(
    questionText: String,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // [Materi: Row & Alignment] Menjajarkan teks pertanyaan dan aksi di tengah bawah
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$questionText ",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // [Materi: Modifier clickable] Membuat teks aksi dapat disentuh untuk berpindah halaman
        Text(
            text = actionText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

// [Materi: Multi-Preview] Pratinjau AuthFooterText mode Terang
@Preview(showBackground = true)
@Composable
fun AuthFooterTextPreview() {
    PantrickTheme {
        AuthFooterText(
            questionText = "Don't have an account?",
            actionText = "Sign up",
            onActionClick = {}
        )
    }
}

// [Materi: Multi-Preview] Pratinjau AuthFooterText mode Gelap
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun AuthFooterTextDarkPreview() {
    PantrickTheme(darkTheme = true) {
        AuthFooterText(
            questionText = "Already have an account?",
            actionText = "Login",
            onActionClick = {}
        )
    }
}
