// [Materi: Reusable Composable] Komponen Logo Pantrick yang dapat digunakan ulang di semua layar
package com.example.pantrick.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.example.pantrick.R
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.util.PantrickConstants

// [Materi: Default Parameter] Ukuran logo memiliki nilai default LOGO_SIZE (76.dp) agar konsisten dan fleksibel diubah
@Composable
fun PantrickLogo(
    modifier: Modifier = Modifier,
    size: Dp = PantrickConstants.LOGO_SIZE
) {
    // [Materi: Box & Alignment] Box untuk memposisikan gambar logo di tengah secara horizontal
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // [Materi: Image & Resource] Memanggil aset drawable menggunakan painterResource
        Image(
            painter = painterResource(id = R.drawable.logo_pantrick),
            contentDescription = "Logo Pantrick",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(size)
        )
    }
}

// [Materi: Preview Composable] Pratinjau komponen Logo dalam mode Terang
@Preview(showBackground = true)
@Composable
fun PantrickLogoPreview() {
    PantrickTheme {
        PantrickLogo()
    }
}

// [Materi: Dark Theme Preview] Pratinjau komponen Logo dalam mode Gelap
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PantrickLogoDarkPreview() {
    PantrickTheme(darkTheme = true) {
        PantrickLogo()
    }
}
