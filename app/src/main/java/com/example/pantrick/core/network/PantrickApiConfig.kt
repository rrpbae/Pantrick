// [Materi: Network Configuration] Konfigurasi URL backend Pantrick
package com.example.pantrick.core.network

object PantrickApiConfig {
    // Ganti ke IP LAN atau host production jika perlu
    // Untuk emulator Android, gunakan 10.0.2.2 (loopback host)
    // Untuk device fisik, gunakan IP LAN PC yang menjalankan backend
    const val BASE_URL = "http://10.0.2.2:8081"
}
