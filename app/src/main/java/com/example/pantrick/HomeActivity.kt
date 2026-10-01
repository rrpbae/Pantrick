// [Materi: Activity & Compose Setup] Entry Point utama aplikasi dengan Jetpack Compose
package com.example.pantrick

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.navigation.PantrickNavHost

// [Materi: ComponentActivity] Activity tunggal (Single-Activity Architecture) sebagai host navigasi Compose
class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // [Materi: Edge-to-Edge UI] Menampilkan konten hingga tepi layar perangkat modern
        enableEdgeToEdge()
        setContent {
            // [Materi: Theme Wrapper] Menerapkan tema Material 3 ke seluruh hierarki composable
            PantrickTheme {
                PantrickNavHost()
            }
        }
    }
}
