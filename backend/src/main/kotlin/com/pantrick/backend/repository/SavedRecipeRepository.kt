package com.pantrick.backend.repository

import com.pantrick.backend.models.CookingHistory
import com.pantrick.backend.models.RecipeCollection
import com.pantrick.backend.models.SavedRecipe
import com.pantrick.backend.models.ShoppingListItem
import java.time.Instant

/**
 * Repository interface untuk mengelola saved recipes dan recipe collections per user.
 */
interface SavedRecipeRepository {
    fun saveRecipe(userId: Int, recipeId: String): SavedRecipe
    fun unsaveRecipe(userId: Int, recipeId: String): Boolean
    fun isRecipeSaved(userId: Int, recipeId: String): Boolean
    fun getSavedRecipesByUserId(userId: Int): List<SavedRecipe>

    fun createCollection(userId: Int, name: String): RecipeCollection
    fun getCollectionsByUserId(userId: Int): List<RecipeCollection>
    fun getCollectionById(userId: Int, collectionId: String): RecipeCollection?
    fun updateCollection(userId: Int, collectionId: String, name: String): RecipeCollection?
    fun deleteCollection(userId: Int, collectionId: String): Boolean
    fun addRecipeToCollection(userId: Int, collectionId: String, recipeId: String): RecipeCollection?
    fun removeRecipeFromCollection(userId: Int, collectionId: String, recipeId: String): RecipeCollection?
}

/**
 * In-Memory Implementation untuk SavedRecipeRepository.
 */
class InMemorySavedRecipeRepository : SavedRecipeRepository {
    private val savedRecipes = mutableListOf<SavedRecipe>()
    private val collections = mutableListOf<RecipeCollection>()
    private var nextSavedId = 1
    private var nextCollectionId = 1

    override fun saveRecipe(userId: Int, recipeId: String): SavedRecipe {
        val existing = savedRecipes.find { it.userId == userId && it.recipeId == recipeId }
        if (existing != null) return existing

        val newSave = SavedRecipe(
            id = "saved-${nextSavedId++}",
            userId = userId,
            recipeId = recipeId,
            createdAt = Instant.now().toString()
        )
        savedRecipes.add(newSave)
        return newSave
    }

    override fun unsaveRecipe(userId: Int, recipeId: String): Boolean {
        return savedRecipes.removeIf { it.userId == userId && it.recipeId == recipeId }
    }

    override fun isRecipeSaved(userId: Int, recipeId: String): Boolean {
        return savedRecipes.any { it.userId == userId && it.recipeId == recipeId }
    }

    override fun getSavedRecipesByUserId(userId: Int): List<SavedRecipe> {
        return savedRecipes.filter { it.userId == userId }
    }

    override fun createCollection(userId: Int, name: String): RecipeCollection {
        val now = Instant.now().toString()
        val collection = RecipeCollection(
            id = "col-${nextCollectionId++}",
            userId = userId,
            name = name,
            recipeIds = emptyList(),
            createdAt = now,
            updatedAt = now
        )
        collections.add(collection)
        return collection
    }

    override fun getCollectionsByUserId(userId: Int): List<RecipeCollection> {
        return collections.filter { it.userId == userId }
    }

    override fun getCollectionById(userId: Int, collectionId: String): RecipeCollection? {
        return collections.find { it.userId == userId && it.id == collectionId }
    }

    override fun updateCollection(userId: Int, collectionId: String, name: String): RecipeCollection? {
        val index = collections.indexOfFirst { it.userId == userId && it.id == collectionId }
        if (index == -1) return null
        val existing = collections[index]
        val updated = existing.copy(name = name, updatedAt = Instant.now().toString())
        collections[index] = updated
        return updated
    }

    override fun deleteCollection(userId: Int, collectionId: String): Boolean {
        return collections.removeIf { it.userId == userId && it.id == collectionId }
    }

    override fun addRecipeToCollection(userId: Int, collectionId: String, recipeId: String): RecipeCollection? {
        val index = collections.indexOfFirst { it.userId == userId && it.id == collectionId }
        if (index == -1) return null
        val existing = collections[index]
        if (existing.recipeIds.contains(recipeId)) return existing
        val updated = existing.copy(
            recipeIds = existing.recipeIds + recipeId,
            updatedAt = Instant.now().toString()
        )
        collections[index] = updated
        return updated
    }

    override fun removeRecipeFromCollection(userId: Int, collectionId: String, recipeId: String): RecipeCollection? {
        val index = collections.indexOfFirst { it.userId == userId && it.id == collectionId }
        if (index == -1) return null
        val existing = collections[index]
        val updated = existing.copy(
            recipeIds = existing.recipeIds.filter { it != recipeId },
            updatedAt = Instant.now().toString()
        )
        collections[index] = updated
        return updated
    }
}

/**
 * Repository interface & implementation untuk Cooking History.
 */
interface CookingHistoryRepository {
    fun recordCooking(userId: Int, recipeId: String): CookingHistory
    fun getHistoryByUserId(userId: Int): List<CookingHistory>
}

class InMemoryCookingHistoryRepository : CookingHistoryRepository {
    private val history = mutableListOf<CookingHistory>()
    private var nextId = 1

    override fun recordCooking(userId: Int, recipeId: String): CookingHistory {
        val record = CookingHistory(
            id = "cook-${nextId++}",
            userId = userId,
            recipeId = recipeId,
            cookedAt = Instant.now().toString()
        )
        history.add(record)
        return record
    }

    override fun getHistoryByUserId(userId: Int): List<CookingHistory> {
        return history.filter { it.userId == userId }
    }
}

/**
 * Repository interface & implementation untuk Shopping List.
 */
interface ShoppingListRepository {
    fun addItem(userId: Int, ingredientName: String, quantity: String?, unit: String?, recipeId: String?): ShoppingListItem
    fun getItemsByUserId(userId: Int): List<ShoppingListItem>
}

class InMemoryShoppingListRepository : ShoppingListRepository {
    private val items = mutableListOf<ShoppingListItem>()
    private var nextId = 1

    override fun addItem(userId: Int, ingredientName: String, quantity: String?, unit: String?, recipeId: String?): ShoppingListItem {
        val item = ShoppingListItem(
            id = "shop-${nextId++}",
            userId = userId,
            ingredientName = ingredientName,
            quantity = quantity,
            unit = unit,
            recipeId = recipeId,
            createdAt = Instant.now().toString()
        )
        items.add(item)
        return item
    }

    override fun getItemsByUserId(userId: Int): List<ShoppingListItem> {
        return items.filter { it.userId == userId }
    }
}
