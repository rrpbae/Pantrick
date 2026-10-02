# Dokumentasi API Login Pantrick (Backend Ktor)

Dokumentasi ini menjelaskan penggunaan endpoint autentikasi **Login** untuk aplikasi **Pantrick**.

---

## **Endpoint**

### **POST /api/login**

Mengautentikasi pengguna berdasarkan email dan password yang terdaftar, serta mengembalikan token JWT untuk akses API berikutnya.

---

## **Request**

- **URL:** `/api/login`
- **Method:** `POST`
- **Headers:**
  - `Content-Type: application/json`

### **Body Format (JSON):**

```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

### **Parameter Validation:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `email` | String | Ya | Alamat email terdaftar pengguna (Format valid). |
| `password` | String | Ya | Password pengguna. |

---

## **Response**

### **1. HTTP 200 OK (Login Berhasil)**

Dikembalikan jika email dan password cocok dengan data pengguna.

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

> **Catatan Keamanan:** Data password / hash password **tidak pernah** dikembalikan di dalam response.

---

### **2. HTTP 400 Bad Request (Validasi Gagal)**

Dikembalikan jika input tidak diisi atau format email tidak valid.

#### **Contoh Email / Password Kosong:**
```json
{
  "success": false,
  "message": "Validasi gagal",
  "errors": {
    "email": "Email wajib diisi",
    "password": "Password wajib diisi"
  }
}
```

#### **Contoh Format Email Tidak Valid:**
```json
{
  "success": false,
  "message": "Validasi gagal",
  "errors": {
    "email": "Format email tidak valid"
  }
}
```

---

### **3. HTTP 401 Unauthorized (Kredensial Salah)**

Dikembalikan jika email tidak ditemukan di database atau password salah.

```json
{
  "success": false,
  "message": "Email atau password salah"
}
```

---

### **4. HTTP 500 Internal Server Error (Error Server)**

Dikembalikan jika terjadi kesalahan tidak terduga pada server.

```json
{
  "success": false,
  "message": "Terjadi kesalahan internal pada server"
}
```

---

## **Default Seed Users (Untuk Testing Login Backend)**

| Email | Password | Role |
|-------|----------|------|
| `user@example.com` | `password123` | Default User |
| `wahid@pantrick.com` | `secret123` | Test User |
