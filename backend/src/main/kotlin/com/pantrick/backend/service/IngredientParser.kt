package com.pantrick.backend.service

import com.pantrick.backend.models.RecipeIngredient

object IngredientParser {

    private val UNIT_PATTERN = Regex(
        """^(tsp\.?|teaspoons?|tbsp\.?|tablespoons?|cups?|oz\.?|ounces?|lbs?\.?|pounds?|g|grams?|kg|kilograms?|ml|milliliters?|l|liters?|cloves?|slices?|pinches?|cans?|bottles?|packets?|sticks?|heads?|pieces?|bunches?|dashes?|sprigs?|stems?|stalks?)(?=\s+|$|\b)""",
        RegexOption.IGNORE_CASE
    )

    private val QUANTITY_PATTERN = Regex(
        """^(\d*\s*[¼½¾⅓⅔⅛⅜⅝⅞]|\d+\s+\d+/\d+|\d+/\d+|\d+(?:\.\d+)?)"""
    )

    fun parseIngredientsList(rawString: String): List<RecipeIngredient> {
        if (rawString.isBlank()) return emptyList()
        val items = extractPythonListItems(rawString)
        return items.map { parseSingleIngredient(it) }
    }

    fun parseSingleIngredient(raw: String): RecipeIngredient {
        val trimmedRaw = raw.trim()
        val displayName = trimmedRaw

        val (quantity, remainderAfterQty) = extractQuantity(trimmedRaw)
        val (unit, remainderAfterUnit) = extractUnit(remainderAfterQty)
        
        // FIX: Normalize only the ingredient name part (after quantity and unit extraction)
        // For "1 (3 1/2–4-lb.) chicken", we want normalizedName = "chicken", not "1 3 1 2 4 lb chicken"
        val ingredientNamePart = remainderAfterUnit.ifBlank { 
            remainderAfterQty.ifBlank { trimmedRaw } 
        }
        val normalizedName = normalizeIngredientName(ingredientNamePart)

        return RecipeIngredient(
            raw = trimmedRaw,
            displayName = displayName,
            normalizedName = normalizedName,
            quantity = quantity,
            unit = unit
        )
    }

    fun normalizeIngredientName(name: String): String {
        return name.lowercase()
            .replace(Regex("""[^\w\s]"""), " ") // replace punctuation with space
            .replace(Regex("""\s+"""), " ")     // normalize multiple spaces
            .trim()
    }

    private fun extractQuantity(text: String): Pair<String?, String> {
        val match = QUANTITY_PATTERN.find(text) ?: return Pair(null, text)
        val qtyStr = match.value
        val remainder = text.substring(match.range.last + 1).trim()
        return Pair(qtyStr, remainder)
    }

    private fun extractUnit(text: String): Pair<String?, String> {
        val match = UNIT_PATTERN.find(text) ?: return Pair(null, text)
        val unitStr = match.value
        val remainder = text.substring(match.range.last + 1).trim()
        return Pair(unitStr, remainder)
    }

    private fun extractPythonListItems(input: String): List<String> {
        val trimmed = input.trim()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return listOf(trimmed)
        }

        val inner = trimmed.substring(1, trimmed.length - 1).trim()
        if (inner.isEmpty()) return emptyList()

        val results = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var quoteChar = ' '
        var i = 0

        while (i < inner.length) {
            val c = inner[i]
            if (inQuotes) {
                if (c == quoteChar) {
                    // Check if escaped
                    if (i > 0 && inner[i - 1] == '\\') {
                        sb.append(c)
                    } else if (i + 1 < inner.length && inner[i + 1] == quoteChar) {
                        // Double quote inside quoted string like ""
                        sb.append(c)
                        i++
                    } else {
                        inQuotes = false
                        results.add(sb.toString())
                        sb.clear()
                    }
                } else {
                    sb.append(c)
                }
            } else {
                if (c == '\'' || c == '"') {
                    inQuotes = true
                    quoteChar = c
                }
            }
            i++
        }

        if (sb.isNotEmpty() && results.isEmpty()) {
            results.add(sb.toString())
        }

        return results
    }
}
