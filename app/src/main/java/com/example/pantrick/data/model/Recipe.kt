// [Materi: Kotlin Data Class & Catalog] Model representasi resep dan katalog referensi resep Indonesia
package com.example.pantrick.data.model

import androidx.annotation.DrawableRes
import com.example.pantrick.R

// [Materi: Model Resep Terhitung] Representasi resep setelah dicocokkan dengan bahan pengguna
data class Recipe(
    val id: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val servings: Int,
    val matchPercent: Int,
    val usesLabel: String,
    val readyCount: Int,
    val totalCount: Int,
    val missingIngredient: String,
    @get:DrawableRes val imageRes: Int
)

// [Materi: Template Resep Statis] Definisi master resep beserta daftar kata kunci bahan yang dibutuhkan
data class RecipeTemplate(
    val id: String,
    val title: String,
    val description: String,
    val durationMinutes: Int,
    val servings: Int,
    val ingredientKeywords: List<String>,
    @get:DrawableRes val imageRes: Int
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
            imageRes = R.drawable.ic_placeholder_pasta
        ),
        RecipeTemplate(
            id = "recipe_nasgor_telur",
            title = "Nasi Goreng Telur",
            description = "Nasi goreng harum dan lezat dengan telur orak-arik dan bumbu dapur sederhana khas rumahan.",
            durationMinutes = 15,
            servings = 2,
            ingredientKeywords = listOf("nasi", "telur", "bawang", "kecap"),
            imageRes = R.drawable.ic_placeholder_pasta
        ),
        RecipeTemplate(
            id = "recipe_ayam_tumis",
            title = "Ayam Tumis Bawang",
            description = "Potongan ayam lembut ditumis bersama irisan bawang melimpah dengan saus gurih manis.",
            durationMinutes = 25,
            servings = 3,
            ingredientKeywords = listOf("ayam", "bawang", "saus", "cabai"),
            imageRes = R.drawable.ic_placeholder_pasta
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
                imageRes = template.imageRes
            )
        }

        // Ambil resep dengan skor kecocokan tertinggi yang memenuhi ambang batas minimum
        val best = matchedRecipes.maxByOrNull { it.matchPercent }
        return if (best != null && best.matchPercent >= minMatchPercent) best else null
    }
}
