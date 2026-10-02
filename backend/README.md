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
