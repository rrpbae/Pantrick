# Pantrick

Pantrick adalah aplikasi Android untuk membantu pengguna mengelola bahan makanan di pantry serta menemukan rekomendasi resep berdasarkan bahan yang tersedia.

## Teknologi

- Android: Kotlin + Android Studio
- Backend: Kotlin + Ktor 3.0.3
- Authentication: JWT + BCrypt
- Recipe Dataset: Food Ingredients and Recipe Dataset with Image Name Mapping
- Database: In-memory repository
- Backend Port: `8081`

> Catatan: backend menggunakan in-memory repository. Data pengguna/pantry dapat kembali kosong ketika backend dihentikan atau dijalankan ulang.

## Struktur Project

```text
Pantrick/
├── app/
├── backend/
├── Dataset_Resep/
│   ├── Food Ingredients and Recipe Dataset with Image Name Mapping.csv
│   └── Food Images/
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

Dataset diperlukan oleh backend untuk memuat resep, bahan, instruksi, dan gambar.

# Cara Menjalankan

## 1. Menjalankan Backend

Buka PowerShell pada **root project `Pantrick`**, bukan folder `backend`.

```powershell
cd "D:\Perkuliahan_Semester_5\Pemrograman Mobile\Pantrick"
.\gradlew.bat :backend:run
```

Backend berjalan pada port:

```text
8081
```

` :backend:run` memang tidak kembali ke prompt selama server masih berjalan. Jika terlihat:

```text
83% EXECUTING
> :backend:run
```

itu berarti Gradle sedang menjalankan server, bukan berhenti pada 83%.

**Jangan menekan `Ctrl+C` selama backend masih diperlukan.**

## 2. Menghubungkan HP Fisik

Buka terminal kedua.

Cek perangkat:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

Pastikan HP muncul dengan status `device`.

Kemudian:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse tcp:8081 tcp:8081
```

Jika berhasil, biasanya output:

```text
8081
```

Dengan ADB Reverse, koneksi menjadi:

```text
HP 127.0.0.1:8081
        ↓
ADB Reverse
        ↓
Laptop 127.0.0.1:8081
        ↓
Ktor Backend
```

Android menggunakan:

```text
http://127.0.0.1:8081
```

sebagai alamat backend untuk setup HP fisik ini.

## 3. Menjalankan Android

Setelah backend dan ADB Reverse aktif:

1. Buka project Pantrick di Android Studio.
2. Hubungkan HP.
3. Pilih HP sebagai target device.
4. Tekan **Run ▶**.

Tidak perlu menjalankan `installDebug` secara manual jika aplikasi dijalankan melalui tombol **Run ▶** Android Studio.

## Urutan Development

### Terminal 1

```powershell
cd "D:\Perkuliahan_Semester_5\Pemrograman Mobile\Pantrick"
.\gradlew.bat :backend:run
```

Biarkan tetap berjalan.

### Terminal 2

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse tcp:8081 tcp:8081
```

### Android Studio

```text
Run ▶
```

## Jika Port 8081 Sudah Digunakan

Jika muncul:

```text
Address already in use
```

jangan menjalankan backend kedua. Periksa:

```powershell
netstat -ano | findstr :8081
```

Jika terdapat `LISTENING`, kemungkinan backend yang lama masih berjalan.

## Memeriksa ADB Reverse

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse --list
```

Jika mapping belum ada:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse tcp:8081 tcp:8081
```

Mapping dapat perlu dibuat kembali setelah HP terputus atau koneksi ADB berubah.

## Recipe Dataset

Backend memuat dataset ketika startup. Dataset menyediakan:

- Nama resep
- Bahan-bahan
- Instruksi memasak
- Nama file gambar

Data rekomendasi resep berasal dari dataset tersebut. Jangan menghapus atau memindahkan folder dataset.

## Rekomendasi Resep

Recommendation menggunakan bahan pantry user.

Contoh:

```text
Pantry:
- chicken
- onion

Recipe:
- chicken
- onion
- milk
- garlic
- cheese
```

Hasil:

```text
2/5 bahan tersedia
```

Pada detail resep, bahan dapat ditampilkan:

```text
✓ Chicken      Tersedia
✓ Onion        Tersedia
✕ Milk         Belum tersedia
✕ Garlic       Belum tersedia
✕ Cheese       Belum tersedia
```

Logic ingredient matching recommendation dan cooking readiness dibuat konsisten.

## Fitur Memasak

Tombol memasak hanya aktif jika seluruh bahan yang diperlukan tersedia.

```text
5/5 bahan tersedia → dapat memasak
2/5 bahan tersedia → tidak dapat memasak
```

## Notifikasi Kedaluwarsa

Fitur notifikasi digunakan untuk memberikan peringatan ketika bahan pantry mendekati tanggal kedaluwarsa sesuai logic aplikasi.

## Troubleshooting

### `./gradlew` tidak dikenali

Gunakan PowerShell Windows:

```powershell
.\gradlew.bat :backend:run
```

Jalankan dari root project, bukan dari folder `backend`.

### `adb` tidak dikenali

Gunakan path langsung:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

### HP tidak dapat terhubung ke backend

Pastikan:

1. Backend Ktor masih berjalan.
2. HP terdeteksi oleh ADB.
3. ADB Reverse sudah dijalankan.
4. Base URL Android menggunakan `http://127.0.0.1:8081`.

## Ringkasan

```text
1. Terminal 1
   .\gradlew.bat :backend:run

2. Terminal 2
   adb devices
   adb reverse tcp:8081 tcp:8081

3. Android Studio
   Run ▶ ke HP

4. Backend tetap berjalan selama aplikasi membutuhkan API.
```
