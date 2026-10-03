package com.pantrick.backend.service

import com.pantrick.backend.models.CreatePantryItemRequest
import com.pantrick.backend.models.ExpirationPriority
import com.pantrick.backend.models.ExpirationStatus
import com.pantrick.backend.models.ExpiringPantryItem
import com.pantrick.backend.models.PantryItem
import com.pantrick.backend.models.PantryItemResponseDto
import com.pantrick.backend.models.PantrySummaryData
import com.pantrick.backend.models.StorageType
import com.pantrick.backend.models.UpdatePantryItemRequest
import com.pantrick.backend.repository.PantryRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

class PantryService(
    private val pantryRepository: PantryRepository
) {

    fun getPantryItems(
        userId: Int,
        storageTypeFilter: String? = null,
        categoryFilter: String? = null,
        searchQuery: String? = null,
        statusFilter: String? = null,
        nowDate: LocalDate = LocalDate.now()
    ): List<PantryItemResponseDto> {
        var items = pantryRepository.getItemsByUserId(userId)

        if (!storageTypeFilter.isNullOrBlank() && !storageTypeFilter.equals("All", ignoreCase = true)) {
            val targetStorage = StorageType.fromString(storageTypeFilter)
            items = items.filter { it.storageType == targetStorage }
        }

        if (!categoryFilter.isNullOrBlank() && !categoryFilter.equals("All", ignoreCase = true)) {
            items = items.filter { it.category.equals(categoryFilter.trim(), ignoreCase = true) }
        }

        if (!searchQuery.isNullOrBlank()) {
            val query = searchQuery.trim().lowercase()
            items = items.filter {
                it.name.lowercase().contains(query) ||
                        it.ingredientName.lowercase().contains(query) ||
                        it.normalizedName.contains(query)
            }
        }

        val dtoList = items.map { toResponseDto(it, nowDate) }

        if (!statusFilter.isNullOrBlank()) {
            val targetStatus = statusFilter.trim().uppercase()
            return dtoList.filter { it.expirationStatus?.name == targetStatus }
        }

        return dtoList
    }

    fun getPantryItemById(id: String, userId: Int, nowDate: LocalDate = LocalDate.now()): PantryItemResponseDto? {
        val item = pantryRepository.getItemById(id) ?: return null
        if (item.userId != userId) return null
        return toResponseDto(item, nowDate)
    }

    fun createPantryItem(userId: Int, request: CreatePantryItemRequest, nowDate: LocalDate = LocalDate.now()): PantryItemResponseDto {
        val name = request.name?.trim() ?: throw IllegalArgumentException("Nama item tidak boleh kosong")
        if (name.isBlank()) throw IllegalArgumentException("Nama item tidak boleh kosong")

        val quantity = request.quantity ?: 1.0
        if (quantity <= 0) throw IllegalArgumentException("Quantity harus lebih dari 0")

        val unit = request.unit?.trim() ?: throw IllegalArgumentException("Unit tidak boleh kosong")
        if (unit.isBlank()) throw IllegalArgumentException("Unit tidak boleh kosong")

        val storageType = StorageType.fromString(request.storageType)
        val category = request.category?.trim()?.ifBlank { "Pantry" } ?: "Pantry"
        val expDate = request.expirationDate?.trim()?.ifBlank { null }

        if (expDate != null) {
            validateDateFormat(expDate)
        }

        val nowStr = nowDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val newItem = PantryItem(
            id = "",
            userId = userId,
            name = name,
            ingredientName = name,
            normalizedName = IngredientParser.normalizeIngredientName(name),
            quantity = quantity,
            unit = unit,
            category = category,
            storageType = storageType,
            expirationDate = expDate,
            imageUrl = request.imageUrl?.trim()?.ifBlank { null },
            createdAt = nowStr,
            updatedAt = nowStr
        )

        val created = pantryRepository.addItem(newItem)
        return toResponseDto(created, nowDate)
    }

    fun updatePantryItem(id: String, userId: Int, request: UpdatePantryItemRequest, nowDate: LocalDate = LocalDate.now()): PantryItemResponseDto? {
        val existing = pantryRepository.getItemById(id) ?: return null
        if (existing.userId != userId) return null

        val name = request.name?.trim() ?: existing.name
        if (name.isBlank()) throw IllegalArgumentException("Nama item tidak boleh kosong")

        val quantity = request.quantity ?: existing.quantity
        if (quantity <= 0) throw IllegalArgumentException("Quantity harus lebih dari 0")

        val unit = request.unit?.trim() ?: existing.unit
        if (unit.isBlank()) throw IllegalArgumentException("Unit tidak boleh kosong")

        val storageType = if (request.storageType != null) StorageType.fromString(request.storageType) else existing.storageType
        val category = request.category?.trim()?.ifBlank { existing.category } ?: existing.category
        val expDate = if (request.expirationDate != null) request.expirationDate.trim().ifBlank { null } else existing.expirationDate

        if (expDate != null) {
            validateDateFormat(expDate)
        }

        val imageUrl = if (request.imageUrl != null) request.imageUrl.trim().ifBlank { null } else existing.imageUrl
        val isConsumed = request.isConsumed ?: existing.isConsumed

        val nowStr = nowDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

        val updatedItem = existing.copy(
            name = name,
            ingredientName = name,
            normalizedName = IngredientParser.normalizeIngredientName(name),
            quantity = quantity,
            unit = unit,
            category = category,
            storageType = storageType,
            expirationDate = expDate,
            imageUrl = imageUrl,
            isConsumed = isConsumed,
            updatedAt = nowStr
        )

        val updated = pantryRepository.updateItem(updatedItem) ?: return null
        return toResponseDto(updated, nowDate)
    }

    fun deletePantryItem(id: String, userId: Int): Boolean {
        return pantryRepository.deleteItem(id, userId)
    }

    fun getPantryCategories(userId: Int): List<String> {
        val items = pantryRepository.getItemsByUserId(userId)
        val categories = items.map { it.category.trim() }.filter { it.isNotBlank() }.toSet().toMutableList()
        if (categories.isEmpty()) {
            categories.addAll(listOf("Dairy & Eggs", "Produce", "Meat", "Pantry", "Beverages"))
        }
        return categories.sorted()
    }

    fun getPantrySummary(userId: Int, nowDate: LocalDate = LocalDate.now()): PantrySummaryData {
        val items = pantryRepository.getItemsByUserId(userId)
        var fridge = 0
        var freezer = 0
        var pantry = 0
        var expiringSoon = 0
        var expired = 0

        for (item in items) {
            when (item.storageType) {
                StorageType.FRIDGE -> fridge++
                StorageType.FREEZER -> freezer++
                StorageType.PANTRY -> pantry++
            }

            val expStatus = calculateExpirationStatus(item.expirationDate, nowDate)?.first
            if (expStatus == ExpirationStatus.EXPIRED) {
                expired++
            } else if (expStatus == ExpirationStatus.EXPIRING_SOON) {
                expiringSoon++
            }
        }

        return PantrySummaryData(
            fridge = fridge,
            freezer = freezer,
            pantry = pantry,
            totalItems = items.size,
            expiringSoonItems = expiringSoon,
            expiredItems = expired
        )
    }

    fun getNeedsAttentionItems(userId: Int, nowDate: LocalDate = LocalDate.now()): List<PantryItemResponseDto> {
        val items = pantryRepository.getItemsByUserId(userId)
        val result = mutableListOf<PantryItemResponseDto>()

        for (item in items) {
            val dto = toResponseDto(item, nowDate)
            if (dto.expirationStatus == ExpirationStatus.EXPIRED || dto.expirationStatus == ExpirationStatus.EXPIRING_SOON) {
                result.add(dto)
            }
        }

        return result.sortedWith(compareBy({ it.daysRemaining ?: Long.MAX_VALUE }, { it.name }))
    }

    fun toResponseDto(item: PantryItem, nowDate: LocalDate): PantryItemResponseDto {
        val (status, daysRemaining) = calculateExpirationStatus(item.expirationDate, nowDate)
            ?: Pair(null, null)

        val imageUrl = if (item.imageUrl.isNullOrBlank()) "/api/pantry/items/${item.id}/image" else item.imageUrl

        return PantryItemResponseDto(
            id = item.id,
            userId = item.userId,
            name = item.name,
            quantity = item.quantity,
            unit = item.unit,
            category = item.category,
            storageType = item.storageType,
            expirationDate = item.expirationDate,
            expirationStatus = status,
            daysRemaining = daysRemaining,
            imageUrl = imageUrl,
            isConsumed = item.isConsumed,
            createdAt = item.createdAt,
            updatedAt = item.updatedAt
        )
    }

    private fun calculateExpirationStatus(dateStr: String?, nowDate: LocalDate): Pair<ExpirationStatus, Long>? {
        if (dateStr.isNullOrBlank()) return null
        val expDate = try {
            LocalDate.parse(dateStr)
        } catch (e: Exception) {
            return null
        }

        val daysRemaining = ChronoUnit.DAYS.between(nowDate, expDate)
        val status = when {
            daysRemaining < 0 -> ExpirationStatus.EXPIRED
            daysRemaining <= 3 -> ExpirationStatus.EXPIRING_SOON
            else -> ExpirationStatus.FRESH
        }
        return Pair(status, daysRemaining)
    }

    private fun validateDateFormat(dateStr: String) {
        try {
            LocalDate.parse(dateStr)
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Format expirationDate tidak valid, gunakan format YYYY-MM-DD")
        }
    }
}
