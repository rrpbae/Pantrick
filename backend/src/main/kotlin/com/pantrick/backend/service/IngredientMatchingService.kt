package com.pantrick.backend.service

/**
 * IngredientMatchingService — shared logic untuk matching ingredient names.
 * 
 * Digunakan oleh:
 * - RecommendationService (untuk recommendation list)
 * - CookingSessionService (untuk cooking readiness check)
 * 
 * Tujuan: Konsistensi hasil matching antara recommendation dan cooking readiness.
 */
object IngredientMatchingService {
    
    /**
     * Ingredient aliases untuk handling bahasa Indonesia ↔ English.
     * Format: normalized form → list of accepted variations
     */
    private val INGREDIENT_ALIASES = mapOf(
        // Poultry & Meat
        "chicken" to listOf("ayam", "chickens"),
        "ayam" to listOf("chicken", "chickens"),
        "beef" to listOf("daging sapi", "daging", "sapi"),
        "daging" to listOf("meat", "beef", "daging sapi"),
        "pork" to listOf("daging babi", "babi"),
        
        // Seafood
        "fish" to listOf("ikan"),
        "ikan" to listOf("fish", "fishes"),
        "shrimp" to listOf("udang", "shrimps"),
        "udang" to listOf("shrimp", "shrimps"),
        
        // Vegetables
        "onion" to listOf("bawang", "bawang merah", "onions"),
        "bawang" to listOf("onion", "onions"),
        "garlic" to listOf("bawang putih", "garlics"),
        "tomato" to listOf("tomat", "tomatoes"),
        "tomat" to listOf("tomato", "tomatoes"),
        "potato" to listOf("kentang", "potatoes"),
        "kentang" to listOf("potato", "potatoes"),
        "carrot" to listOf("wortel", "carrots"),
        "wortel" to listOf("carrot", "carrots"),
        "cabbage" to listOf("kol", "kubis", "cabbages"),
        "chili" to listOf("cabai", "cabe", "chilis", "chilies"),
        "cabai" to listOf("chili", "cabe", "chilis"),
        
        // Eggs & Dairy
        "egg" to listOf("telur", "telor", "eggs"),
        "telur" to listOf("egg", "telor", "eggs"),
        "milk" to listOf("susu"),
        "susu" to listOf("milk"),
        "cheese" to listOf("keju"),
        "keju" to listOf("cheese"),
        "butter" to listOf("mentega"),
        "mentega" to listOf("butter"),
        
        // Grains & Staples
        "rice" to listOf("nasi", "beras"),
        "nasi" to listOf("rice"),
        "beras" to listOf("rice"),
        "flour" to listOf("tepung"),
        "tepung" to listOf("flour"),
        "bread" to listOf("roti", "breads"),
        "roti" to listOf("bread", "breads"),
        
        // Condiments
        "salt" to listOf("garam"),
        "garam" to listOf("salt"),
        "sugar" to listOf("gula"),
        "gula" to listOf("sugar"),
        "oil" to listOf("minyak"),
        "minyak" to listOf("oil"),
        "soy sauce" to listOf("kecap", "kecap asin"),
        "kecap" to listOf("soy sauce")
    )
    
    /**
     * Check if ingredient matches pantry item name.
     * 
     * Matching rules:
     * 1. Exact match (after normalization)
     * 2. Word boundary match (avoid "rice" matching "licorice")
     * 3. Alias/synonym match (e.g., "chicken" ↔ "ayam")
     * 4. Fuzzy match for typos and plural (85% similarity threshold)
     */
    fun isIngredientMatch(ingNorm: String, pantryNorm: String): Boolean {
        // 1. Exact match
        if (ingNorm == pantryNorm) return true
        
        // 2. Word boundary match
        val ingWords = ingNorm.split(Regex("\\s+"))
        val pantryWords = pantryNorm.split(Regex("\\s+"))
        
        // Check if pantryName is a word in ingredient (e.g., "chicken" in "chicken breast")
        if (ingWords.any { it == pantryNorm }) return true
        
        // Check if ingredient is a word in pantryName (e.g., "breast" in "chicken breast")
        if (pantryWords.any { it == ingNorm }) return true
        
        // 3. Alias/synonym matching
        if (checkAliasMatch(ingNorm, pantryNorm)) return true
        
        // 4. Fuzzy match for typos and plural
        return isFuzzyMatch(ingNorm, pantryNorm)
    }
    
    /**
     * Check alias/synonym matching.
     * Example: "chicken" should match "ayam"
     */
    private fun checkAliasMatch(a: String, b: String): Boolean {
        // Check if 'a' has aliases that match 'b'
        INGREDIENT_ALIASES[a]?.let { aliases ->
            if (b in aliases) return true
            if (aliases.any { isFuzzyMatch(it, b) }) return true
        }
        
        // Check if 'b' has aliases that match 'a'
        INGREDIENT_ALIASES[b]?.let { aliases ->
            if (a in aliases) return true
            if (aliases.any { isFuzzyMatch(it, a) }) return true
        }
        
        // Check word-level alias matching
        val aWords = a.split(Regex("\\s+"))
        val bWords = b.split(Regex("\\s+"))
        
        for (aw in aWords) {
            INGREDIENT_ALIASES[aw]?.let { aliases ->
                if (bWords.any { it in aliases }) return true
            }
        }
        
        for (bw in bWords) {
            INGREDIENT_ALIASES[bw]?.let { aliases ->
                if (aWords.any { it in aliases }) return true
            }
        }
        
        return false
    }
    
    /**
     * Fuzzy match untuk menangani plural/singular dan typo kecil.
     * Example: "tomato" matches "tomatoes", "chicken" matches "chickens"
     */
    fun isFuzzyMatch(a: String, b: String): Boolean {
        // Handle plural: remove trailing 's', 'es'
        val aStem = a.removeSuffix("es").removeSuffix("s")
        val bStem = b.removeSuffix("es").removeSuffix("s")
        
        if (aStem == bStem) return true
        
        // Levenshtein distance for close matches
        val distance = levenshteinDistance(a, b)
        val maxLen = maxOf(a.length, b.length)
        if (maxLen == 0) return false
        
        val similarity = 1.0 - (distance.toDouble() / maxLen)
        return similarity >= 0.85 // 85% similarity threshold
    }
    
    /**
     * Calculate Levenshtein distance between two strings.
     */
    private fun levenshteinDistance(a: String, b: String): Int {
        val costs = IntArray(b.length + 1) { it }
        
        for (i in 1..a.length) {
            var lastValue = i
            for (j in 1..b.length) {
                val newValue = if (a[i - 1] == b[j - 1]) {
                    costs[j - 1]
                } else {
                    1 + minOf(costs[j - 1], costs[j], lastValue)
                }
                costs[j - 1] = lastValue
                lastValue = newValue
            }
            costs[b.length] = lastValue
        }
        
        return costs[b.length]
    }
}
