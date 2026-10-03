// [Materi: Type-Safe Navigation] Definisi rute navigasi berbasis tipe data Kotlin (Serializable)
package com.example.pantrick.navigation

import kotlinx.serialization.Serializable

// [Materi: @Serializable Object Route] Rute Alur Autentikasi
@Serializable
data object LoginRoute

@Serializable
data object SignUpRoute

// [Materi: @Serializable Object Route] Rute 5 Tab Utama Pantrick
@Serializable
data object HomeRoute

@Serializable
data object PantryRoute

@Serializable
data class AddRoute(val initialLocation: String = "KULKAS")

// 👇 TAMBAHKAN INI UNTUK RUTE FORM TAMBAH BAHAN DI PANTRY 👇
@Serializable
data class AddPantryItemRoute(val initialLocation: String = "KULKAS")

@Serializable
data class EditIngredientRoute(val itemId: String)

@Serializable
data object RecipesRoute

@Serializable
data class RecipeDetailRoute(val recipeId: String)

@Serializable
data object ProfileRoute

// Kompatibilitas dengan Screen interface sebelumnya (jika ada kode lama)
sealed interface Screen {
    @Serializable
    data object Login : Screen

    @Serializable
    data object SignUp : Screen

    @Serializable
    data object ForgotPassword : Screen

    @Serializable
    data object Home : Screen
}