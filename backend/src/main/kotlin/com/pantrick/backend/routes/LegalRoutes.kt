package com.pantrick.backend.routes

import com.pantrick.backend.models.LegalDocument
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.legalRoutes() {
    route("/api/legal") {
        get("/terms") {
            val termsDoc = LegalDocument(
                type = "terms_of_service",
                title = "Syarat dan Ketentuan Layanan Pantrick",
                version = "1.0",
                updatedAt = "2026-10-03",
                content = """
                    Selamat datang di Pantrick. Dengan menggunakan aplikasi ini, Anda menyetujui syarat dan ketentuan berikut:
                    1. Penggunaan Layanan: Pantrick menyediakan rekomendasi resep masakan berbasis bahan makanan di dapur Anda.
                    2. Akun Pengguna: Anda bertanggung jawab untuk menjaga kerahasiaan kata sandi dan informasi akun Anda.
                    3. Batasan Tanggung Jawab: Resep dan rekomendasi disajikan sebagai panduan. Harap perhatikan tanggal kedaluwarsa dan alergi makanan Anda.
                    4. Perubahan Ketentuan: Kami berhak memperbarui syarat dan ketentuan ini sewaktu-waktu.
                """.trimIndent()
            )
            call.respond(HttpStatusCode.OK, termsDoc)
        }

        get("/privacy") {
            val privacyDoc = LegalDocument(
                type = "privacy_policy",
                title = "Kebijakan Privasi Pantrick",
                version = "1.0",
                updatedAt = "2026-10-03",
                content = """
                    Kebijakan Privasi Pantrick menjelaskan bagaimana kami mengumpulkan, menggunakan, dan melindungi informasi Anda:
                    1. Pengumpulan Data: Kami mengumpulkan data akun (nama, email) dan daftar bahan dapur (pantry) yang Anda masukkan.
                    2. Penggunaan Data: Data digunakan semata-mata untuk memberikan layanan rekomendasi resep dan pengelolaan dapur secara personal.
                    3. Keamanan Data: Kami menggunakan standar enkripsi modern (termasuk BCrypt untuk kata sandi) untuk melindungi data Anda.
                    4. Hak Pengguna: Anda berhak mengelola data Anda kapan saja melalui fitur akun dalam aplikasi.
                """.trimIndent()
            )
            call.respond(HttpStatusCode.OK, privacyDoc)
        }
    }
}
