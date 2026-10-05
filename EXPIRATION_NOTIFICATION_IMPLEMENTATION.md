# IMPLEMENTASI NOTIFIKASI KEDALUWARSA - LOCAL STORAGE

## ✅ STATUS: SELESAI & DIKOMPILASI BERHASIL

**Build Status:** `BUILD SUCCESSFUL in 15s`  
**Tanggal Implementasi:** Context Transfer - Task 6  
**Pendekatan:** LOCAL-ONLY (SharedPreferences), TIDAK bergantung pada backend

---

## 📋 RINGKASAN IMPLEMENTASI

Fitur notifikasi kedaluwarsa menggunakan **DATA LOKAL** dari SharedPreferences untuk mengecek bahan yang akan kedaluwarsa besok dan menampilkan **ANDROID SYSTEM NOTIFICATION** di notification shade.

---

## 📂 FILE YANG DIBUAT/DIUBAH

### 1. **FILE BARU: `ExpirationNotificationManager.kt`**
**Path:** `app/src/main/java/com/example/pantrick/util/ExpirationNotificationManager.kt`

**Fungsi Utama:**
- `checkAndNotifyExpiringItems(email: String?)` - Mengecek semua bahan dan mengirim notifikasi
- `createNotificationChannel()` - Membuat channel "Kedaluwarsa Bahan"
- `sendNotificationIfNotAlreadySent()` - Mencegah duplikasi notifikasi
- `sendNotification()` - Membuat dan menampilkan Android system notification

**Logika Pengecekan:**
```kotlin
val today = LocalDate.now()
val tomorrowEpochDay = today.plusDays(1).toEpochDay()

val expiringTomorrow = items.filter { item ->
    item.expiryEpochDay == tomorrowEpochDay
}
```

**Anti-Duplikasi:**
- Menggunakan SharedPreferences key: `expiration_notified_<itemId>_<expiryEpochDay>_<email>`
- Jika key sudah `true`, notifikasi TIDAK dikirim lagi
- Jika expiryDate berubah, key berbeda → notifikasi baru boleh dikirim

**Notification Content:**
- **Title:** "Bahan akan kedaluwarsa besok"
- **Body:** "[nama bahan] akan kedaluwarsa besok. Segera gunakan sebelum terbuang."
- **Example:** "Chicken akan kedaluwarsa besok. Segera gunakan sebelum terbuang."

---

### 2. **FILE BARU: `ic_notification.xml`**
**Path:** `app/src/main/res/drawable/ic_notification.xml`

Icon notifikasi berbentuk bell (lonceng) standar Android.

---

### 3. **MODIFIED: `PantryViewModel.kt`**
**Path:** `app/src/main/java/com/example/pantrick/ui/viewmodel/PantryViewModel.kt`

**Perubahan:**
1. **Import tambahan:**
   ```kotlin
   import com.example.pantrick.util.ExpirationNotificationManager
   ```

2. **Instance baru:**
   ```kotlin
   private val notificationManager = ExpirationNotificationManager(application)
   ```

3. **Trigger di `loadItemsForUser()`:**
   ```kotlin
   // Setelah berhasil load items
   notificationManager.checkAndNotifyExpiringItems(email)
   ```

4. **Trigger di `addItem()`:**
   ```kotlin
   // Setelah berhasil add item
   notificationManager.checkAndNotifyExpiringItems(email)
   ```

5. **Trigger di `updateItem()`:**
   ```kotlin
   // Setelah berhasil update item
   notificationManager.checkAndNotifyExpiringItems(email)
   ```

**Kapan Notifikasi Dicek:**
- ✅ Saat PantryScreen dibuka (`loadItemsForUser` dipanggil)
- ✅ Setelah user menambahkan bahan baru (`addItem`)
- ✅ Setelah user mengedit bahan (`updateItem`)

---

### 4. **MODIFIED: `AndroidManifest.xml`**
**Path:** `app/src/main/AndroidManifest.xml`

**Perubahan:**
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

Permission untuk Android 13+ (API 33+) agar bisa menampilkan notifikasi.

---

## 🔧 CARA KERJA SISTEM

