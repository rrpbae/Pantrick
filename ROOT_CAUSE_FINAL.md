# 🚨 ROOT CAUSE ANALYSIS - FINAL DIAGNOSIS

## 📅 Date: 5 Oktober 2026

---

## ⚠️ CRITICAL ROOT CAUSE IDENTIFIED

### **Android Pantry is LOCAL ONLY - NO BACKEND SYNC!**

---

## 🔍 DATA FLOW AUDIT RESULTS

### **CURRENT FLOW (BROKEN)**:

```
User adds "chicken" in Android Pantry
    ↓
PantryViewModel.addItem()
    ↓
PantryRepository.addItem()
    ↓
PantrickPreferences.savePantryItems()  ← Saves to SharedPreferences (LOCAL)
    ↓
❌ NO BACKEND API CALL!
    ↓
User opens Recipe Detail
    ↓
RecipeDetailScreen → cookingViewModel.checkReadiness(recipeId, jwtToken)
    ↓
CookingSessionRepository.checkReadiness() → GET /api/recipes/{recipeId}/cook/readiness
    ↓
Backend CookingSessionService.checkReadiness(userId, recipeId)
    ↓
Backend: pantryRepository.getItemsByUserId(userId)  ← Gets BACKEND pantry
    ↓
Backend pantry is EMPTY or has DIFFERENT data than Android local storage!
    ↓
Result: chicken NOT FOUND ❌
    ↓
UI shows: "Belum tersedia" ❌
```

---

## 📂 FILE EVIDENCE

### **Android Pantry Storage**:

**File**: `app/src/main/java/com/example/pantrick/data/repository/PantryRepository.kt`

```kotlin
class PantryRepository(private val preferences: PantrickPreferences) {
    
    fun addItem(email: String, item: PantryItem): List<PantryItem> {
        val currentItems = preferences.getPantryItems(email).toMutableList()
        currentItems.add(0, item)
        preferences.savePantryItems(email, currentItems)  // ← LOCAL ONLY!
        return currentItems
    }
    
    // No backend API calls anywhere!
}
```

**File**: `app/src/main/java/com/example/pantrick/data/local/PantrickPreferences.kt`

```kotlin
fun savePantryItems(email: String, items: List<PantryItem>) {
    val normalized = User.normalizeEmail(email)
    val rawJson = json.encodeToString(items)
    prefs.edit().putString("$KEY_PANTRY_PREFIX$normalized", rawJson).apply()
    // ← Saves to SharedPreferences only!
}
```

### **Backend Pantry API EXISTS but NOT USED**:

**File**: `backend/src/main/kotlin/com/pantrick/backend/routes/PantryRoutes.kt`

```kotlin
fun Route.pantryRoutes(pantryService: PantryService) {
    route("/api/pantry") {
        route("/items") {
            // ✅ GET /api/pantry/items - Fetch pantry
            get { ... }
            
            // ✅ POST /api/pantry/items - Add pantry item
            post { ... }
            
            // ✅ PUT /api/pantry/items/{id} - Update pantry item
            put("/{id}") { ... }
            
            // ✅ DELETE /api/pantry/items/{id} - Delete pantry item
            delete("/{id}") { ... }
        }
    }
}
```

**These endpoints EXIST but Android NEVER calls them!**

### **Recipe Detail DOES call Backend**:

**File**: `app/src/main/java/com/example/pantrick/ui/screen/RecipeDetailScreen.kt`

```kotlin
LaunchedEffect(recipeId, jwtToken) {
    if (jwtToken.isNotBlank()) {
        cookingViewModel.checkReadiness(recipeId, jwtToken)  // ← Calls backend!
    }
}
```

**File**: `app/src/main/java/com/example/pantrick/data/repository/CookingSessionRepository.kt`

