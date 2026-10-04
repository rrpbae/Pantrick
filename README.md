# Pantrick

Aplikasi Android untuk mengelola bahan makanan di pantry dan menemukan rekomendasi resep berdasarkan bahan yang tersedia.

## Teknologi

- **Android**: Kotlin + Jetpack Compose
- **Backend**: Kotlin + Ktor 3.0.3
- **Port**: 8081

## Prasyarat

- Android Studio
- JDK 11+
- HP Android dengan USB debugging enabled
- ADB (Android Debug Bridge)

## Setup Project

### 1. Clone Repository

```bash
git clone https://github.com/rrpbae/Pantrick.git
cd Pantrick
```

### 2. Dataset

Dataset **TIDAK** disimpan di repository GitHub karena ukurannya besar. Download dataset secara terpisah:

**Download Link**: [LINK DATASET DI SINI]

Setelah download:

1. Extract file dataset
2. Letakkan folder `Dataset_Resep` di **root project Pantrick**

Struktur folder harus seperti ini:

```
Pantrick/
├── app/
├── backend/
├── Dataset_Resep/              ← folder dataset di sini
│   ├── Food Ingredients and Recipe Dataset with Image Name Mapping.csv
│   └── Food Images/
│       └── Food Images/
│           ├── 0.jpg
│           ├── 1.jpg
│           └── ...
├── gradlew
├── gradlew.bat
└── settings.gradle.kts
```

> ⚠️ **Penting**: Pastikan nama folder dan struktur persis seperti di atas agar backend dapat memuat dataset.

### 3. Menjalankan Backend

Buka terminal di **root project Pantrick**, lalu jalankan:

```powershell
.\gradlew.bat :backend:run
```

Backend akan berjalan di port **8081**. Terminal akan menampilkan:

```
> Task :backend:run
83% EXECUTING
```

Ini **normal** — backend sedang berjalan. **Jangan tutup terminal** selama backend masih dibutuhkan.

### 4. Setup Perangkat Android

Buka terminal **baru** (terminal backend tetap berjalan), lalu:

**Cek perangkat:**

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
```

Pastikan HP muncul dengan status `device`.

**Setup ADB reverse:**

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse tcp:8081 tcp:8081
```

ADB reverse memungkinkan HP mengakses backend di laptop melalui `http://127.0.0.1:8081`.

### 5. Menjalankan Aplikasi Android

1. Buka project Pantrick di **Android Studio**
2. Pastikan backend sudah berjalan
3. Hubungkan HP via USB
4. Pilih HP sebagai target device
5. Klik tombol **Run ▶**

## Ringkasan Urutan Setup

**Terminal 1 (Backend):**

```powershell
.\gradlew.bat :backend:run
```

**Terminal 2 (ADB):**

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" devices
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse tcp:8081 tcp:8081
```

**Android Studio:**

```
Run ▶
```

## Catatan

- Backend menggunakan **in-memory database** — data akan hilang saat backend di-restart
- ADB reverse perlu dijalankan ulang jika HP terputus atau reconnect
- Jika port 8081 sudah digunakan, cek dengan: `netstat -ano | findstr :8081`
