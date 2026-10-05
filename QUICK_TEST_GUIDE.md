# 🚀 PANDUAN TEST CEPAT - NOTIFIKASI KEDALUWARSA

## ✅ BUILD STATUS: BERHASIL
```
BUILD SUCCESSFUL in 15s
```

---

## 📱 LANGKAH TEST PALING SINGKAT

### **Test 1: Notifikasi Muncul**

1. Buka aplikasi Pantrick
2. Login
3. Masuk ke Pantry
4. Tambah bahan baru:
   - **Name:** `Chicken`
   - **Quantity:** `1 buah`
   - **Location:** Kulkas
   - **Expiry:** **BESOK** (pilih tanggal besok)
5. **Simpan**

**✅ EXPECTED:**
- Chicken tampil di list
- **Android notification muncul di notification shade**
- Title: "Bahan akan kedaluwarsa besok"
- Body: "Chicken akan kedaluwarsa besok. Segera gunakan sebelum terbuang."

---

### **Test 2: Tidak Duplikat**

1. Tutup aplikasi
2. Buka lagi aplikasi
3. Masuk ke Pantry

**✅ EXPECTED:**
- Chicken masih di list
- **Notification TIDAK muncul lagi** (anti-duplikasi berfungsi)

---

## 📂 FILES YANG DIUBAH

1. **NEW:** `app/src/main/java/com/example/pantrick/util/ExpirationNotificationManager.kt`
2. **NEW:** `app/src/main/res/drawable/ic_notification.xml`
3. **MODIFIED:** `app/src/main/java/com/example/pantrick/ui/viewmodel/PantryViewModel.kt`
4. **MODIFIED:** `app/src/main/AndroidManifest.xml` (tambah permission)

---

## 🔧 CARA KERJA

- **Data Source:** LOCAL SharedPreferences (BUKAN backend)
- **Trigger:** Saat PantryScreen dibuka, setelah add/edit bahan
- **Rule:** Jika `expiryEpochDay == BESOK` → kirim notification
- **Anti-Duplikat:** Track di SharedPreferences key `expiration_notified_<id>_<date>_<email>`

---

## 📊 LOG DEBUGGING

```bash
adb logcat | findstr ExpirationNotifManager
```

**Expected logs:**
```
✅ Notification channel 'Kedaluwarsa Bahan' berhasil dibuat
✅ Mengecek X bahan untuk user: email@example.com
✅ Ditemukan X bahan yang akan kedaluwarsa besok
✅ System notification ditampilkan untuk 'Chicken' (ID: xxxxx)
```

---

## ⚠️ TROUBLESHOOTING

**Notification tidak muncul?**
1. Cek Settings > Apps > Pantrick > Notifications (pastikan enabled)
2. Android 13+: Beri permission POST_NOTIFICATIONS saat diminta
3. Cek logcat untuk error

**Notification duplikat?**
1. Clear app data untuk reset tracking
2. Cek log: pastikan key tracking tersimpan

---

## ✅ HASIL AKHIR

**Status:** SELESAI & SIAP PRODUCTION  
**Approach:** LOCAL-ONLY, CEPAT, STABIL  
**Build:** SUCCESS  

**Detail lengkap:** Lihat `EXPIRATION_NOTIFICATION_IMPLEMENTATION.md`