### **Flow Diagram:**

```
User Action (Add/Edit Bahan ATAU Buka Pantry)
           ↓
PantryViewModel method dipanggil
           ↓
notificationManager.checkAndNotifyExpiringItems(email)
           ↓
Load pantry items dari SharedPreferences
           ↓
Filter items dengan expiryEpochDay == BESOK
           ↓
Untuk setiap item yang expire besok:
    ├─ Cek apakah sudah pernah notifikasi (SharedPreferences key)
    ├─ Jika sudah → SKIP
    └─ Jika belum → Kirim notification + tandai sebagai sent
                    ↓
          Android System Notification muncul di shade
```

---

## 🧪 CARA MANUAL TEST

### **Scenario 1: Bahan Expire BESOK**

1. **Buka aplikasi Pantrick**
2. **Login dengan akun test**
3. **Tambah bahan baru:**
   - Name: `Chicken`
   - Quantity: `1`
   - Unit: `buah`
   - Location: `Kulkas`
   - **Expiry Date: BESOK** (pilih tanggal besok)
4. **Simpan bahan**
5. **Expected Result:**
   - ✅ Chicken tampil di Pantry
   - ✅ Status kedaluwarsa: "1 hari lagi" atau "Besok"
   - ✅ **Android notification muncul di notification shade:**
     - Title: "Bahan akan kedaluwarsa besok"
     - Body: "Chicken akan kedaluwarsa besok. Segera gunakan sebelum terbuang."

---

### **Scenario 2: Anti-Duplikasi**

1. **Setelah Scenario 1 berhasil**
2. **Tutup aplikasi → Buka lagi**
3. **Masuk ke PantryScreen**
4. **Expected Result:**
   - ✅ Chicken masih tampil
   - ❌ **Notification TIDAK muncul lagi** (karena sudah pernah dikirim)

**Log di Logcat:**
```
ExpirationNotifManager: Notification untuk 'Chicken' sudah pernah dikirim, skip
```

---

### **Scenario 3: Expiry Date Berubah**

1. **Edit bahan Chicken**
2. **Ubah expiry date ke tanggal lain (contoh: lusa)**
3. **Simpan**
4. **Expected Result:**
   - ❌ Notification TIDAK muncul (karena expire bukan besok)

5. **Edit lagi Chicken**
6. **Ubah expiry date ke BESOK lagi**
7. **Simpan**
8. **Expected Result:**
   - ✅ **Notification muncul lagi** (karena expiryEpochDay berbeda, key berbeda)

---

### **Scenario 4: Bahan Expire > 1 Hari**

1. **Tambah bahan:**
   - Name: `Beef`
   - Expiry: 3 hari lagi
2. **Simpan**
3. **Expected Result:**
   - ✅ Beef tampil di Pantry
   - ❌ **Notification TIDAK muncul** (karena expire masih > 1 hari)

---

### **Scenario 5: Bahan Sudah Kedaluwarsa**

1. **Tambah bahan:**
   - Name: `Milk`
   - Expiry: KEMARIN
2. **Simpan**
3. **Expected Result:**
   - ✅ Milk tampil di Pantry dengan status "Kedaluwarsa"
   - ❌ **Notification TIDAK muncul** (notifikasi hanya untuk H-1, bukan H-0 atau sudah lewat)

---

## 📱 NOTIFIKASI ANDROID

### **Notification Channel:**
- **Channel ID:** `pantrick_expiration_channel`
- **Channel Name:** "Kedaluwarsa Bahan"
- **Importance:** HIGH (dengan vibration dan lights)

### **Notification Properties:**
- **Small Icon:** Bell icon (`ic_notification.xml`)
- **Priority:** HIGH
- **Auto Cancel:** true (dismiss saat diklik)
- **Content Intent:** Membuka HomeActivity saat diklik
- **Style:** BigTextStyle (untuk text panjang)

### **Unique Notification ID:**
```kotlin
"${item.id}_${item.expiryEpochDay}".hashCode()
```
Kombinasi item ID dan expiryEpochDay memastikan setiap notifikasi unik per bahan dan tanggal.

---

