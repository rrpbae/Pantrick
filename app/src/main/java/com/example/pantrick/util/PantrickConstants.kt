// [Materi: Kotlin Object] Menggunakan 'object' untuk membuat singleton kumpulan konstanta aplikasi
// Menghindari magic numbers & magic strings di composable
package com.example.pantrick.util

import androidx.compose.ui.unit.dp

// [Materi: Constants & Clean Code] Kumpulan konstanta terpusat untuk konfigurasi UI, validasi, dan batas kedaluwarsa
object PantrickConstants {
    // [Materi: Aturan Validasi] Aturan minimal panjang password
    const val MIN_PASSWORD_LENGTH = 6

    // [Materi: Coroutine Delay] Durasi simulasi panggilan server asinkron (dalam milidetik)
    const val ASYNC_DELAY_MS = 1000L

    // [Materi: Dimens & Layout] Ukuran konsisten sesuai pedoman desain
    val LOGO_SIZE = 76.dp
    val LOGO_SIZE_SMALL = 28.dp
    val BUTTON_HEIGHT = 52.dp
    val CORNER_RADIUS = 12.dp
    val CARD_CORNER_RADIUS = 20.dp
    val SCREEN_PADDING = 24.dp
    val HOME_HORIZONTAL_PADDING = 20.dp
    val ICON_BOX_SIZE = 40.dp
    val ICON_BOX_CORNER_RADIUS = 8.dp
    val EXPIRING_CARD_WIDTH = 170.dp

    // [Materi: Business Rule Constants] Ambang batas hari hampir kedaluwarsa (<= 2 hari)
    const val EXPIRING_THRESHOLD_DAYS = 2L
}
