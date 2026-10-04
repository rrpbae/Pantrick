// [Materi: Kotlin Data Class & Catalog] Model representasi resep dan katalog referensi resep Indonesia
package com.example.pantrick.data.model

import androidx.annotation.DrawableRes
import com.example.pantrick.R
import kotlinx.serialization.Serializable

// [Materi: Model Bahan Resep] Representasi detail bahan resep beserta takaran dan kata kunci pencocokan
@Serializable
data class RecipeIngredient(
    val name: String,
    val amount: String,
    val keyword: String
)

// [Materi: Model Resep Terhitung] Representasi resep setelah dicocokkan dengan bahan pengguna
@Serializable
data class Recipe(
    val id: String,
    val title: String,
    val description: String = "",
    val durationMinutes: Int = 0,
    val servings: Int = 0,
    val matchPercent: Int = 0,
    val usesLabel: String = "",
    val readyCount: Int = 0,
    val totalCount: Int = 0,
    val missingIngredient: String = "",
    @get:DrawableRes val imageRes: Int = R.drawable.ic_placeholder_pasta,
    val savedAtEpochMillis: Long = 0L,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<String> = emptyList()
)

// [Materi: Template Resep Statis] Definisi master resep beserta daftar kata kunci bahan yang dibutuhkan
data class RecipeTemplate(
    val id: String,
    val title: String,
    val description: String,
    val durationMinutes: Int = 0,
    val servings: Int = 0,
    val ingredientKeywords: List<String>,
    @get:DrawableRes val imageRes: Int,
    val ingredients: List<RecipeIngredient> = emptyList(),
    val steps: List<String> = emptyList()
)

// [Materi: Kotlin Object Singleton] Katalog referensi 3 resep Indonesia dan kalkulator kecocokan bahan
object RecipeCatalog {
    const val MIN_RECIPE_MATCH_PERCENT = 30

