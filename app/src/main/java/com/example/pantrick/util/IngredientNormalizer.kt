// [Materi: Utility & Normalization] Alat bantu normalisasi bahan untuk pencocokan Indonesia ↔ English
package com.example.pantrick.util

/**
 * Normalisasi dan pencocokan bahan masakan antara input Bahasa Indonesia dengan dataset Bahasa Inggris.
 *
 * Dataset backend menggunakan nama bahan Bahasa Inggris (chicken, onion, milk, dst.)
 * sementara user Pantrick menginput dalam Bahasa Indonesia (ayam, bawang, susu, dst.)
 *
 * Semua matching dibandingkan menggunakan canonical English name agar konsisten.
 */
object IngredientNormalizer {

    // =====================================================================
    // ALIAS DICTIONARY: Indonesian/variant → canonical English ingredient
    // =====================================================================
    private val ALIAS_MAP: Map<String, String> = mapOf(
        // --- DAGING & UNGGAS ---
        "ayam" to "chicken",
        "daging ayam" to "chicken",
        "fillet ayam" to "chicken",
        "dada ayam" to "chicken breast",
        "paha ayam" to "chicken thigh",
        "sayap ayam" to "chicken wing",
        "daging sapi" to "beef",
        "sapi" to "beef",
        "daging giling" to "ground beef",
        "daging cincang" to "ground beef",
        "babi" to "pork",
        "daging babi" to "pork",
        "ikan" to "fish",
        "ikan tuna" to "tuna",
        "ikan salmon" to "salmon",
        "ikan kod" to "cod",
        "udang" to "shrimp",
        "cumi" to "squid",
        "cumi-cumi" to "squid",
        "kepiting" to "crab",
        "kerang" to "clam",
        "kambing" to "lamb",
        "daging kambing" to "lamb",
        "bebek" to "duck",
        "daging bebek" to "duck",
        "sosis" to "sausage",
        "bacon" to "bacon",
        "ham" to "ham",
        "kornet" to "corned beef",

        // --- TELUR & SUSU ---
        "telur" to "egg",
        "telur ayam" to "egg",
        "telur bebek" to "duck egg",
        "kuning telur" to "egg yolk",
        "putih telur" to "egg white",
        "susu" to "milk",
        "susu cair" to "milk",
        "susu sapi" to "milk",
        "susu full cream" to "whole milk",
        "susu skim" to "skim milk",
        "susu kental manis" to "condensed milk",
        "krim" to "cream",
        "krim kental" to "heavy cream",
        "buttermilk" to "buttermilk",
        "keju" to "cheese",
        "keju cheddar" to "cheddar cheese",
        "keju mozzarella" to "mozzarella cheese",
        "keju parmesan" to "parmesan cheese",
        "keju ricotta" to "ricotta cheese",
        "keju cream" to "cream cheese",
        "mentega" to "butter",
        "margarin" to "margarine",
        "yogurt" to "yogurt",
        "yoghurt" to "yogurt",

        // --- SAYURAN ---
        "bawang" to "onion",
        "bawang bombay" to "onion",
        "bawang merah" to "shallot",
        "bawang putih" to "garlic",
        "bawang daun" to "scallion",
        "bawang prei" to "leek",
        "daun bawang" to "scallion",
        "wortel" to "carrot",
        "kentang" to "potato",
        "tomat" to "tomato",
        "timun" to "cucumber",
        "mentimun" to "cucumber",
        "brokoli" to "broccoli",
        "kembang kol" to "cauliflower",
        "kubis" to "cabbage",
        "sawi" to "bok choy",
        "bayam" to "spinach",
        "kangkung" to "water spinach",
        "selada" to "lettuce",
        "paprika" to "bell pepper",
        "paprika merah" to "red bell pepper",
        "paprika hijau" to "green bell pepper",
        "cabai" to "chili",
        "cabe" to "chili",
        "cabai merah" to "red chili",
        "cabai hijau" to "green chili",
        "cabai rawit" to "cayenne pepper",
        "jagung" to "corn",
        "kacang panjang" to "long beans",
        "kacang polong" to "peas",
        "kacang kapri" to "peas",
        "kacang edamame" to "edamame",
        "kacang hijau" to "mung bean",
        "kacang merah" to "kidney bean",
        "kacang hitam" to "black bean",
        "tahu" to "tofu",
        "tempe" to "tempeh",
        "jamur" to "mushroom",
        "jamur tiram" to "oyster mushroom",
        "jamur shiitake" to "shiitake mushroom",
        "terong" to "eggplant",
        "labu" to "pumpkin",
        "labu siam" to "chayote",
        "rebung" to "bamboo shoot",
        "asparagus" to "asparagus",
        "zucchini" to "zucchini",
        "seledri" to "celery",
        "peterseli" to "parsley",

        // --- BUMBU & REMPAH ---
        "garam" to "salt",
        "gula" to "sugar",
        "gula pasir" to "sugar",
        "gula merah" to "brown sugar",
        "gula aren" to "palm sugar",
        "madu" to "honey",
        "lada" to "pepper",
        "lada hitam" to "black pepper",
        "merica" to "black pepper",
        "merica hitam" to "black pepper",
        "merica bubuk" to "black pepper",
        "lada putih" to "white pepper",
        "lada merah" to "red pepper",
        "cabai bubuk" to "chili powder",
        "ketumbar" to "coriander",
        "jintan" to "cumin",
        "kunyit" to "turmeric",
        "jahe" to "ginger",
        "lengkuas" to "galangal",
        "serai" to "lemongrass",
        "daun salam" to "bay leaf",
        "daun jeruk" to "kaffir lime leaf",
        "pala" to "nutmeg",
        "kayu manis" to "cinnamon",
        "cengkeh" to "cloves",
        "kapulaga" to "cardamom",
        "adas" to "fennel",
        "biji adas" to "fennel seeds",
        "biji wijen" to "sesame seeds",
        "wijen" to "sesame",
        "paprika bubuk" to "paprika",
        "oregano" to "oregano",
        "basil" to "basil",
        "daun basil" to "basil",
        "thyme" to "thyme",
        "rosemary" to "rosemary",
        "daun mint" to "mint",
        "mint" to "mint",
        "vanili" to "vanilla",
        "ekstrak vanila" to "vanilla extract",
        "saffron" to "saffron",

        // --- BAHAN DAPUR ---
        "tepung" to "flour",
        "tepung terigu" to "flour",
        "tepung maizena" to "cornstarch",
        "tepung beras" to "rice flour",
        "tepung roti" to "breadcrumbs",
        "nasi" to "rice",
        "beras" to "rice",
        "pasta" to "pasta",
        "mie" to "noodle",
        "mi" to "noodle",
        "mie instan" to "instant noodle",
        "roti" to "bread",
        "roti tawar" to "bread",
        "minyak" to "oil",
        "minyak goreng" to "oil",
        "minyak sayur" to "vegetable oil",
        "minyak zaitun" to "olive oil",
        "minyak wijen" to "sesame oil",
        "minyak kelapa" to "coconut oil",
        "santan" to "coconut milk",
        "kecap manis" to "sweet soy sauce",
        "kecap asin" to "soy sauce",
        "kecap" to "soy sauce",
        "saus tiram" to "oyster sauce",
        "saus tomat" to "ketchup",
        "sambal" to "chili sauce",
        "cuka" to "vinegar",
        "jeruk nipis" to "lime",
        "jeruk lemon" to "lemon",
        "lemon" to "lemon",
        "lime" to "lime",
        "kaldu" to "broth",
        "kaldu ayam" to "chicken broth",
        "kaldu sapi" to "beef broth",
        "kaldu sayuran" to "vegetable broth",
        "soda kue" to "baking soda",
        "baking powder" to "baking powder",
        "coklat" to "chocolate",
        "dark chocolate" to "dark chocolate",
        "coklat bubuk" to "cocoa powder",
        "selai" to "jam",
        "selai kacang" to "peanut butter",
        "kacang tanah" to "peanut",
        "kacang almond" to "almond",
        "kacang mete" to "cashew",
        "kacang kenari" to "walnut",
        "alpukat" to "avocado",
        "pisang" to "banana",
        "apel" to "apple",
        "jeruk" to "orange",
        "mangga" to "mango",
        "nanas" to "pineapple",
        "stroberi" to "strawberry",
        "anggur" to "grape",
        "semangka" to "watermelon",
        "melon" to "melon",
        "pepaya" to "papaya",

        // --- LAIN-LAIN ---
        "air" to "water",
        "es batu" to "ice",
        "maple syrup" to "maple syrup",
        "sirup" to "syrup",
        "sirup maple" to "maple syrup"
    )

