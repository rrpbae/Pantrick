# 🔍 RUNTIME BUG ANALYSIS - Chicken Matching Issue

## 📋 USER REPORT

**Pantry**: chicken, quantity: 1 buah
**Recipe**: Fully Salted Roast Chicken
**Recipe Ingredient**: "1 (3 1/2–4-lb.) chicken"
**Status in UI**: "Belum tersedia" (NOT AVAILABLE) ❌

**Expected**: chicken should be MATCHED and AVAILABLE ✅

---

## 🔍 ROOT CAUSE ANALYSIS - PART A: INGREDIENT PARSING

### **Dataset Raw Ingredient**:
```
'1 (3 1/2–4-lb.) chicken'
```

### **IngredientParser Flow**:

1. **`parseSingleIngredient("1 (3 1/2–4-lb.) chicken")`**
   
2. **Quantity Extraction** (`extractQuantity`):
   - Pattern: `^(\d*\s*[¼½¾⅓⅔⅛⅜⅝⅞]|\d+\s+\d+/\d+|\d+/\d+|\d+(?:\.\d+)?)`
   - Match: `"1"` ✅
   - Remainder: `"(3 1/2–4-lb.) chicken"`

3. **Unit Extraction** (`extractUnit`):
   - Pattern: `^(tsp\.?|teaspoons?|tbsp\.?|...|lbs?\.?|pounds?|...)`
   - Text to match: `"(3 1/2–4-lb.) chicken"`
   - **PROBLEM**: Pattern looks for unit AT THE START `^`
   - `"(3 1/2–4-lb.)"` starts with `(` → NO MATCH! ❌
   - Unit: `null`
   - Remainder: `"(3 1/2–4-lb.) chicken"` (unchanged)

4. **normalizeIngredientName()**:
   - Called on: `"1 (3 1/2–4-lb.) chicken"`  ← FULL RAW STRING!
   - Process:
     - `.lowercase()` → `"1 (3 1/2–4-lb.) chicken"`
     - `.replace(Regex("""[^\w\s]"""), " ")` → `"1  3 1 2 4 lb  chicken"` (punctuation → spaces)
     - `.replace(Regex("""\s+"""), " ")` → `"1 3 1 2 4 lb chicken"`
     - `.trim()` → `"1 3 1 2 4 lb chicken"`
   - **NORMALIZED NAME**: `"1 3 1 2 4 lb chicken"` ❌❌❌

5. **RecipeIngredient Object**:
   ```kotlin
   RecipeIngredient(
       raw = "1 (3 1/2–4-lb.) chicken",
       displayName = "1 (3 1/2–4-lb.) chicken",
       normalizedName = "1 3 1 2 4 lb chicken",  ← WRONG!
       quantity = "1",
       unit = null
   )
   ```

---

### **Pantry Item**:
```kotlin
PantryItem(
    name = "chicken",
    normalizedName = "chicken",  ← Correct
    quantity = 1.0,
    unit = "buah"
)
```

---

### **Ingredient Matching** (`CookingSessionService.buildAvailability()`):

```kotlin
val ingNorm = ing.normalizedName  // "1 3 1 2 4 lb chicken"
val pNorm = pantry.normalizedName  // "chicken"

IngredientMatchingService.isIngredientMatch(ingNorm, pNorm)
```

**Matching Checks**:

1. **Exact match**: `"1 3 1 2 4 lb chicken" == "chicken"` → NO ❌
2. **Word boundary match**:
   - ingWords: `["1", "3", "1", "2", "4", "lb", "chicken"]`
   - pantryWords: `["chicken"]`
   - Check if `"chicken"` in ingWords: YES ✅
   - **SHOULD MATCH** but let's continue...

3. **Word check**: `pantryWords.any { it == ingNorm }`
   - Check if `"chicken" == "1 3 1 2 4 lb chicken"` → NO

4. **Alias match**: `checkAliasMatch("1 3 1 2 4 lb chicken", "chicken")`
   - Check INGREDIENT_ALIASES for "1 3 1 2 4 lb chicken": NO ENTRY
   - Check INGREDIENT_ALIASES for "chicken": YES! `listOf("ayam", "chickens")`
   - Check if "1 3 1 2 4 lb chicken" in ["ayam", "chickens"]: NO
   - **Word-level alias**:
     - aWords: ["1", "3", "1", "2", "4", "lb", "chicken"]
     - bWords: ["chicken"]
     - For "chicken" in aWords: Check INGREDIENT_ALIASES["chicken"] = ["ayam", "chickens"]
     - Check if any bWord in aliases: "chicken" in ["ayam", "chickens"]? NO ❌
   
   **BUG FOUND!** Alias check fails because it checks if pantry word is in alias list, but should check if INGREDIENT word matches pantry!

