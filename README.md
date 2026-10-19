# Aplikasi Android "Inventaris" — Klien REST API Laravel (Warung ZaRa)

Aplikasi Android klien untuk sistem manajemen inventaris dan akuntansi Warung ZaRa.

## Tech Stack
- **Kotlin**, **Jetpack Compose**, **Material 3**
- **Architecture**: MVVM (ViewModel + StateFlow), **Hilt** (Dependency Injection), **Navigation Compose**
- **Networking**: Retrofit + OkHttp (Logging Interceptor hanya aktif di build debug), `kotlinx.serialization`
- **Storage**: Jetpack DataStore Preferences (penyimpanan JWT token aman)
- **Hardware Integration**: CameraX + Google ML Kit Barcode Scanning untuk scan barcode/QR produk

## Base URL & Environment
- **Debug**: `http://10.0.2.2:8000/api/` (Android Emulator mengakses Laravel lokal di `localhost:8000`). Jika menggunakan perangkat fisik, ganti `BASE_URL` di `app/build.gradle.kts` ke IP laptop pada jaringan WiFi yang sama (misal `http://192.168.1.10:8000/api/`).
- **Release**: `https://GANTI-DENGAN-DOMAIN-ASLI/api/`

## Network Security
Cleartext HTTP traffic (`http://`) diizinkan khusus untuk build debug melalui `network_security_config.xml`. Build release mewajibkan HTTPS (`https://`).

## Asumsi yang Diambil
1. Autentikasi menggunakan Laravel Sanctum Bearer Token yang disimpan secara aman di DataStore Preferences.
2. Ketika token kadaluarsa atau mendapat respons `401 Unauthorized`, aplikasi otomatis menghapus token tersimpan dan mengarahkan pengguna kembali ke halaman Login.
3. Pencarian produk, filter stok menipis (`?stok_menipis=true`), dan scan barcode langsung berinteraksi secara real-time dengan API backend tanpa penyimpanan offline cache (sesuai spesifikasi bahwa backend adalah sumber kebenaran tunggal untuk validasi stok dan transaksi).