    // [Materi: Static Reference Data] Tiga resep Nusantara praktis
    val recipes: List<RecipeTemplate> = listOf(
        RecipeTemplate(
            id = "recipe_pasta_garlic",
            title = "Pasta Krim Bawang Putih",
            description = "Pasta saus krim lembut kaya rasa dengan aroma bawang putih gurih yang menggugah selera.",
            durationMinutes = 20,
            servings = 2,
            ingredientKeywords = listOf("pasta", "bawang", "susu", "keju"),
            imageRes = R.drawable.ic_placeholder_pasta,
            // TEMPORARY: ganti dengan data dari dataset
            ingredients = listOf(
                RecipeIngredient(name = "Pasta Penne", amount = "200 g", keyword = "pasta"),
                RecipeIngredient(name = "Bawang Putih", amount = "3 siung, cincang", keyword = "bawang"),
                RecipeIngredient(name = "Susu Cair / Krim", amount = "150 ml", keyword = "susu"),
                RecipeIngredient(name = "Keju Parut", amount = "50 g", keyword = "keju")
            ),
            // TEMPORARY: ganti dengan data dari dataset
            steps = listOf(
                "Rebus pasta dalam air mendidih yang diberi sedikit garam hingga al dente, lalu tiriskan.",
                "Panaskan mentega di wajan, tumis bawang putih cincang hingga harum dan kekuningan.",
                "Tuangkan susu cair, aduk perlahan dengan api kecil hingga mulai mendidih.",
                "Masukkan keju parut, aduk hingga larut dan saus mengental gurih.",
                "Masukkan pasta rebus, bumbui dengan garam dan merica, aduk rata lalu sajikan hangat."
            )
        ),
        RecipeTemplate(
            id = "recipe_nasgor_telur",
            title = "Nasi Goreng Telur",
            description = "Nasi goreng harum dan lezat dengan telur orak-arik dan bumbu dapur sederhana khas rumahan.",
            durationMinutes = 15,
            servings = 2,
            ingredientKeywords = listOf("nasi", "telur", "bawang", "kecap"),
            imageRes = R.drawable.ic_placeholder_pasta,
            // TEMPORARY: ganti dengan data dari dataset
            ingredients = listOf(
                RecipeIngredient(name = "Nasi Putih", amount = "2 piring (dingin)", keyword = "nasi"),
                RecipeIngredient(name = "Telur Ayam", amount = "2 butir, kocok lepas", keyword = "telur"),
                RecipeIngredient(name = "Bawang Merah & Putih", amount = "4 siung, iris tipis", keyword = "bawang"),
                RecipeIngredient(name = "Kecap Manis", amount = "2 sdm", keyword = "kecap")
            ),
            // TEMPORARY: ganti dengan data dari dataset
            steps = listOf(
                "Panaskan minyak di wajan, buat telur orak-arik hingga matang, lalu sisihkan.",
                "Tumis irisan bawang merah dan bawang putih hingga harum dan matang.",
                "Masukkan nasi putih dingin, aduk cepat dengan api sedang agar nasi tidak menggumpal.",
                "Tuangkan kecap manis dan sedikit garam, aduk rata hingga warna kecap merata.",
                "Masukkan kembali telur orak-arik, aduk sebentar hingga bumbu meresap, angkat dan sajikan."
            )
        ),
        RecipeTemplate(
            id = "recipe_ayam_tumis",
            title = "Ayam Tumis Bawang",
            description = "Potongan ayam lembut ditumis bersama irisan bawang melimpah dengan saus gurih manis.",
            durationMinutes = 25,
            servings = 3,
            ingredientKeywords = listOf("ayam", "bawang", "saus", "cabai"),
            imageRes = R.drawable.ic_placeholder_pasta,
            // TEMPORARY: ganti dengan data dari dataset
            ingredients = listOf(
                RecipeIngredient(name = "Daging Ayam Fillet", amount = "300 g, potong dadu", keyword = "ayam"),
                RecipeIngredient(name = "Bawang Bombai", amount = "1 buah, iris tebal", keyword = "bawang"),
                RecipeIngredient(name = "Cabai Merah", amount = "3 buah, iris serong", keyword = "cabai"),
                RecipeIngredient(name = "Saus Tiram", amount = "2 sdm", keyword = "saus")
            ),
            // TEMPORARY: ganti dengan data dari dataset
            steps = listOf(
                "Cuci bersih potongan ayam, tiriskan dan lumuri sedikit garam.",
                "Panaskan minyak di wajan, tumis ayam hingga berubah warna dan setengah matang.",
                "Masukkan irisan bawang bombai dan cabai merah, tumis bersama ayam hingga layu dan harum.",
                "Tambahkan saus tiram dan kecap manis, aduk rata hingga seluruh ayam terbalut saus.",
                "Masak dengan api sedang hingga ayam matang empuk dan saus mengental, lalu angkat."
            )
        )
    )

    // [Materi: Algoritma Pencocokan Bahan] Menghitung persentase kecocokan bahan pantry pengguna dengan template resep
    fun findBestMatch(userItems: List<PantryItem>, minMatchPercent: Int = MIN_RECIPE_MATCH_PERCENT): Recipe? {
        if (userItems.isEmpty()) return null

        val matchedRecipes = recipes.map { template ->
            val readyKeywords = template.ingredientKeywords.filter { keyword ->
                userItems.any { item -> item.name.contains(keyword, ignoreCase = true) }
            }
            val missingKeywords = template.ingredientKeywords.filterNot { keyword ->
                userItems.any { item -> item.name.contains(keyword, ignoreCase = true) }
            }
            val matchPercent = if (template.ingredientKeywords.isNotEmpty()) {
                (readyKeywords.size * 100) / template.ingredientKeywords.size
            } else 0

            val missingText = if (missingKeywords.isNotEmpty()) {
                missingKeywords.first().replaceFirstChar { it.uppercase() }
            } else {
                "Lengkap"
            }

            Recipe(
                id = template.id,
                title = template.title,
                description = template.description,
                durationMinutes = template.durationMinutes,
                servings = template.servings,
                matchPercent = matchPercent,
                usesLabel = "Cocok dengan bahanmu",
                readyCount = readyKeywords.size,
                totalCount = template.ingredientKeywords.size,
                missingIngredient = missingText,
                imageRes = template.imageRes,
                ingredients = template.ingredients,
                steps = template.steps
            )
        }

        // Ambil resep dengan skor kecocokan tertinggi yang memenuhi ambang batas minimum
        val best = matchedRecipes.maxByOrNull { it.matchPercent }
        return if (best != null && best.matchPercent >= minMatchPercent) best else null
    }
}
