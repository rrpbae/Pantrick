# 🔍 RUNTIME DEBUG GUIDE - NOTIFICATION BELL

## ✅ BUILD STATUS
```
Backend: BUILD SUCCESSFUL in 4s
Android: BUILD SUCCESSFUL in 57s
```

---

## 🎯 IMPLEMENTASI LOGGING END-TO-END

Saya telah menambahkan **EXTENSIVE LOGGING** di setiap tahap untuk trace runtime flow:

### **1. ANDROID UI (NotificationScreenBackend)**
**File:** `app/src/main/java/com/example/pantrick/ui/screen/NotificationScreenBackend.kt`

**Logs:**
```kotlin
LaunchedEffect(jwtToken) {
    if (jwtToken.isNotBlank()) {
        println("[NOTIF UI] Bell screen opened, loading notifications...")
        viewModel.loadNotifications(jwtToken)
    } else {
        println("[NOTIF UI] Bell screen opened but JWT token is blank!")
    }
}
```

---

### **2. ANDROID VIEWMODEL (NotificationViewModel)**
**File:** `app/src/main/java/com/example/pantrick/ui/viewmodel/NotificationViewModel.kt`

**Logs:**
```kotlin
fun loadNotifications(token: String) {
    Log.d(TAG, "[NOTIF UI] Loading notifications with token...")
    println("[NOTIF UI] Loading notifications...")
    
    // Success:
    println("[NOTIF UI] SUCCESS: ${response.notifications.size} notifications, unread=${response.unreadCount}")
    response.notifications.forEachIndexed { index, notif ->
        println("[NOTIF UI] [$index] ${notif.name}: ${notif.message} (days=${notif.daysRemaining}, priority=${notif.priority})")
    }
    
    // Error:
    println("[NOTIF UI] ERROR: Backend returned success=false, message=${response.message}")
    println("[NOTIF UI] EXCEPTION: ${error.message}")
}
```

---

### **3. ANDROID API REPOSITORY (NotificationRepository)**
**File:** `app/src/main/java/com/example/pantrick/data/repository/NotificationRepository.kt`

**Logs:**
```kotlin
suspend fun getNotifications(token: String): Result<NotificationListResponse> {
    println("[NOTIF API] Sending GET /api/notifications...")
    val resp = client.get("$base/api/notifications") { ... }
    println("[NOTIF API] Response HTTP ${resp.status.value}")
    println("[NOTIF API] Raw response: success=${dto.success}, message=${dto.message}, count=${dto.notifications.size}")
    
    // Exception:
    println("[NOTIF API] EXCEPTION: ${e.message}")
}
```

---

### **4. BACKEND AUTH (HomeRoutes)**
**File:** `backend/src/main/kotlin/com/pantrick/backend/routes/HomeRoutes.kt`

**Logs:**
```kotlin
fun ApplicationCall.getAuthenticatedUserId(): Int? {
    // No header:
    println("[AUTH] No Authorization header found")
    
    // Invalid format:
    println("[AUTH] Authorization header doesn't start with 'Bearer '")
    
    // Blank token:
    println("[AUTH] Token is blank")
    
    // Success:
    println("[AUTH] JWT decoded successfully: userId=$userId")
    
    // Failed:
    println("[AUTH] JWT verification failed: ${e.message}")
}
```

---

### **5. BACKEND NOTIFICATION ROUTE**
**File:** `backend/src/main/kotlin/com/pantrick/backend/routes/NotificationRoutes.kt`

**Logs:**
```kotlin
get {
    println("[NOTIFICATION ROUTE] GET /api/notifications called")
    // After auth:
    println("[NOTIFICATION ROUTE] User authenticated: userId=$userId")
    // Before response:
    println("[NOTIFICATION ROUTE] Sending response: ${response.notifications.size} notifications")
}
```

---

### **6. BACKEND NOTIFICATION SERVICE**
**File:** `backend/src/main/kotlin/com/pantrick/backend/service/NotificationService.kt`