5. **Fuzzy match**: `isFuzzyMatch("1 3 1 2 4 lb chicken", "chicken")`
   - Levenshtein distance between "1 3 1 2 4 lb chicken" (22 chars) and "chicken" (7 chars)
   - Distance: ~15
   - Similarity: 1 - (15/22) = ~32% < 85% threshold
   - NO MATCH ❌

**RESULT**: NO MATCH FOUND! ❌

---

## 🎯 ROOT CAUSES IDENTIFIED:

### **ROOT CAUSE #1: normalizedName includes quantity + weight spec**
- `IngredientParser.parseSingleIngredient()` calls `normalizeIngredientName(trimmedRaw)`
- `trimmedRaw` = full string "1 (3 1/2–4-lb.) chicken"
- Should normalize only the INGREDIENT NAME part after extracting quantity and unit!

### **ROOT CAUSE #2: Word boundary match NOT WORKING**
- Code: `if (ingWords.any { it == pantryNorm }) return true`
- ingWords contains "chicken", pantryNorm is "chicken"
- **THIS SHOULD RETURN TRUE** but doesn't seem to execute properly!
- Need to verify logic flow

### **ROOT CAUSE #3: Unit extraction fails for complex formats**
- `"(3 1/2–4-lb.)"` is NOT recognized as unit because pattern expects unit AT START `^`
- Pattern doesn't handle parenthesized weight ranges

---

## 🔧 SOLUTIONS NEEDED:

### **FIX #1: Normalize only ingredient name, NOT full raw string**

**Current**:
```kotlin
fun parseSingleIngredient(raw: String): RecipeIngredient {
    val trimmedRaw = raw.trim()
    val normalizedName = normalizeIngredientName(trimmedRaw)  ← WRONG!
    ...
}
```

**Should be**:
```kotlin
fun parseSingleIngredient(raw: String): RecipeIngredient {
    val trimmedRaw = raw.trim()
    val (quantity, remainderAfterQty) = extractQuantity(trimmedRaw)
    val (unit, remainderAfterUnit) = extractUnit(remainderAfterQty)
    
    // Normalize ONLY the ingredient name part (after qty + unit)
    val ingredientNamePart = remainderAfterUnit.ifBlank { remainderAfterQty.ifBlank { trimmedRaw } }
    val normalizedName = normalizeIngredientName(ingredientNamePart)  ← CORRECT!
    ...
}
```

### **FIX #2: Fix word boundary match logic**

**Current** (line ~57 in IngredientMatchingService):
```kotlin
// Check if pantryName is a word in ingredient
if (ingWords.any { it == pantryNorm }) return true
```

**This should work!** Need to verify why it's not returning true.

Possible issue: Maybe there's an early return or condition that prevents this from executing?

### **FIX #3: Improve unit extraction for complex formats** (OPTIONAL)

For cases like `"(3 1/2–4-lb.)"`, either:
- Strip parentheses before unit extraction
- Or accept that some recipes have ambiguous quantity specifications and handle gracefully

---

## 📊 EXPECTED BEHAVIOR AFTER FIX:

### **After Fix #1**:
```kotlin
RecipeIngredient(
    raw = "1 (3 1/2–4-lb.) chicken",
    displayName = "1 (3 1/2–4-lb.) chicken",
    normalizedName = "chicken",  ← FIXED!
    quantity = "1",
    unit = null  // or "lb" if Fix #3 implemented
)
```

### **Matching Flow**:
```
ingNorm = "chicken"
pNorm = "chicken"

1. Exact match: "chicken" == "chicken" → YES ✅
2. MATCH FOUND!
3. canCook = true
4. UI shows "Tersedia" ✅
```

---

## 🔍 NEXT STEPS:

1. ✅ **Verify** the exact code flow in `IngredientMatchingService.isIngredientMatch()`
2. ✅ **Fix** `IngredientParser.parseSingleIngredient()` to normalize only ingredient name
3. ✅ **Test** with runtime data to confirm fix
4. ✅ **Document** any cases where quantity/unit extraction fails and define fallback behavior

---

## PART B: SAVED RECIPE IMAGE ISSUE

**TO BE INVESTIGATED** after fixing chicken matching issue.

Preliminary hypothesis:
- Recipe Detail loads image successfully → `imageName` present
- Saved Recipe does NOT show image → `imageName` missing or URL wrong
- Need to check:
  - SavedRecipe API response structure
  - RecipesScreen image loading logic
  - Image URL construction

---

**Status**: ROOT CAUSE IDENTIFIED for chicken matching  
**Next**: Implement Fix #1 in IngredientParser.kt
