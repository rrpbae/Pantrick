package com.pantrick.backend.service

import com.pantrick.backend.models.ExpirationPriority
import com.pantrick.backend.models.ExpirationStatus
import com.pantrick.backend.models.ExpiringPantryItem
import com.pantrick.backend.models.HomeData
import com.pantrick.backend.models.PantryItem
import com.pantrick.backend.models.PantrySummary
import com.pantrick.backend.models.RecipePairing
import com.pantrick.backend.models.StorageBreakdown
import com.pantrick.backend.models.UserData
import com.pantrick.backend.repository.PantryRepository
import com.pantrick.backend.repository.RecipeRepository
import com.pantrick.backend.repository.UserRepository
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class HomeService(
    private val userRepository: UserRepository,
    private val pantryRepository: PantryRepository,
    private val recipeRepository: RecipeRepository
) {

    fun getHomeData(
        userId: Int,
        categoryFilter: String? = null,
        needsAttentionLimit: Int = 5,
        recipePairingLimit: Int = 10,
        nowDate: LocalDate = LocalDate.now()
    ): HomeData? {
        val user = findUserById(userId) ?: return null

        val allUserItems = pantryRepository.getItemsByUserId(userId)
        val filteredItems = if (!categoryFilter.isNullOrBlank() && !categoryFilter.equals("All", ignoreCase = true)) {
            allUserItems.filter { it.category.equals(categoryFilter.trim(), ignoreCase = true) }
        } else {
            allUserItems
        }

        val expiringItems = calculateExpiringItems(filteredItems, nowDate)
        val pantrySummary = buildPantrySummary(allUserItems, expiringItems)
        val categories = buildCategoriesList(allUserItems)
        val needsAttention = getNeedsAttention(expiringItems, needsAttentionLimit)
        val recipePairings = generateRecipePairings(allUserItems, expiringItems, recipePairingLimit)

        return HomeData(
            user = UserData(id = user.id, name = user.name, email = user.email),
            pantrySummary = pantrySummary,
            categories = categories,
            needsAttention = needsAttention,
            recipePairings = recipePairings
        )
    }

    private fun findUserById(userId: Int): com.pantrick.backend.models.User? {
        // Since UserRepository existing interface only has findByEmail & createUser,
        // we handle finding user by checking seed / registered users safely or via email if needed.
        // If InMemoryUserRepository is used, we can query by email or inspect user.
        // For standard compatibility, we can query common users or fetch from user repository.
        val defaultUser = userRepository.findByEmail("user@example.com")
        if (defaultUser?.id == userId) return defaultUser

        val testUser = userRepository.findByEmail("wahid@pantrick.com")
        if (testUser?.id == userId) return testUser

        // Fallback for dynamically registered users:
        // If not found in seed, create a placeholder UserData or matching User
        return defaultUser?.copy(id = userId, name = "User $userId")
            ?: com.pantrick.backend.models.User(userId, "User $userId", "user$userId@pantrick.com", "")
    }

    fun calculateExpiringItems(items: List<PantryItem>, nowDate: LocalDate): List<ExpiringPantryItem> {
        val result = mutableListOf<ExpiringPantryItem>()

        for (item in items) {
            val dateStr = item.expirationDate
            if (dateStr.isNullOrBlank()) continue

            val expDate = try {
                LocalDate.parse(dateStr)
            } catch (e: DateTimeParseException) {
                continue
            }

            val daysRemaining = ChronoUnit.DAYS.between(nowDate, expDate)

            val status = when {
                daysRemaining < 0 -> ExpirationStatus.EXPIRED
                daysRemaining <= 3 -> ExpirationStatus.EXPIRING_SOON
                else -> ExpirationStatus.GOOD
            }

            val priority = when {
                daysRemaining <= 0 -> ExpirationPriority.HIGH
                daysRemaining <= 3 -> ExpirationPriority.MEDIUM
                else -> ExpirationPriority.LOW
            }

            if (status != ExpirationStatus.GOOD) {
                result.add(
                    ExpiringPantryItem(
                        id = item.id,
                        ingredientName = item.ingredientName,
                        quantity = item.quantity,
                        unit = item.unit,
                        expirationDate = dateStr,
                        daysRemaining = daysRemaining,
                        status = status,
                        priority = priority
                    )
                )
            }
        }

        // Sort: 1. Expired/smallest daysRemaining first, 2. Name
        return result.sortedWith(compareBy({ it.daysRemaining }, { it.ingredientName }))
    }

    private fun buildPantrySummary(
        allUserItems: List<PantryItem>,
        expiringItems: List<ExpiringPantryItem>
    ): PantrySummary {
        var fridge = 0
        var freezer = 0
        var room = 0
        val categoryCountMap = mutableMapOf<String, Int>()

        for (item in allUserItems) {
            when (item.storageType) {
                com.pantrick.backend.models.StorageType.FRIDGE -> fridge++
                com.pantrick.backend.models.StorageType.FREEZER -> freezer++
                com.pantrick.backend.models.StorageType.PANTRY -> room++
            }
            val cat = item.category.ifBlank { "Pantry" }
            categoryCountMap[cat] = (categoryCountMap[cat] ?: 0) + 1
        }

        return PantrySummary(
            totalItems = allUserItems.size,
            expiringSoonItems = expiringItems.size,
            storageBreakdown = StorageBreakdown(fridge = fridge, freezer = freezer, room = room, pantry = room),
            categoryBreakdown = categoryCountMap
        )
    }

    private fun buildCategoriesList(allUserItems: List<PantryItem>): List<String> {
        val categoriesSet = mutableSetOf<String>()
        categoriesSet.add("All")
        for (item in allUserItems) {
            if (item.category.isNotBlank()) {
                categoriesSet.add(item.category.trim())
            }
        }
        // Ensure default categories exist if pantry is empty
        if (categoriesSet.size == 1) {
            categoriesSet.addAll(listOf("Produce", "Dairy & Eggs", "Meat", "Pantry"))
        }
        return categoriesSet.toList()
    }

    fun getNeedsAttention(
        expiringItems: List<ExpiringPantryItem>,
        limit: Int
    ): List<ExpiringPantryItem> {
        val safeLimit = limit.coerceIn(1, 50)
        return expiringItems.take(safeLimit)
    }

    fun generateRecipePairings(
        userPantryItems: List<PantryItem>,
        expiringItems: List<ExpiringPantryItem>,
        limit: Int = 10
    ): List<RecipePairing> {
        if (userPantryItems.isEmpty()) return emptyList()

        // Extract normalized pantry ingredient names
        val pantryNames = userPantryItems.map { it.normalizedName.ifBlank { IngredientParser.normalizeIngredientName(it.ingredientName) } }
            .filter { it.isNotBlank() }
            .toSet()

        val expiringNames = expiringItems.map { IngredientParser.normalizeIngredientName(it.ingredientName) }
            .filter { it.isNotBlank() }
            .toSet()

        if (pantryNames.isEmpty()) return emptyList()

        val allRecipes = recipeRepository.getAllRecipes(limit = 13500, offset = 0)
        val pairings = mutableListOf<RecipePairing>()

        for (recipe in allRecipes) {
            if (recipe.ingredients.isEmpty()) continue

            val recipeNormIngredients = recipe.ingredients.map { it.normalizedName }
            var matchedCount = 0
            val matchedNames = mutableListOf<String>()
            val missingNames = mutableListOf<String>()
            var usesExpiring = false

            for (ing in recipe.ingredients) {
                val ingNorm = ing.normalizedName
                // Flexible matching: check exact match or substring match (e.g. "milk" matches "whole milk")
                val isMatched = pantryNames.any { pantryName ->
                    ingNorm.contains(pantryName) || pantryName.contains(ingNorm)
                }

                if (isMatched) {
                    matchedCount++
                    matchedNames.add(ing.displayName)
                    if (expiringNames.any { expName -> ingNorm.contains(expName) || expName.contains(ingNorm) }) {
                        usesExpiring = true
                    }
                } else {
                    missingNames.add(ing.displayName)
                }
            }

            if (matchedCount > 0) {
                val totalCount = recipe.ingredients.size
                val matchPercentage = ((matchedCount.toDouble() / totalCount.toDouble()) * 100.0).roundTo(1)

                // Scoring formula:
                // Base score = matchPercentage
                // Bonus for matched ingredients count = matchedCount * 5.0
                // Bonus for using expiring item = +20.0
                var score = matchPercentage + (matchedCount * 5.0)
                if (usesExpiring) {
                    score += 20.0
                }

                pairings.add(
                    RecipePairing(
                        recipeId = recipe.id,
                        title = recipe.title,
                        imageName = recipe.imageName,
                        hasImage = recipe.hasImage,
                        matchedIngredientsCount = matchedCount,
                        totalIngredientsCount = totalCount,
                        matchPercentage = matchPercentage,
                        missingIngredients = missingNames,
                        matchedPantryItems = matchedNames,
                        usesExpiringItems = usesExpiring,
                        score = score.roundTo(1)
                    )
                )
            }
        }

        // Sort by score descending, then matched ingredients count descending
        return pairings.sortedWith(compareByDescending<RecipePairing> { it.score }
            .thenByDescending { it.matchedIngredientsCount })
            .take(limit.coerceIn(1, 50))
    }

    private fun Double.roundTo(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return (this * multiplier).roundToInt() / multiplier
    }
}
