// [Materi: Compose Theme & Material 3] Konfigurasi Tema Sentral Pantrick
// Menerapkan Material Design 3 dengan dukungan Light dan Dark mode secara dinamis
package com.example.pantrick.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// [Materi: Material Design 3 LightColorScheme] Skema warna terang resmi aplikasi Pantrick
private val LightColorScheme = lightColorScheme(
    primary = ColorForestGreen,             // #1E4B27 brand utama, badge, ikon aksen, match tag, tab aktif, link
    onPrimary = ColorSurfaceWhite,
    primaryContainer = ColorIconBoxBg,      // #E8F2EA kotak hijau muda
    onPrimaryContainer = ColorForestGreen,
    secondary = ColorWarmPeach,             // #F7D3C1 kartu expiring soon, pill tab aktif, tombol sekunder
    onSecondary = ColorDarkChocolate,
    secondaryContainer = ColorWarmPeach,
    onSecondaryContainer = ColorDarkChocolate,
    tertiary = ColorDarkChocolate,          // #4A2C21 judul, tombol aksi utama CTA
    onTertiary = ColorSurfaceWhite,
    background = ColorSoftCream,            // #F7F0E8 background aplikasi
    onBackground = ColorDarkChocolate,      // #4A2C21 teks judul / label kontras tinggi
    surface = ColorSurfaceWhite,            // #FFFFFF permukaan kartu
    onSurface = ColorDarkChocolate,
    surfaceVariant = ColorIconBoxBg,
    onSurfaceVariant = ColorTextSubtitleBrown,
    error = ColorUrgencyRed,                // #B3261E titik notifikasi, badge ACTION, badge 1 day left, teks expiry mendesak
    onError = ColorSurfaceWhite,
    outline = ColorInputBorder
)

// [Materi: Material Design 3 DarkColorScheme] Skema warna gelap aplikasi Pantrick (keterbacaan dan kontras tetap terjaga)
private val DarkColorScheme = darkColorScheme(
    primary = ColorDarkPrimaryGreen,
    onPrimary = ColorDarkBackground,
    primaryContainer = ColorDarkIconBoxBg,
    onPrimaryContainer = ColorDarkPrimaryGreen,
    secondary = ColorDarkWarmPeach,
    onSecondary = ColorDarkTextTitle,
    secondaryContainer = ColorDarkWarmPeach,
    onSecondaryContainer = ColorDarkTextTitle,
    tertiary = ColorDarkChocolate,
    onTertiary = ColorDarkBackground,
    background = ColorDarkBackground,
    onBackground = ColorDarkTextTitle,
    surface = ColorDarkSurface,
    onSurface = ColorDarkTextTitle,
    surfaceVariant = ColorDarkIconBoxBg,
    onSurfaceVariant = ColorDarkTextSubtitle,
    error = ColorDarkError,
    onError = ColorDarkBackground,
    outline = ColorDarkInputBorder
)

// [Materi: Kotlin Object] Singleton untuk mengakses token tema Pantrick secara terstruktur
object PantrickTheme {
    val colors: PantrickColors
        @Composable
        @ReadOnlyComposable
        get() = LocalPantrickColors.current

    val typography: PantrickTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalPantrickTypography.current

    val dimens: Dimens
        get() = Dimens

    val spacing: Spacing
        get() = Spacing
}

// [Materi: Higher-Order Function & Trailing Lambda] Composable wrapper yang menyediakan skema warna & tipografi ke sub-tree composable
@Composable
fun PantrickTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colors: PantrickColors? = null,
    typography: PantrickTypography = PantrickTypography(),
    content: @Composable () -> Unit
) {
    val activeColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val activeColors = colors ?: if (darkTheme) {
        PantrickColors(
            forestGreen = ColorDarkPrimaryGreen,
            softCream = ColorDarkBackground,
            warmPeach = ColorDarkWarmPeach,
            darkChocolate = ColorDarkChocolate,
            urgencyRed = ColorDarkError,
            primaryDark = ColorDarkPrimaryGreen,
            buttonBrown = ColorDarkButtonBrown,
            background = ColorDarkBackground,
            surface = ColorDarkSurface,
            textTitle = ColorDarkTextTitle,
            textSubtitle = ColorDarkTextSubtitle,
            iconBoxBg = ColorDarkIconBoxBg,
            inputBorder = ColorDarkInputBorder,
            error = ColorDarkError
        )
    } else {
        PantrickColors()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                // Set status bar & nav bar icons agar kontras dengan latar
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalPantrickColors provides activeColors,
        LocalPantrickTypography provides typography
    ) {
        MaterialTheme(
            colorScheme = activeColorScheme,
            typography = MaterialTypography,
            content = content
        )
    }
}