**Logs:**
```kotlin
fun getNotifications(userId: Int, nowDate: LocalDate): NotificationListResponse {
    println("[GET NOTIFICATIONS] userId=$userId, nowDate=$nowDate, itemCount=${userItems.size}")
    
    // For each item:
    println("[EXPIRY] Checking item: id=${item.id}, name=${item.name}, userId=${item.userId}, quantity=${item.quantity}, isConsumed=${item.isConsumed}, expirationDate=${item.expirationDate}")
    
    // Skip reasons:
    println("[EXPIRY] SKIP: item ${item.name} is consumed or quantity=0")
    println("[EXPIRY] SKIP: item ${item.name} has no expirationDate")
    println("[EXPIRY] SKIP: item ${item.name} has invalid expirationDate format: $dateStr")
    println("[EXPIRY] SKIP: item ${item.name} daysRemaining=$daysRemaining doesn't match notification rules")
    
    // Valid item:
    println("[EXPIRY] userId=$userId, ingredient=${item.name}, expirationDate=$dateStr, daysRemaining=$daysRemaining")
    
    // Notification created:
    println("[NOTIFICATION] notificationCreated=true, notificationId=$notificationId, userId=$userId, type=$notifType, message=$message")
    
    // Final result:
    println("[GET NOTIFICATIONS] userId=$userId, count=${sorted.size}, unreadCount=$unreadCount")
}
```

---

### **7. BACKEND PANTRY ROUTE (for sync trace)**
**File:** `backend/src/main/kotlin/com/pantrick/backend/routes/PantryRoutes.kt`

**Logs:**
```kotlin
post {
    println("[PANTRY ROUTE] POST /api/pantry/items called")
    println("[PANTRY ROUTE] User authenticated: userId=$userId")
    println("[PANTRY ROUTE] Request received: name=${request.name}, expirationDate=${request.expirationDate}")
    println("[PANTRY ROUTE] Item created: id=${createdItem.id}, name=${createdItem.name}, expirationDate=${createdItem.expirationDate}")
    
    // Error:
    println("[PANTRY ROUTE] Invalid request body")
    println("[PANTRY ROUTE] Validation error: ${e.message}")
}
```

---

## 🧪 CARA TEST RUNTIME

### **Persiapan:**

1. **Start Backend:**
   ```bash
   .\gradlew.bat :backend:run
   ```
   Backend akan log di console