```kotlin
suspend fun checkReadiness(recipeId: String, token: String): Result<CookingReadinessDto> {
    return try {
        val resp = client.get("$base/api/recipes/$recipeId/cook/readiness") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val dto = resp.body<CookingReadinessDto>()
        Result.success(dto)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

---

## 🎯 WHY THE BUG OCCURS

### **Scenario**:
1. User login dengan JWT token → userId = 1 (from backend)
2. User adds "chicken" (1 buah) in Android Pantry
3. Android saves to `SharedPreferences` with key `pantry_user@example.com`
4. **NO backend API call!**
5. Backend pantry for userId=1 is STILL EMPTY or has seed data only
6. User opens "Fully Salted Roast Chicken" recipe
7. Android calls `GET /api/recipes/2599/cook/readiness` with JWT
8. Backend extracts userId=1 from JWT
9. Backend calls `pantryRepository.getItemsByUserId(1)`
10. Backend pantry returns EMPTY or seed data (NO "chicken")
11. Backend checks if "chicken" exists in pantry → NOT FOUND
12. Backend returns `canCook=false`, ingredient status = "Belum tersedia"
13. Android UI shows chicken as NOT AVAILABLE ❌

---

## 🚨 ADDITIONAL ROOT CAUSES

### **ROOT CAUSE #2: Saved Recipes Also LOCAL ONLY**

**File**: `app/src/main/java/com/example/pantrick/data/local/PantrickPreferences.kt`

```kotlin
fun getSavedRecipes(email: String?): List<Recipe> {
    if (email.isNullOrBlank()) return emptyList()
    val normalized = User.normalizeEmail(email)
    val rawJson = prefs.getString("$KEY_SAVED_RECIPES_PREFIX$normalized", null) ?: return emptyList()
    return json.decodeFromString<List<Recipe>>(rawJson)  // ← LOCAL ONLY!
}
```

Backend HAS `/api/saved-recipes` API but Android uses local storage!

This explains the image bug:
- Recipe Detail loads recipe from backend → has `imageName`
- User saves recipe → Android saves to SharedPreferences
- Android fetches saved recipes from SharedPreferences (NOT backend)
- If serialization/storage loses `imageName` field → image shows placeholder

---

## 📊 ARCHITECTURE ISSUES FOUND

| Feature | Current Implementation | Issue |
|---------|----------------------|-------|
| **Pantry** | LOCAL (SharedPreferences) | ❌ No backend sync → readiness check fails |
| **Saved Recipes** | LOCAL (SharedPreferences) | ❌ No backend sync → possible image metadata loss |
| **Recommendation** | BACKEND (via HomeViewModel) | ✅ Correct (after previous fixes) |
| **Recipe Detail** | BACKEND | ✅ Correct |
| **Cooking Readiness** | BACKEND | ✅ Correct BUT depends on backend pantry |
| **Cooking Session** | BACKEND | ✅ Correct |

---

## 💡 SOLUTION ARCHITECTURE

### **Option A: Full Backend Sync (RECOMMENDED)**

Make Android Pantry and Saved Recipes use backend APIs as single source of truth.

#### **For Pantry**:
1. Create `PantryApiRepository.kt` in Android
2. Implement methods calling backend Pantry API:
   - `GET /api/pantry/items` → fetch pantry
   - `POST /api/pantry/items` → add item
   - `PUT /api/pantry/items/{id}` → update item
   - `DELETE /api/pantry/items/{id}` → delete item
3. Update `PantryViewModel` to use `PantryApiRepository` instead of local `PantryRepository`
4. Keep local storage as CACHE ONLY for offline support
5. Sync on app start and after each operation

#### **For Saved Recipes**:
1. Create `SavedRecipeApiRepository.kt` in Android
2. Implement methods calling backend Saved Recipe API:
   - `GET /api/saved-recipes` → fetch saved recipes
   - `POST /api/saved-recipes` → save recipe
   - `DELETE /api/saved-recipes/{id}` → unsave recipe
3. Update `RecipeViewModel` to use `SavedRecipeApiRepository`
4. Keep local storage as CACHE ONLY

#### **Benefits**:
- ✅ Single source of truth (backend)
- ✅ Consistent data across devices
- ✅ Cooking readiness works correctly
- ✅ Image metadata preserved
- ✅ User isolation enforced by backend

### **Option B: Hybrid (NOT RECOMMENDED)**

Keep local storage but sync with backend on specific events. This is COMPLEX and ERROR-PRONE.

---

## 🔧 IMPLEMENTATION PLAN

### **Phase 1: Create Android Pantry API Repository** ⚠️ CRITICAL

**File**: `app/src/main/java/com/example/pantrick/data/repository/PantryApiRepository.kt` (NEW)

```kotlin
class PantryApiRepository {
    private val client = PantrickHttpClient.client
    private val base = PantrickApiConfig.BASE_URL

