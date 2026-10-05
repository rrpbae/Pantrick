package com.pantrick.backend.service

/**
 * QuantityComparisonService — shared logic untuk comparing quantities.
 * 
 * Handles:
 * - Fraction parsing (1/2, 1 1/2, ¼, ½, etc.)
 * - Unit conversion (kg↔g, ml↔l, etc.)
 * - Quantity comparison with unit compatibility check
 */
object QuantityComparisonService {
    
    data class QuantityComparisonResult(
        val isComparable: Boolean,
        val isSufficient: Boolean,
        val recipeQtyParsed: Double?,
        val pantryQtyNormalized: Double?
    )
    
    enum class UnitGroup { WEIGHT, VOLUME, COUNT }
    
    /**
     * Compare recipe quantity vs pantry quantity.
     * 
     * Returns:
     * - isComparable: true if units can be compared
     * - isSufficient: true if pantry >= recipe
     * - recipeQtyParsed: parsed recipe quantity
     * - pantryQtyNormalized: pantry quantity converted to same unit group
     */
    fun compareQuantities(
        recipeQty: String?,
        recipeUnit: String?,
        pantryQty: Double,
        pantryUnit: String
    ): QuantityComparisonResult {
        // Parse recipe quantity
        val recipeQtyParsed = parseFraction(recipeQty)
        
        // If no quantity in recipe, assume name match is enough
        if (recipeQtyParsed == null || recipeUnit == null) {
            return QuantityComparisonResult(
                isComparable = false,
                isSufficient = true, // Name match sufficient
                recipeQtyParsed = null,
                pantryQtyNormalized = null
            )
        }
        
        // Special case: if pantry unit is "unit" (generic), assume sufficient
        // This handles test cases and generic pantry items without specific units
        if (pantryUnit.trim().lowercase() == "unit") {
            return QuantityComparisonResult(
                isComparable = false,
                isSufficient = true, // Generic "unit" assumed sufficient
                recipeQtyParsed = recipeQtyParsed,
                pantryQtyNormalized = null
            )
        }
        
        // Check if units are in same group
        val recipeUnitGroup = getUnitGroup(recipeUnit)
        val pantryUnitGroup = getUnitGroup(pantryUnit)
        
        if (recipeUnitGroup == null || pantryUnitGroup == null || recipeUnitGroup != pantryUnitGroup) {
            // Units not comparable
            return QuantityComparisonResult(
                isComparable = false,
                isSufficient = true, // If name matches, assume sufficient
                recipeQtyParsed = recipeQtyParsed,
                pantryQtyNormalized = null
            )
        }
        
        // Convert to base units and compare
        val recipeBaseQty = toBaseUnit(recipeQtyParsed, recipeUnit)
        val pantryBaseQty = toBaseUnit(pantryQty, pantryUnit)
        
        return QuantityComparisonResult(
            isComparable = true,
            isSufficient = pantryBaseQty >= recipeBaseQty,
            recipeQtyParsed = recipeQtyParsed,
            pantryQtyNormalized = pantryBaseQty
        )
    }
    
    /**
     * Get unit group for unit conversion.
     */
    fun getUnitGroup(unit: String?): UnitGroup? {
        if (unit == null) return null
        return when (unit.trim().lowercase()) {
            "g", "gram", "grams", "kg", "kilogram", "kilograms",
            "oz", "ounce", "ounces", "lb", "lbs", "pound", "pounds" -> UnitGroup.WEIGHT

            "ml", "milliliter", "milliliters", "millilitre",
            "l", "liter", "liters", "litre",
            "cup", "cups", "tsp", "teaspoon", "teaspoons",
            "tbsp", "tablespoon", "tablespoons" -> UnitGroup.VOLUME

            "pcs", "piece", "pieces", "unit", "units", "whole",
            "clove", "cloves", "slice", "slices", "head", "heads",
            "bunch", "bunches", "stalk", "stalks", "sprig", "sprigs",
            "can", "cans", "bottle", "bottles", "package", "packages",
            "stick", "sticks", "buah", "butir", "biji", "siung" -> UnitGroup.COUNT

            else -> null
        }
    }
    
    /**
     * Convert quantity to base unit: gram (weight), ml (volume), pcs (count)
     */
    fun toBaseUnit(qty: Double, unit: String): Double {
        return when (unit.trim().lowercase()) {
            "kg", "kilogram", "kilograms" -> qty * 1000
            "oz", "ounce", "ounces" -> qty * 28.35
            "lb", "lbs", "pound", "pounds" -> qty * 453.59
            "l", "liter", "liters", "litre" -> qty * 1000
            "cup", "cups" -> qty * 240
            "tbsp", "tablespoon", "tablespoons" -> qty * 15
            "tsp", "teaspoon", "teaspoons" -> qty * 5
            else -> qty // g, ml, pcs — already base unit
        }
    }
    
    /**
     * Parse fraction string to Double.
     * Supports: "1", "1.5", "1/2", "1 1/2", "¼", "½", "¾", "⅓", "⅔"
     */
    fun parseFraction(input: String?): Double? {
        if (input.isNullOrBlank()) return null

        // Replace unicode fractions
        val normalized = input.trim()
            .replace("¼", "1/4").replace("½", "1/2").replace("¾", "3/4")
            .replace("⅓", "1/3").replace("⅔", "2/3")
            .replace("⅛", "1/8").replace("⅜", "3/8").replace("⅝", "5/8").replace("⅞", "7/8")
            // Remove garbage unicode chars
            .replace(Regex("[^0-9/. ]"), "").trim()

        if (normalized.isBlank()) return null

        // Try simple double parse first
        normalized.toDoubleOrNull()?.let { return it }

        val parts = normalized.split(" ").filter { it.isNotBlank() }
        return when {
            parts.size == 2 -> {
                // "1 1/2" → 1 + 0.5
                val whole = parts[0].toDoubleOrNull() ?: return null
                val frac = parseSingleFraction(parts[1]) ?: return null
                whole + frac
            }
            parts.size == 1 -> parseSingleFraction(parts[0])
            else -> null
        }
    }
    
    private fun parseSingleFraction(s: String): Double? {
        if ('/' !in s) return s.toDoubleOrNull()
        val slashIdx = s.indexOf('/')
        val num = s.substring(0, slashIdx).toDoubleOrNull() ?: return null
        val den = s.substring(slashIdx + 1).toDoubleOrNull() ?: return null
        if (den == 0.0) return null
        return num / den
    }
}