    // Unit-related words to strip from beginning of normalized names
    private val UNIT_WORDS = setOf(
        "tsp", "tbsp", "cup", "cups", "oz", "lb", "lbs", "g", "kg", "ml", "l",
        "tablespoon", "tablespoons", "teaspoon", "teaspoons", "ounce", "ounces",
        "pound", "pounds", "gram", "grams", "liter", "liters", "milliliter", "milliliters",
        "clove", "cloves", "slice", "slices", "pinch", "can", "cans", "bottle",
        "piece", "pieces", "bunch", "stick", "sticks", "head", "heads",
        "sprig", "sprigs", "dash", "dashes", "packet", "packets", "stem", "stalks",
        "of", "a", "an"
    )

    /**
     * Mengekstrak nama inti bahan dari string normalisasi backend.
     * Contoh:
     *   "1 tbsp finely chopped rosemary" → "rosemary"
     *   "2 cups unsalted chicken broth"  → "chicken broth"
     *   "freshly ground black pepper"    → "black pepper"
     *   "kosher salt"                    → "salt"
     */
    fun extractCoreName(normalized: String): String {
        var tokens = normalized
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .split(" ")
            .toMutableList()

        // Drop leading numbers and fraction artifacts
        while (tokens.isNotEmpty() && (tokens.first().all { it.isDigit() } || tokens.first().isBlank())) {
            tokens.removeAt(0)
        }
        // Drop leading unit words
        while (tokens.isNotEmpty() && tokens.first() in UNIT_WORDS) {
            tokens.removeAt(0)
        }

        return tokens.joinToString(" ").ifBlank { normalized.trim().lowercase() }
    }

