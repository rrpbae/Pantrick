// [Materi: Network Configuration] Konfigurasi URL backend Pantrick
package com.example.pantrick.core.network

object PantrickApiConfig {
    // Untuk physical device via USB debugging + ADB reverse (adb reverse tcp:8081 tcp:8081),
    // atau untuk Android emulator: gunakan 127.0.0.1:8081
    const val BASE_URL = "http://127.0.0.1:8081"
}
