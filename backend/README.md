# Dokumentasi API Autentikasi Pantrick (Backend Ktor)

Dokumentasi ini menjelaskan penggunaan endpoint autentikasi **Login** dan **Register** untuk aplikasi **Pantrick**.

---

## **1. POST /api/register**

Membuat akun pengguna baru di Pantrick.

### **Request**
- **Method:** `POST`
- **URL:** `/api/register`
- **Headers:** `Content-Type: application/json`

```json
{
  "name": "Budi",
  "email": "budi@example.com",
  "password": "password123",
  "passwordConfirmation": "password123"
}
```

### **Parameter Validation:**
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `name` | String | Ya | Nama lengkap pengguna. |
| `email` | String | Ya | Alamat email unik (Format email valid). |
| `password` | String | Ya | Password akun. |
| `passwordConfirmation` / `password_confirmation` | String | Ya | Harus sama persis dengan `password`. |

### **Response**

#### **HTTP 201 Created (Registrasi Berhasil)**
```json
{
  "success": true,
  "message": "Registrasi berhasil",
  "data": {
    "user": {
      "id": 3,
      "name": "Budi",
      "email": "budi@example.com"
    }
  }
}
```

#### **HTTP 400 Bad Request (Validasi Gagal / Email Terdaftar)**
```json
{
  "success": false,
  "message": "Validasi gagal",
  "errors": {
    "email": "Email sudah terdaftar"
  }
}
```

---

## **2. POST /api/login**

Mengautentikasi pengguna berdasarkan email dan password yang terdaftar.

### **Request**
- **Method:** `POST`
- **URL:** `/api/login`
- **Headers:** `Content-Type: application/json`

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

### **Response**

#### **HTTP 200 OK (Login Berhasil)**
```json
{
  "success": true,
  "message": "Login berhasil",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "name": "Pantrick User",
      "email": "user@example.com"
    }
  }
}
```

#### **HTTP 401 Unauthorized (Kredensial Salah)**
```json
{
  "success": false,
  "message": "Email atau password salah"
}
```

---

## **Default Seed Users**

| Email | Password | Role |
|-------|----------|------|
| `user@example.com` | `password123` | Default User |
| `wahid@pantrick.com` | `secret123` | Test User |

---

# **Dokumentasi Dataset & API Resep (Backend Pantrick)**

## **1. Lokasi & Format Dataset**
- **CSV Dataset**: `Dataset_Resep/Food Ingredients and Recipe Dataset with Image Name Mapping.csv`
- **Folder Gambar**: `Dataset_Resep/Food Images/Food Images/`
- **Format Header CSV**: `,Title,Ingredients,Instructions,Image_Name,Cleaned_Ingredients`
- **Bahan Resep**: Disimpan dalam format string array `['1 cup milk', '2 tbsp butter']`.
- **Gambar**: Disimpan sebagai file `.jpg` yang dipetakan dengan slug `Image_Name`.

---

## **2. Cara Dataset Dimuat**
Dataset dimuat **1x saat startup backend** melalui service `RecipeDatasetLoader` secara otomatis.
Data hasil parsing disimpan ke dalam `InMemoryRecipeRepository` yang melayani semua request API resep.

---

## **3. Keterbatasan Data & Penanganan Data Invalid**
- **In-Memory**: Repository saat ini bersifat in-memory. Data dimuat dari CSV setiap kali backend di-restart.
- **Title Kosong**: Baris tanpa `Title` valid di-skip secara otomatis.
- **Corrupted Image Name (`#NAME?`)**: Baris dengan `Image_Name` bernilai `#NAME?` (akibat format Excel formula) tetap dimuat dengan status `hasImage = false`.
- **Informasi Tambahan**: Data seperti `cookingTimeMinutes`, `servings`, dan `category` diset `null` (optional) karena tidak tersedia di CSV asli.

---

## **4. Statistik Dataset (Startup Summary)**
- **Total Baris CSV**: `13.501`
- **Total Resep Valid**: `13.496`
- **Resep Dengan Gambar**: `13.466`
- **Resep Tanpa Gambar**: `30`
- **Duplikat Judul**: `195`
- **Row Skipped/Invalid**: `5`

---

## **5. Endpoint API Resep**

### **A. GET /api/recipes**
Mengambil daftar resep dengan dukungan pagination.
- **Query Params**: `limit` (default 50, max 200), `offset` (default 0)
- **Response**: `RecipeListResponse`

### **B. GET /api/recipes/{id}**
Mengambil detail resep berdasarkan ID.
- **Response**: `RecipeDetailResponse`

### **C. GET /api/recipes/search?q=...**
Mencari resep berdasarkan judul resep atau nama bahan (*case-insensitive*).
- **Query Params**: `q` (kata kunci), `limit`, `offset`
- **Response**: `RecipeListResponse`

### **D. GET /api/recipes/{id}/ingredients**
Mengambil daftar bahan (`RecipeIngredient`) dari resep tertentu.
- **Response**: `RecipeIngredientsResponse`

---

## **6. Cara Menjalankan Backend**
```bash
.\gradlew.bat :backend:run
```
Backend akan berjalan secara default di `http://localhost:8081`.

