package com.pantrick.backend.repository

import com.pantrick.backend.models.PantryItem
import com.pantrick.backend.models.StorageType
import com.pantrick.backend.service.IngredientParser
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

interface PantryRepository {
    fun getItemsByUserId(userId: Int): List<PantryItem>
    fun getItemsByUserIdAndCategory(userId: Int, category: String): List<PantryItem>
    fun addItem(item: PantryItem): PantryItem
    fun updateItem(item: PantryItem): PantryItem?
    fun deleteItem(id: String, userId: Int): Boolean
    fun getItemById(id: String): PantryItem?
}

class InMemoryPantryRepository : PantryRepository {
    private val pantryItems = ConcurrentHashMap<String, PantryItem>()
    private val idCounter = AtomicLong(10)

    init {
        seedDemoData()
    }

    private fun seedDemoData() {
        val sampleItems = listOf(
            PantryItem(
                id = "pantry-1",
                userId = 1,
                name = "Fresh Whole Milk",
                ingredientName = "Fresh Whole Milk",
                normalizedName = IngredientParser.normalizeIngredientName("Fresh Whole Milk"),
                quantity = 1.0,
                unit = "Gallon",
                category = "Dairy & Eggs",
                storageType = StorageType.FRIDGE,
                expirationDate = "2026-10-03", // 2 days left relative to 2026-10-01
                imageUrl = "/api/pantry/items/pantry-1/image"
            ),
            PantryItem(
                id = "pantry-2",
                userId = 1,
                name = "Unsalted Butter",
                ingredientName = "Unsalted Butter",
                normalizedName = IngredientParser.normalizeIngredientName("Unsalted Butter"),
                quantity = 200.0,
                unit = "grams",
                category = "Dairy & Eggs",
                storageType = StorageType.FRIDGE,
                expirationDate = "2026-10-15",
                imageUrl = "/api/pantry/items/pantry-2/image"
            ),
            PantryItem(
                id = "pantry-3",
                userId = 1,
                name = "Fresh Garlic",
                ingredientName = "Fresh Garlic",
                normalizedName = IngredientParser.normalizeIngredientName("Fresh Garlic"),
                quantity = 1.0,
                unit = "head",
                category = "Produce",
                storageType = StorageType.PANTRY,
                expirationDate = "2026-10-01", // Expires today
                imageUrl = "/api/pantry/items/pantry-3/image"
            ),
            PantryItem(
                id = "pantry-4",
                userId = 1,
                name = "Penne Pasta",
                ingredientName = "Penne Pasta",
                normalizedName = IngredientParser.normalizeIngredientName("Penne Pasta"),
                quantity = 500.0,
                unit = "grams",
                category = "Pantry",
                storageType = StorageType.PANTRY,
                expirationDate = "2026-12-31",
                imageUrl = "/api/pantry/items/pantry-4/image"
            ),
            PantryItem(
                id = "pantry-5",
                userId = 1,
                name = "Chicken Breast",
                ingredientName = "Chicken Breast",
                normalizedName = IngredientParser.normalizeIngredientName("Chicken Breast"),
                quantity = 500.0,
                unit = "grams",
                category = "Meat",
                storageType = StorageType.FREEZER,
                expirationDate = "2026-10-02", // 1 day left
                imageUrl = "/api/pantry/items/pantry-5/image"
            )
        )
        sampleItems.forEach { pantryItems[it.id] = it }
    }

    override fun getItemsByUserId(userId: Int): List<PantryItem> {
        return pantryItems.values.filter { it.userId == userId }
    }

    override fun getItemsByUserIdAndCategory(userId: Int, category: String): List<PantryItem> {
        if (category.equals("All", ignoreCase = true) || category.isBlank()) {
            return getItemsByUserId(userId)
        }
        return pantryItems.values.filter {
            it.userId == userId && it.category.equals(category.trim(), ignoreCase = true)
        }
    }

    override fun addItem(item: PantryItem): PantryItem {
        val id = if (item.id.isNotBlank()) item.id else "pantry-${idCounter.getAndIncrement()}"
        val imageUrl = if (item.imageUrl.isNullOrBlank()) "/api/pantry/items/$id/image" else item.imageUrl
        val newItem = item.copy(id = id, imageUrl = imageUrl)
        pantryItems[id] = newItem
        return newItem
    }

    override fun updateItem(item: PantryItem): PantryItem? {
        val existing = pantryItems[item.id] ?: return null
        if (existing.userId != item.userId) return null
        val imageUrl = if (item.imageUrl.isNullOrBlank()) "/api/pantry/items/${item.id}/image" else item.imageUrl
        val updated = item.copy(imageUrl = imageUrl)
        pantryItems[item.id] = updated
        return updated
    }

    override fun deleteItem(id: String, userId: Int): Boolean {
        val existing = pantryItems[id] ?: return false
        if (existing.userId != userId) return false
        return pantryItems.remove(id) != null
    }

    override fun getItemById(id: String): PantryItem? {
        return pantryItems[id]
    }
}