2. **Install Android APK Terbaru:**
   ```bash
   # APK ada di: app/build/outputs/apk/debug/app-debug.apk
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Monitor Android Logs:**
   ```bash
   adb logcat -c  # Clear logs
   adb logcat | findstr "NOTIF\|SYNC\|EXPIRY\|AUTH"
   ```

---

### **Test Scenario:**

1. **Buka Aplikasi & Login**
   - Login dengan akun (contoh: test@example.com)
   - Pastikan "Remember Me" dicentang

2. **Tambah Bahan Expire BESOK:**
   - Buka Pantry
   - Tap "+" untuk add
   - Input:
     - **Nama:** chicken
     - **Quantity:** 1 buah
     - **Location:** Kulkas
     - **Expiry:** pilih tanggal **BESOK**
   - Tap "Simpan"

3. **Cek Backend Console:**
   **Expected logs:**
   ```
   [PANTRY ROUTE] POST /api/pantry/items called
   [AUTH] JWT decoded successfully: userId=1
   [PANTRY ROUTE] User authenticated: userId=1
   [PANTRY ROUTE] Request received: name=chicken, expirationDate=2026-10-06
   [PANTRY ROUTE] Item created: id=pantry-11, name=chicken, expirationDate=2026-10-06
   ```

4. **Cek Android Logcat:**
   **Expected logs:**
   ```
   [EXPIRY] Syncing item to backend: name=chicken, expirationDate=2026-10-06, daysRemaining=1
   [SYNC] ADD berhasil: chicken
   ```

5. **Klik Ikon Lonceng 🔔:**
   - Tap notification icon di top bar

6. **Cek Android Logcat:**
   **Expected logs:**
   ```
   [NOTIF UI] Bell screen opened, loading notifications...
   [NOTIF UI] Loading notifications...
   [NOTIF API] Sending GET /api/notifications...
   [NOTIF API] Response HTTP 200
   [NOTIF API] Raw response: success=true, message=Berhasil mendapatkan daftar notifikasi, count=1
   [NOTIF UI] SUCCESS: 1 notifications, unread=1
   [NOTIF UI] [0] chicken: Besok chicken akan kedaluwarsa. Ini peringatan terakhir! (days=1, priority=HIGH)
   ```

7. **Cek Backend Console:**
   **Expected logs:**
   ```
   [NOTIFICATION ROUTE] GET /api/notifications called
   [AUTH] JWT decoded successfully: userId=1
   [NOTIFICATION ROUTE] User authenticated: userId=1
   [GET NOTIFICATIONS] userId=1, nowDate=2026-10-05, itemCount=6
   [EXPIRY] Checking item: id=pantry-1, name=Fresh Whole Milk, userId=1, quantity=1.0, isConsumed=false, expirationDate=2026-10-03
   [EXPIRY] userId=1, ingredient=Fresh Whole Milk, expirationDate=2026-10-03, daysRemaining=-2
   [NOTIFICATION] notificationCreated=true, notificationId=notif-pantry-1-EXPIRED, userId=1, type=EXPIRED, message=Fresh Whole Milk sudah kedaluwarsa.
   [EXPIRY] Checking item: id=pantry-11, name=chicken, userId=1, quantity=1.0, isConsumed=false, expirationDate=2026-10-06
   [EXPIRY] userId=1, ingredient=chicken, expirationDate=2026-10-06, daysRemaining=1
   [NOTIFICATION] notificationCreated=true, notificationId=notif-pantry-11-H1, userId=1, type=H1, message=Besok chicken akan kedaluwarsa. Ini peringatan terakhir!
   [GET NOTIFICATIONS] userId=1, count=2, unreadCount=2
   [NOTIFICATION ROUTE] Sending response: 2 notifications
   ```

8. **Cek UI NotificationScreen:**
   **Expected:**
   - ✅ Tampil notification: "Besok chicken akan kedaluwarsa. Ini peringatan terakhir!"
   - ✅ Priority: HIGH (kategori "Peringatan Penting")
   - ✅ Badge "Besok"
   - ✅ Unread count badge di top bar

---

## 📊 TABEL VERIFIKASI RUNTIME

| Tahap | Check Point | Expected Log | Status |
|-------|-------------|--------------|--------|
| **1. Pantry Sync** | User tap Save di Add Pantry | `[PANTRY ROUTE] POST /api/pantry/items called` | ⬜ |
| **2. Auth** | Backend extract userId | `[AUTH] JWT decoded successfully: userId=1` | ⬜ |
| **3. Pantry Created** | Backend simpan item | `[PANTRY ROUTE] Item created: id=..., name=chicken, expirationDate=2026-10-06` | ⬜ |
| **4. Android Sync** | Android log sync success | `[SYNC] ADD berhasil: chicken` | ⬜ |
| **5. Bell Clicked** | User tap ikon lonceng | `[NOTIF UI] Bell screen opened, loading notifications...` | ⬜ |
| **6. API Call** | Android send request | `[NOTIF API] Sending GET /api/notifications...` | ⬜ |
| **7. Backend Received** | Backend terima request | `[NOTIFICATION ROUTE] GET /api/notifications called` | ⬜ |
| **8. Auth** | Backend extract userId | `[AUTH] JWT decoded successfully: userId=1` | ⬜ |
| **9. Expiry Detection** | Backend cek expiry | `[EXPIRY] userId=1, ingredient=chicken, expirationDate=2026-10-06, daysRemaining=1` | ⬜ |
| **10. Notification Created** | Backend buat notif | `[NOTIFICATION] notificationCreated=true, notificationId=notif-pantry-11-H1, userId=1, type=H1` | ⬜ |
| **11. HTTP Response** | Android terima response | `[NOTIF API] Response HTTP 200` | ⬜ |
| **12. Parse Success** | Android parse data | `[NOTIF UI] SUCCESS: 1 notifications, unread=1` | ⬜ |
| **13. UI Render** | NotificationScreen tampil | Lihat notification di layar | ⬜ |

---

## 🚨 TROUBLESHOOTING

### **ISSUE 1: JWT Token Blank**

**Log:**
```
[NOTIF UI] Bell screen opened but JWT token is blank!
```

**Cause:** User login tanpa "Remember Me" atau token expired

**Solution:**
- Logout dan login lagi dengan "Remember Me" ✅
- Cek `PantrickPreferences.getJwtToken()` return value

---

### **ISSUE 2: Pantry Sync Gagal**

**Log:**
```
[SYNC] ADD gagal: Connection refused
```

**Cause:** Backend tidak running atau URL salah

**Solution:**
- Start backend: `.\gradlew.bat :backend:run`
- Cek `PantrickApiConfig.BASE_URL` = `http://10.0.2.2:8080` (emulator) atau `http://YOUR_IP:8080` (device)

---

### **ISSUE 3: Backend Tidak Terima Request**

**Log Backend TIDAK muncul:**
```
[PANTRY ROUTE] POST /api/pantry/items called  # ← TIDAK ADA
```

**Cause:** 
- Backend tidak running
- Android kirim ke URL salah
- Firewall block

**Solution:**
- Cek backend running di port 8080
- Test manual: `curl http://localhost:8080/api/health`
- Cek firewall settings