    /**
     * Normalisasi nama bahan dari input user (Bahasa Indonesia atau Inggris).
     * Kembalikan canonical English name jika ada alias, atau cukup lowercase & trim.
     *
     * Contoh:
     *   "Ayam"       → "chicken"
     *   "lada hitam" → "black pepper"
     *   "Butter"     → "butter"
     */
    fun normalizeUserInput(input: String): String {
        val clean = input.trim().lowercase()
        return ALIAS_MAP[clean] ?: clean
    }

    /**
     * Cek apakah ingredient dari resep ada di pantry user.
     * Menggunakan pencocokan dua arah yang toleran terhadap partial match.
     *
     * @param ingredientNormalized  normalizedName dari backend (mis. "1 tbsp unsalted butter")
     * @param ingredientRaw         raw string dari backend (mis. "1 Tbsp. unsalted butter")
     * @param pantryItemName        nama bahan dari pantry user (bisa Bahasa Indonesia)
     */
    fun isMatch(
        ingredientNormalized: String,
        ingredientRaw: String,
        pantryItemName: String
    ): Boolean {
        val pantryCanonical = normalizeUserInput(pantryItemName)
        val ingredientCore = extractCoreName(ingredientNormalized)

        // 1. Exact / substring match antara canonical pantry dan core ingredient
        if (ingredientCore.contains(pantryCanonical) || pantryCanonical.contains(ingredientCore)) {
            return true
        }

        // 2. Cek setiap kata pantry canonical terhadap core ingredient
        val pantryTokens = pantryCanonical.split(" ").filter { it.length > 2 }
        val coreTokens = ingredientCore.split(" ").filter { it.length > 2 }
        if (pantryTokens.isNotEmpty() && coreTokens.isNotEmpty()) {
            if (pantryTokens.any { pt -> coreTokens.any { ct -> pt == ct } }) {
                return true
            }
        }

        // 3. Fallback: cek raw string juga (untuk edge case)
        val rawLower = ingredientRaw.trim().lowercase()
        val rawCore = extractCoreName(rawLower)
        if (rawCore.contains(pantryCanonical) || pantryCanonical.contains(rawCore)) {
            return true
        }

        return false
    }

    /**
     * Mendapatkan nama tampilan bersih untuk ingredient dari backend.
     * Menghapus quantity dan unit dari awal string.
     *
     * Contoh:
     *   raw = "1 Tbsp. finely chopped rosemary", quantity = "1", unit = "Tbsp."
     *   → "Finely chopped rosemary"
     *
     *   raw = "Freshly ground black pepper", quantity = null
     *   → "Freshly ground black pepper"
     */
    fun getDisplayName(raw: String, quantity: String?, unit: String?): String {
        var text = raw.trim()
        // Hapus quantity dari awal
        if (quantity != null) {
            val afterQty = text.removePrefix(quantity).trimStart()
            if (afterQty.isNotBlank()) text = afterQty
        }
        // Hapus unit dari awal (setelah quantity)
        if (unit != null) {
            val afterUnit = text.removePrefix(unit).trimStart().removePrefix(".").trimStart()
            if (afterUnit.isNotBlank()) text = afterUnit
        }
        // Capitalize huruf pertama
        return text.replaceFirstChar { it.uppercase() }
    }

    /**
     * Mentranslasi daftar bahan dari input user (bisa Indonesia) ke canonical English
     * sebelum dikirim ke backend recommendation engine.
     */
    fun translateIngredientsForSearch(userInputs: Collection<String>): List<String> {
        return userInputs.map { normalizeUserInput(it) }
    }
}