    suspend fun getItems(token: String): Result<List<PantryItemDto>> {
        return try {
            val resp = client.get("$base/api/pantry/items") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }
            val dto = resp.body<PantryListResponse>()
            Result.success(dto.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addItem(token: String, request: AddPantryItemRequest): Result<PantryItemDto> {
        return try {
            val resp = client.post("$base/api/pantry/items") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val dto = resp.body<PantryItemResponse>()
            Result.success(dto.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ... updateItem, deleteItem, etc.
}
```

### **Phase 2: Update PantryViewModel**

**File**: `app/src/main/java/com/example/pantrick/ui/viewmodel/PantryViewModel.kt`

```kotlin
class PantryViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = PantrickPreferences(application)
    private val localRepository = PantryRepository(preferences)  // Keep for cache
    private val apiRepository = PantryApiRepository()  // NEW!

    fun loadItemsForUser(email: String?, jwtToken: String?) {
        if (email.isNullOrBlank() || jwtToken.isNullOrBlank()) {
            _items.value = emptyList()
            return
        }

        _uiState.value = PantryUiState.Loading
        viewModelScope.launch {
            // Try to fetch from backend
            val result = apiRepository.getItems(jwtToken)
            if (result.isSuccess) {
                val items = result.getOrNull() ?: emptyList()
                _items.value = items
                _uiState.value = PantryUiState.Success(items)
                // Cache locally
                localRepository.saveItems(email, items)
            } else {
                // Fallback to local cache
                try {
                    val cached = localRepository.getItems(email)
                    _items.value = cached
                    _uiState.value = PantryUiState.Success(cached)
                } catch (e: Exception) {
                    _uiState.value = PantryUiState.Error("Failed to load pantry")
                }
            }
        }
    }

    fun addItem(email: String?, jwtToken: String?, item: PantryItem) {
        if (email.isNullOrBlank() || jwtToken.isNullOrBlank()) return

        viewModelScope.launch {
            val request = AddPantryItemRequest(
                name = item.name,
                quantity = item.quantity,
                unit = item.unit,
                storageType = item.location.toBackendEnum()
            )
            
            val result = apiRepository.addItem(jwtToken, request)
            if (result.isSuccess) {
                // Refresh from backend
                loadItemsForUser(email, jwtToken)
            } else {
                _uiState.value = PantryUiState.Error("Failed to add item")
            }
        }
    }

    // ... similar for update, delete, etc.
}
```

### **Phase 3: Update Backend Models in Android**

**File**: `app/src/main/java/com/example/pantrick/data/model/BackendModels.kt`

Add DTOs for Pantry API:
```kotlin
@Serializable
data class PantryListResponse(
    val success: Boolean,
    val message: String,
    val total: Int,
    val data: List<PantryItemDto>
)

@Serializable
data class PantryItemDto(
    val id: String,
    val userId: Int,
    val name: String,
    val ingredientName: String,
    val normalizedName: String,
    val quantity: Double,
    val unit: String,
    val storageType: String,  // "FRIDGE", "FREEZER", "PANTRY"
    val purchasedAt: String?,
    val expiryDate: String?,
    val isConsumed: Boolean,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class AddPantryItemRequest(
    val name: String,
    val quantity: Double,
    val unit: String,
    val storageType: String,
    val purchasedAt: String? = null,
    val expiryDate: String? = null
)
```

### **Phase 4: Fix Recipe Image Endpoint**

**Backend**: Create `/api/recipes/{recipeId}/image` endpoint

**File**: `backend/src/main/kotlin/com/pantrick/backend/routes/RecipeRoutes.kt`

```kotlin
get("/{id}/image") {
    val recipeId = call.parameters["id"] ?: return@get call.respond(
        HttpStatusCode.BadRequest,
        ErrorResponse(success = false, message = "Recipe ID required")
    )
    
    val recipe = recipeRepository.getRecipeById(recipeId)
    if (recipe == null) {
        return@get call.respond(HttpStatusCode.NotFound)
    }
    
    if (!recipe.hasImage || recipe.imageName.isNullOrBlank()) {
        return@get call.respond(HttpStatusCode.NotFound)
    }
    
    // Try to find image file
    val imageFile = findImageFile(recipe.imageName)
    if (imageFile == null || !imageFile.exists()) {
        return@get call.respond(HttpStatusCode.NotFound)
    }
    
    // Serve image
    call.respondFile(imageFile)
}
```

**Android**: Update RecipesScreen to use recipeId:

```kotlin
val imageUrl = if (recipe.hasImage) {
    "${PantrickApiConfig.BASE_URL}/api/recipes/${recipe.id}/image"
} else null
```

---

## ✅ EXPECTED BEHAVIOR AFTER FIX

### **Pantry Flow**:
```
User adds "chicken" (1 buah) in Android
    ↓
PantryViewModel.addItem(email, jwtToken, item)
    ↓
PantryApiRepository.addItem(jwtToken, request)
    ↓
POST /api/pantry/items (Authorization: Bearer {JWT})
    ↓
Backend: Extract userId from JWT
    ↓
Backend: PantryService.addItem(userId, request)
    ↓
Backend: Save to pantryRepository
    ↓
Response: HTTP 201, PantryItemDto
    ↓
Android: Refresh pantry list from backend
    ↓
Android: Cache locally for offline
    ↓
---
User opens Recipe Detail
    ↓
cookingViewModel.checkReadiness(recipeId, jwtToken)
    ↓
GET /api/recipes/{recipeId}/cook/readiness (Authorization: Bearer {JWT})
    ↓
Backend: Extract userId from JWT (SAME USER)
    ↓
Backend: pantryRepository.getItemsByUserId(userId)
    ↓
Backend pantry HAS "chicken" ✅
    ↓
Backend: IngredientMatchingService.isIngredientMatch("chicken", "chicken") → TRUE
    ↓
Backend: QuantityComparisonService → sufficient
    ↓
Response: canCook=true, ingredient status="Tersedia" ✅
    ↓
Android UI: Shows "Tersedia" ✅
```

---

## 📋 FILES TO CREATE/MODIFY

### **NEW FILES** (Android):
1. `app/src/main/java/com/example/pantrick/data/repository/PantryApiRepository.kt`
2. `app/src/main/java/com/example/pantrick/data/repository/SavedRecipeApiRepository.kt`

### **MODIFY** (Android):
1. `app/src/main/java/com/example/pantrick/ui/viewmodel/PantryViewModel.kt`
2. `app/src/main/java/com/example/pantrick/ui/viewmodel/RecipeViewModel.kt`
3. `app/src/main/java/com/example/pantrick/data/model/BackendModels.kt`
4. `app/src/main/java/com/example/pantrick/ui/screen/RecipesScreen.kt`

### **MODIFY** (Backend):
1. `backend/src/main/kotlin/com/pantrick/backend/routes/RecipeRoutes.kt` (add image endpoint)

---

## 🎯 SUCCESS CRITERIA

After implementation:
- ✅ Add "chicken" in Android → saved to BACKEND
- ✅ Recipe Detail calls readiness → backend finds "chicken"
- ✅ UI shows "Tersedia" for chicken ✅
- ✅ Saved recipes fetched from backend → image metadata preserved
- ✅ Recipe image loaded using recipeId → robust and consistent

---

**Status**: ROOT CAUSE IDENTIFIED  
**Issue**: Android uses LOCAL storage, Backend uses BACKEND storage → DATA MISMATCH  
**Solution**: Sync Android with Backend APIs  
**Next**: Implement PantryApiRepository and update ViewModels

