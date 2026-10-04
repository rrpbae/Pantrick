// [Materi: Ktor HTTP Client] Singleton Ktor client untuk seluruh request ke backend Pantrick
package com.example.pantrick.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object PantrickHttpClient {
    val client: HttpClient by lazy {
        HttpClient(Android) {
            engine {
                connectTimeout = 15_000
                socketTimeout = 30_000
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
            }
            install(Logging) {
                level = LogLevel.HEADERS
            }
        }
    }
}
