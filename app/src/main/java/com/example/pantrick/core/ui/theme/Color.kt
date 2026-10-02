// [Materi: Color & Design System] Definisi palet warna resmi aplikasi Pantrick
// Menggunakan nilai heksadesimal Color(0xFF...) untuk konsistensi warna brand
package com.example.pantrick.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// [Materi: Official Color Palette] Palet warna resmi Pantrick sesuai pedoman desain
val ColorForestGreen = Color(0xFF1E4B27)        // Brand utama, badge, ikon aksen, match tag, tab navigasi aktif, link
val ColorSoftCream = Color(0xFFF7F0E8)          // Background aplikasi
val ColorWarmPeach = Color(0xFFF7D3C1)          // Kartu "Expiring Soon", pill tab aktif, tombol sekunder, penanda perhatian
val ColorDarkChocolate = Color(0xFF4A2C21)      // Judul, label kontras tinggi, tombol aksi utama (CTA)
val ColorSurfaceWhite = Color(0xFFFFFFFF)       // Permukaan kartu (putih bersih)
val ColorUrgencyRed = Color(0xFFB3261E)         // Titik notifikasi, badge ACTION, badge "1 day left", teks expiry mendesak

// Token turunan untuk elemen input dan pendukung
val ColorTextSubtitleBrown = Color(0xFF7A685F)  // Abu kecokelatan untuk subjudul & teks pendukung
val ColorIconBoxBg = Color(0xFFE8F2EA)          // Hijau muda lembut sebagai kotak ikon
val ColorInputBorder = Color(0xFFDED8CE)        // Border abu lembut untuk field input
val ColorInputFill = Color(0xFFFFFFFF)          // Isi text field (putih bersih)
val ColorTextBody = Color(0xFF332F2B)           // Teks isi umum
val ColorPlaceholder = Color(0xFF9E978F)         // Placeholder input text field

// [Materi: Dark Theme Colors] Warna adaptif untuk dark theme agar kontras dan tetap nyaman dibaca
val ColorDarkBackground = Color(0xFF171513)
val ColorDarkSurface = Color(0xFF221F1C)
val ColorDarkPrimaryGreen = Color(0xFF76C486)   // Hijau lebih terang untuk keterbacaan di dark background
val ColorDarkWarmPeach = Color(0xFF8C5D4B)
val ColorDarkButtonBrown = Color(0xFF5D3827)
val ColorDarkTextTitle = Color(0xFFF7EFEA)
val ColorDarkTextSubtitle = Color(0xFFC7BDB5)
val ColorDarkIconBoxBg = Color(0xFF233626)
val ColorDarkInputBorder = Color(0xFF4A443E)
val ColorDarkError = Color(0xFFFFB4AB)

// Kompatibilitas dengan nama variabel sebelumnya
val ColorPrimaryGreen = ColorForestGreen
val ColorButtonBrown = ColorDarkChocolate
val ColorBackgroundWarmCream = ColorSoftCream
val ColorTextTitleDark = ColorDarkChocolate
val ColorError = ColorUrgencyRed
val ColorSurface = ColorSurfaceWhite
val ColorPrimaryDark = ColorForestGreen
val ColorPrimaryGradientEnd = Color(0xFF2C6437)
val ColorLeafGreen = ColorForestGreen
val ColorBackground = ColorSoftCream
val ColorTextSecondary = ColorTextSubtitleBrown
val ColorAvatarButtonBg = ColorWarmPeach
val ColorCheckboxBorder = Color(0xFF8F9B8B)
val ColorDividerLine = Color(0xFFE0DDD5)
val ColorDecorativeLeaf = Color(0x141E4B27)
val ColorLogoApple = Color(0xFFD62828)
val ColorLogoJar = Color(0xFFF5A623)
val ColorLogoJarLid = Color(0xFFFFD180)
val ColorLogoPanel = Color(0xFFFFFFFF)
val ColorSocialBorder = Color(0xFFEEEEEE)

// [Materi: data class] Wadah warna untuk CompositionLocal PantrickColors
@Immutable
data class PantrickColors(
    val forestGreen: Color = ColorForestGreen,
    val softCream: Color = ColorSoftCream,
    val warmPeach: Color = ColorWarmPeach,
    val darkChocolate: Color = ColorDarkChocolate,
    val urgencyRed: Color = ColorUrgencyRed,
    val primaryDark: Color = ColorForestGreen,
    val primaryGradientEnd: Color = ColorPrimaryGradientEnd,
    val leafGreen: Color = ColorForestGreen,
    val buttonBrown: Color = ColorDarkChocolate,
    val background: Color = ColorSoftCream,
    val surface: Color = ColorSurfaceWhite,
    val inputFill: Color = ColorInputFill,
    val inputBorder: Color = ColorInputBorder,
    val textTitle: Color = ColorDarkChocolate,
    val textSubtitle: Color = ColorTextSubtitleBrown,
    val textSecondary: Color = ColorTextSubtitleBrown,
    val textBody: Color = ColorTextBody,
    val placeholder: Color = ColorPlaceholder,
    val iconBoxBg: Color = ColorIconBoxBg,
    val avatarButtonBg: Color = ColorWarmPeach,
    val checkboxBorder: Color = ColorCheckboxBorder,
    val dividerLine: Color = ColorDividerLine,
    val decorativeLeaf: Color = ColorDecorativeLeaf,
    val logoApple: Color = ColorLogoApple,
    val logoJar: Color = ColorLogoJar,
    val logoJarLid: Color = ColorLogoJarLid,
    val logoPanel: Color = ColorLogoPanel,
    val error: Color = ColorUrgencyRed,
    val socialBorder: Color = ColorSocialBorder
)

val LocalPantrickColors = staticCompositionLocalOf { PantrickColors() }
