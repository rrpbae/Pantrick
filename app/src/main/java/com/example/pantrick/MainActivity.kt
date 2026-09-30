package com.example.pantrick

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.pantrick.core.ui.theme.PantrickTheme
import com.example.pantrick.navigation.PantrickNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PantrickTheme {
                val navController = rememberNavController()
                PantrickNavHost(navController = navController)
            }
        }
    }
}