---

### **ISSUE 4: Auth Failed**

**Log:**
```
[AUTH] JWT verification failed: Token expired
```

**Cause:** JWT token expired

**Solution:**
- Login ulang
- Cek JWT expiration di backend (default 30 days)

---

### **ISSUE 5: Pantry Item SKIP**

**Log:**
```
[EXPIRY] SKIP: item chicken has no expirationDate
```

**Cause:** Backend pantry item tidak punya `expirationDate`

**Solution:**
- Cek request Android: pastikan `expirationDate` field ada
- Cek backend log: `[PANTRY ROUTE] Request received: name=chicken, expirationDate=2026-10-06`
- Jika `expirationDate=null`, cek mapping di PantryViewModel

---

### **ISSUE 6: Days Remaining Tidak Match**

**Log:**
```
[EXPIRY] SKIP: item chicken daysRemaining=10 doesn't match notification rules
```

**Cause:** Bahan expire > 7 hari, tidak memenuhi aturan notification

**Aturan Backend:**
- `daysRemaining == 1` → H-1 (HIGH)
- `daysRemaining == 2-3` → EXPIRING (MEDIUM)
- `daysRemaining == 7` → H-7 (LOW)
- `daysRemaining <= 0` → EXPIRED (HIGH)
- `daysRemaining > 7` → TIDAK MUNCUL

**Solution:**
- Test dengan bahan expire BESOK (1 hari)
- Atau 7 hari lagi untuk H-7

---

### **ISSUE 7: Android Tidak Terima Response**

**Log:**
```
[NOTIF API] Response HTTP 200
[NOTIF UI] EXCEPTION: kotlinx.serialization.SerializationException
```

**Cause:** Response format tidak match dengan Android DTO

**Solution:**
- Cek backend response structure
- Cek Android `NotificationListResponse` dan `NotificationItemDto`
- Pastikan field names match (e.g., `expirationDate` not `expiryDate`)

---

### **ISSUE 8: UI Kosong Meski Success**

**Log:**
```
[NOTIF UI] SUCCESS: 1 notifications, unread=1
[NOTIF UI] [0] chicken: Besok chicken akan kedaluwarsa...
```

**Tapi UI kosong**

**Cause:** NotificationScreenBackend render logic error

**Solution:**
- Cek `when (val state = uiState)` di NotificationScreenBackend
- Pastikan `is NotificationUiState.Success` branch dieksekusi
- Cek filter: `highPriority`, `mediumPriority`, `lowPriority`
- Debug dengan breakpoint di Composable

---

## 📝 FIXED ISSUES FROM PREVIOUS IMPLEMENTATION

### **1. Field Name Mismatch**
- ❌ **BEFORE:** Android kirim `expiryDate`, backend expect `expirationDate`
- ✅ **FIXED:** Android sekarang kirim `expirationDate`

### **2. Backend Not Receiving Pantry Items**
- ❌ **BEFORE:** Android hanya save ke local, tidak sync ke backend
- ✅ **FIXED:** PantryViewModel sekarang sync setiap add/update/delete

### **3. Notification Generation**
- ✅ **ALREADY CORRECT:** Backend NotificationService generate on-the-fly (lazy evaluation)

### **4. API Integration**
- ✅ **ALREADY CORRECT:** NotificationScreenBackend sudah menggunakan backend API

---

## ✅ ACCEPTANCE CRITERIA

**PASS jika SEMUA ini terpenuhi:**

1. ✅ Backend log menunjukkan pantry item tersimpan dengan `expirationDate`
2. ✅ Backend log menunjukkan expiry detection: `daysRemaining=1`
3. ✅ Backend log menunjukkan notification dibuat: `notificationCreated=true`
4. ✅ Android log menunjukkan HTTP 200 response
5. ✅ Android log menunjukkan parse success: `SUCCESS: 1 notifications`
6. ✅ **UI menampilkan notification di NotificationScreen**

**FAIL jika:**
- Salah satu tahap di atas gagal
- UI tetap kosong meski log menunjukkan notification ada

---

## 🎯 NEXT STEPS

1. **Jalankan backend:** `.\gradlew.bat :backend:run`
2. **Install APK terbaru** ke device/emulator
3. **Monitor logs** (backend console + `adb logcat`)
4. **Test scenario** sesuai panduan di atas
5. **Screenshot logs** jika masih gagal untuk analisis lebih lanjut

**JANGAN laporkan "FIXED" tanpa bukti runtime log dan screenshot UI yang menunjukkan notification muncul!**
