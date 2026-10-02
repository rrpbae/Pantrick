package com.pantrick.backend.service

import com.pantrick.backend.models.CookingHistory
import com.pantrick.backend.models.Recipe
import com.pantrick.backend.models.RecipeCollection
import com.pantrick.backend.models.ShoppingListItem
import com.pantrick.backend.repository.CookingHistoryRepository
import com.pantrick.backend.repository.PantryRepository
import com.pantrick.backend.repository.RecipeRepository
import com.pantrick.backend.repository.SavedRecipeRepository
import com.pantrick.backend.repository.ShoppingListRepository

/**
 * Service untuk menangani Saved Recipes, Recipe Collections, Cooking History, dan Shopping List.
 */
class SavedRecipeService(
    private val savedRecipeRepository: SavedRecipeRepository,
    private val recipeRepository: RecipeRepository,
    private val cookingHistoryRepository: CookingHistoryRepository,
    private val shoppingListRepository: ShoppingListRepository,
    private val pantryRepository: PantryRepository? = null
) {

    // --- SAVED RECIPES ---
    fun saveRecipe(userId: Int, recipeId: String): Boolean {
        val recipe = recipeRepository.getRecipeById(recipeId) ?: return false
        savedRecipeRepository.saveRecipe(userId, recipe.id)
        return true
    }

    fun unsaveRecipe(userId: Int, recipeId: String): Boolean {
        return savedRecipeRepository.unsaveRecipe(userId, recipeId)
    }

    fun isRecipeSaved(userId: Int, recipeId: String): Boolean {
        return savedRecipeRepository.isRecipeSaved(userId, recipeId)
    }

    fun getSavedRecipes(userId: Int): List<Recipe> {
        val savedList = savedRecipeRepository.getSavedRecipesByUserId(userId)
        return savedList.mapNotNull { saved ->
            recipeRepository.getRecipeById(saved.recipeId)
        }
    }

    // --- COLLECTIONS ---
    fun createCollection(userId: Int, name: String): RecipeCollection {
        return savedRecipeRepository.createCollection(userId, name)
    }

    fun getCollections(userId: Int): List<RecipeCollection> {
        var userCols = savedRecipeRepository.getCollectionsByUserId(userId)
        if (userCols.isEmpty()) {
            // Buat default collection "Resep Tersimpan"
            val defaultCol = savedRecipeRepository.createCollection(userId, "Resep Tersimpan")
            userCols = listOf(defaultCol)
        }
        return userCols
    }

    fun getCollectionById(userId: Int, collectionId: String): RecipeCollection? {
        return savedRecipeRepository.getCollectionById(userId, collectionId)
    }

    fun updateCollection(userId: Int, collectionId: String, name: String): RecipeCollection? {
        return savedRecipeRepository.updateCollection(userId, collectionId, name)
    }

    fun deleteCollection(userId: Int, collectionId: String): Boolean {
        return savedRecipeRepository.deleteCollection(userId, collectionId)
    }

    fun addRecipeToCollection(userId: Int, collectionId: String, recipeId: String): RecipeCollection? {
        val recipe = recipeRepository.getRecipeById(recipeId) ?: return null
        return savedRecipeRepository.addRecipeToCollection(userId, collectionId, recipe.id)
    }

    fun removeRecipeFromCollection(userId: Int, collectionId: String, recipeId: String): RecipeCollection? {
        return savedRecipeRepository.removeRecipeFromCollection(userId, collectionId, recipeId)
    }

    fun getRecipesInCollection(userId: Int, collectionId: String): List<Recipe>? {
        val collection = savedRecipeRepository.getCollectionById(userId, collectionId) ?: return null
        return collection.recipeIds.mapNotNull { recipeRepository.getRecipeById(it) }
    }

    // --- COOKING HISTORY ---
    fun recordCooking(userId: Int, recipeId: String): CookingHistory? {
        val recipe = recipeRepository.getRecipeById(recipeId) ?: return null
        return cookingHistoryRepository.recordCooking(userId, recipe.id)
    }

    fun getCookingHistory(userId: Int): List<CookingHistory> {
        return cookingHistoryRepository.getHistoryByUserId(userId)
    }

    // --- SHOPPING LIST ---
    fun addShoppingItem(userId: Int, ingredientName: String, quantity: String?, unit: String?, recipeId: String?): ShoppingListItem {
        return shoppingListRepository.addItem(userId, ingredientName, quantity, unit, recipeId)
    }

    fun getShoppingList(userId: Int): List<ShoppingListItem> {
        return shoppingListRepository.getItemsByUserId(userId)
    }

    /**
     * Menambahkan HANYA bahan yang KURANG dari suatu resep ke keranjang belanja user.
     */
    fun addMissingIngredientsToShoppingList(userId: Int, recipeId: String): List<ShoppingListItem>? {
        val recipe = recipeRepository.getRecipeById(recipeId) ?: return null
        val userPantry = pantryRepository?.getItemsByUserId(userId) ?: emptyList()

        val pantryNormNames: Set<String> = userPantry
            .map { it.normalizedName.ifBlank { IngredientParser.normalizeIngredientName(it.name) } }
            .filter { it.isNotBlank() }
            .toSet()

        val addedItems = mutableListOf<ShoppingListItem>()

        for (ing in recipe.ingredients) {
            val ingNorm = ing.normalizedName
            val isMatched = pantryNormNames.any { pantryName ->
                ingNorm.contains(pantryName) || pantryName.contains(ingNorm)
            }

            // HANYA tambahkan jika TIDAK ada di pantry user (bahan kurang)
            if (!isMatched) {
                val item = shoppingListRepository.addItem(
                    userId = userId,
                    ingredientName = ing.displayName,
                    quantity = ing.quantity,
                    unit = ing.unit,
                    recipeId = recipe.id
                )
                addedItems.add(item)
            }
        }

        return addedItems
    }
}