## 🛡️ PERMISSION HANDLING

### **Android 13+ (API 33+):**
- Memerlukan permission `POST_NOTIFICATIONS`
- ExpirationNotificationManager otomatis cek permission:
  ```kotlin
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (checkSelfPermission(...) != GRANTED) {
          Log.w(TAG, "Permission tidak diberikan, skip notification")
          return
      }
  }
  ```

### **Android < 13:**
- Tidak perlu runtime permission untuk notifikasi
- Notification langsung ditampilkan

---

## 🚫 YANG TIDAK DILAKUKAN

Sesuai instruksi user, implementasi INI TIDAK:
- ❌ Mengubah dataset
- ❌ Mengubah RecommendationService
- ❌ Mengubah CookingSessionService
- ❌ Mengubah matching ingredient
- ❌ Membuat dummy data/notification
- ❌ Menghapus backend notification existing
- ❌ Melakukan refactor besar-besaran
- ❌ Commit/push ke repository
- ❌ Bergantung pada backend API

---

## 📊 DATA SOURCE

**HANYA menggunakan LOCAL STORAGE:**
- `PantrickPreferences.getPantryItems(email)` → dari SharedPreferences
- Field yang digunakan:
  - `PantryItem.name` - Nama bahan
  - `PantryItem.id` - Unique identifier
  - `PantryItem.expiryEpochDay` - Tanggal kedaluwarsa dalam epoch day

**TIDAK ada API call ke backend.**

---

## 🔍 DEBUGGING

### **Log Tags:**
- `ExpirationNotifManager` - untuk semua aktivitas notification

### **Key Logs:**
```
✅ Notification channel 'Kedaluwarsa Bahan' berhasil dibuat
✅ Mengecek X bahan untuk user: email@example.com
✅ Ditemukan X bahan yang akan kedaluwarsa besok
✅ System notification ditampilkan untuk 'Chicken' (ID: 12345)
⚠️ Notification untuk 'Chicken' sudah pernah dikirim, skip
```

### **Cek di Logcat:**
```bash
adb logcat | findstr ExpirationNotifManager
```

---

## ✅ CHECKLIST IMPLEMENTASI

- [x] `ExpirationNotificationManager.kt` dibuat
- [x] Notification channel dibuat
- [x] Logika pengecekan expire besok
- [x] Anti-duplikasi dengan SharedPreferences tracking
- [x] Android system notification build
- [x] Unique notification ID generation
- [x] `ic_notification.xml` icon dibuat
- [x] `PantryViewModel` di-update dengan trigger
- [x] Trigger di `loadItemsForUser()`
- [x] Trigger di `addItem()`
- [x] Trigger di `updateItem()`
- [x] Permission `POST_NOTIFICATIONS` ditambahkan
- [x] Permission check untuk Android 13+
- [x] Build successful: `.\gradlew.bat :app:compileDebugKotlin`

---

## 🎯 RESULT

**Status:** ✅ **IMPLEMENTASI SELESAI & SIAP TEST**  
**Build:** ✅ `BUILD SUCCESSFUL in 15s`  
**Files Changed:** 4 (1 new util, 1 new icon, 2 modified)  
**Approach:** LOCAL-ONLY, FAST, STABLE  

**Next Step:** Manual testing dengan menambahkan bahan expire BESOK di aplikasi Android.

---

## 📝 CATATAN TAMBAHAN

1. **Notification tidak muncul di Emulator?**
   - Pastikan notification permission diberikan di Settings > Apps > Pantrick > Notifications
   - Untuk Android 13+, aplikasi akan request permission otomatis saat pertama kali

2. **Notification muncul berkali-kali?**
   - Cek log: pastikan key tracking `expiration_notified_*` tersimpan dengan benar
   - Clear app data jika perlu reset tracking

3. **Ingin mereset notifikasi yang sudah terkirim?**
   - Clear app data di Settings > Apps > Pantrick > Storage > Clear data
   - Atau hapus SharedPreferences key manual via code

---

**Implementasi ini fokus pada DEADLINE dan STABILITAS, menggunakan data lokal yang sudah ada tanpa mengubah arsitektur existing.**
