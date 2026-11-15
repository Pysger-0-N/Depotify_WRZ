# Depotify - Aplikasi Inventaris & Kasir Warung ZaRa (Android)

Aplikasi mobile Android native untuk manajemen inventaris, Point of Sale (POS/kasir), pengadaan barang (Purchase Order), laporan keuangan bisnis, dan sinkronisasi data dua arah pada Depot Air Minum & Gas (Warung ZaRa).

Aplikasi ini dibangun menggunakan Kotlin, 100% Jetpack Compose dengan arsitektur MVVM (Model-View-ViewModel), Dagger Hilt untuk Dependency Injection, Room Database untuk penyimpanan lokal (offline-first), serta Retrofit untuk komunikasi dengan REST API server. Seluruh antarmuka dikembangkan dengan tema 3D Claymorphism dan teks tampilan menggunakan **Bahasa Baku yang Ringkas, Jelas, dan Profesional**.

---

## Ringkasan Fitur Utama

### 1. Kasir (Point of Sale / POS)
* **Transaksi Instan di Tempat**: Sistem kasir digital dengan keranjang belanja interaktif dan pencarian cepat produk.
* **Jenis Transaksi Fleksibel**: Mendukung transaksi *Isi Ulang (Tukar Galon/Tabung)*, *Beli Baru (+Deposit)*, dan *Beli Putus (Tanpa Deposit)*.
* **Pencatatan Wadah Kosong**: Otomatis mencatat penerimaan galon atau tabung gas kosong dari pelanggan.
* **Variasi Pembayaran**: Mendukung metode pembayaran Tunai (Cash), Transfer Bank, dan metode lainnya.
* **Quick Cash & Kalkulator Kembalian**: Tombol nominal cepat (Pas, 20rb, 50rb, 100rb) dan kalkulasi otomatis uang kembalian.
* **Struk Penjualan & Ekspor**: Tampilan modal struk transaksi dengan fitur **Bagikan Struk** (via WhatsApp/Teks) dan **Ekspor Struk** ke format CSV/Excel.

### 2. Manajemen Produk & Kategori
* **Master Data Terintegrasi**: Kelola data produk dan kategori yang tersimpan pada Room Database (lokal) dan Server Database.
* **Rincian Harga & Deposit**: Pengaturan harga jual, harga beli/modal, biaya deposit wadah, serta ambang batas stok minimum.
* **Peringatan Kerugian (Warning Banner)**: Peringatan otomatis jika harga jual disetel lebih rendah dari harga beli/modal.
* **Pemisahan Stok Isi & Kosong**: Pelacakan kuantitas stok barang isi (siap jual) dan barang kosong (wadah galon/tabung).
* **Filter & Pencarian**: Filter cepat berdasarkan kategori produk serta pencarian nama, barcode, atau SKU.

### 3. Master Data Vendor / Pemasok
* **Manajemen Pemasok**: Kelola informasi vendor (Nama, No. Telepon, Email Login, dan Alamat).
* **Otomatisasi Akun Vendor**: Pembuatan akun login portal vendor secara terpadu oleh Owner.

### 4. Pengadaan Barang (Purchase Order / PO) & Penerimaan Barang
* **Alur PO Berjenjang**: Pengelolaan alur pengadaan barang dengan status `PENDING`, `DIKONFIRMASI`, `DIKIRIM`, `DITERIMA`, dan `DITOLAK`.
* **PO Vendor Anonim & WhatsApp**: Mendukung pembuatan PO bebas/anonim dan integrasi pengiriman rincian pesanan langsung ke WhatsApp vendor.
* **Penerimaan Barang & Direct Receive**: Fitur konfirmasi penerimaan barang yang secara otomatis menambah stok isi produk dan mencatat log pergerakan stok.

### 5. Pergerakan Stok (Stock Movement)
* **Log Aktivitas Stok**: Pencatatan riwayat transaksi stok Masuk (`IN`), Keluar (`OUT`), dan Penyesuaian (`ADJUSTMENT`).
* **Kategori Stok Isi & Kosong**: Pelacakan pergerakan untuk stok `ISI` maupun stok `KOSONG`.
* **Validasi Stok**: Mencegah pencatatan pergerakan stok keluar jika kuantitas melebihi stok yang tersedia.
* **Filter & Ringkasan**: Filter berdasarkan tipe pergerakan, jenis stok, dan ringkasan total kuantitas masuk/keluar.

### 6. Notifikasi Alert Persediaan
* **Peringatan Stok Menipis**: Sistem notifikasi internal yang menampilkan daftar produk dengan stok di bawah ambang batas minimum.
* **Aksi Cepat Restok**: Tombol akses langsung untuk *Restok Manual* atau *Buat PO Baru* langsung dari item notifikasi.
* **Status Penandaan**: Penandaan notifikasi yang sudah dibaca (*Mark as Read*).

### 7. Fitur Penjualan (Sales) & Laporan Keuangan Bisnis
* **Daftar Transaksi Penjualan**: Pemantauan transaksi dari web dan aplikasi depotify.
* **Konfirmasi Penjualan**: Konfirmasi transaksi penjualan online/tatap muka.
* **Dashboard Laporan Keuangan**: Ringkasan KPI *Total Omset Penjualan*, *Pengeluaran PO*, dan *Estimasi Laba Kotor*.
* **Cetak & Ekspor Laporan**: Fitur ekspor laporan keuangan dan rekap transaksi lengkap ke format file CSV / Excel.

### 8. Hak Akses Berbasis Peran (Multi-Role Access Control)
* **Role Owner / Staf**: Akses penuh ke Dashboard, Kasir POS, Produk, Kategori, Vendor, PO, Pergerakan Stok, Penjualan, Laporan Keuangan, dan Sinkronisasi.
* **Role Vendor**: Portal Vendor khusus untuk mengelola produk pasokan sendiri dan memperbarui status pesanan PO yang dikirim oleh toko.

### 9. Arsitektur Offline-First & Sinkronisasi Dua Arah
* **Operasional Offline-First**: Aplikasi tetap dapat berfungsi penuh tanpa koneksi internet dengan menyimpan data di Room DB lokal.
* **Indikator Jaringan Real-Time**: Status konektivitas Online/Offline terpantau secara langsung di baris navigasi atas.
* **Sinkronisasi Otomatis & Manual**: Auto-sync listener serta layar khusus *Sinkronisasi Dua Arah* untuk menyelaraskan data lokal dengan server database.
* **Initial Sync**: Layar penyelarasan data saat pertama kali masuk ke aplikasi.

### 10. Desain Antarmuka Claymorphism & Bahasa Baku
* **Design System 3D Claymorphism**: Tampilan modern dengan efek dual shadow, inset input terbenam, dan skema warna Depotify.
* **Bahasa Baku & Ringkas**: Seluruh teks antarmuka menggunakan bahasa Indonesia baku, lugas, profesional, dan bebas dari kata kaku buatan AI.

---

## Spesifikasi dan Stack Teknologi

* **Bahasa Pemrograman**: Kotlin 1.9.24
* **UI Toolkit**: Jetpack Compose (Compose BOM) dengan Material 3
* **Target SDK**: Android 14 (API Level 34)
* **Minimum SDK**: Android 8.0 (API Level 26)
* **Arsitektur Aplikasi**: Clean Architecture + MVVM + Single Activity Architecture
* **Dependency Injection**: Google Dagger Hilt 2.51.1
* **Database Lokal**: Room Database 2.6.x (KSP)
* **Network & API**: Retrofit 2, OkHttp 3, Logging Interceptor, KotlinX Serialization Converter
* **Local Preference**: AndroidX DataStore Preferences
* **Pemindai Barcode**: CameraX API + Google ML Kit Barcode Scanning

---

## Struktur Direktori Proyek

```
app/src/main/java/com/radityaf/depotify_wrz/
├── InventarisApp.kt             # Class Application utama (@HiltAndroidApp)
├── MainActivity.kt              # Single Activity utama penyedia alur navigasi
├── data/
│   ├── local/                   # Room Database, DAO (Product, Category, Vendor, StockMovement, PendingDelete), Entities
│   ├── model/                   # Data class model request/response API dan domain model
│   ├── remote/                  # Interface ApiService (Retrofit) dan AuthInterceptor
│   └── repository/              # Repository layer (Auth, Product, Sales, Stock, PO, Vendor, Notification, Sync)
├── di/                          # Dagger Hilt Modules (DatabaseModule, NetworkModule)
├── ui/
│   ├── auth/                    # Layar Login dan AuthViewModel
│   ├── components/              # Custom Compose Claymorphism (clayCard, clayInset, clayButton, DepotifyComponents)
│   ├── main/                    # MainScreen dengan Bottom Navigation dan rute Owner
│   ├── navigation/              # Definisi Sealed Class Screen untuk rute navigasi
│   ├── owner/                   # Layar operasional Owner Depot
│   │   ├── category/            # Layar daftar & form Kategori
│   │   ├── home/                # Layar Beranda / Dashboard statistik
│   │   ├── kasir/               # Layar Kasir POS & Modal Struk Penjualan
│   │   ├── more/                # Layar Menu Utama / Lainnya
│   │   ├── notification/        # Layar Notifikasi Stok Menipis
│   │   ├── po/                  # Layar daftar, form, detail PO & Goods Receipt
│   │   ├── product/             # Layar form & detail Produk Owner
│   │   ├── report/              # Layar Laporan Keuangan & Bisnis
│   │   ├── sales/               # Layar daftar Penjualan & konfirmasi
│   │   ├── sync/                # Layar Sinkronisasi Database
│   │   └── vendor/              # Layar daftar, form, detail Master Vendor
│   ├── product/                 # Layar daftar Produk utama dan ProductViewModel
│   ├── profile/                 # Layar profil pengguna dan pengaturan akun
│   ├── stock/                   # Layar riwayat stok dan pencatatan pergerakan stok manual
│   ├── sync/                    # Layar InitialSyncScreen saat awal login
│   ├── theme/                   # Tema warna, skema warna Material3, dan DepotifyColors
│   ├── unsupported/             # Layar penanganan akun yang tidak didukung
│   └── vendor/                  # Layar dan ViewModel khusus untuk portal Vendor
│       ├── VendorMainScreen.kt  # Root navigasi portal Vendor
│       ├── po/                  # Layar PO khusus Vendor
│       └── product/             # Layar katalog produk khusus Vendor
└── utils/                       # Utility helper (FormatUtils, NetworkObserver)
```

---

## Pengaturan dan Cara Menjalankan Project

### 1. Prasyarat Development
1. Android Studio Jellyfish / Koala / Ladybug atau versi yang lebih baru.
2. JDK 11 atau yang lebih baru.
3. Perangkat Android fisik atau Emulator dengan Minimum API Level 26 (Android 8.0+).

### 2. Konfigurasi Alamat API Server
Buka file `app/build.gradle.kts` dan sesuaikan nilai `BASE_URL` sesuai dengan IP lokal atau domain backend Anda:

```kotlin
buildTypes {
    debug {
        buildConfigField("String", "BASE_URL", "\"http://192.168.100.179:8000/api/\"")
    }
    release {
        buildConfigField("String", "BASE_URL", "\"https://domain-anda.com/api/\"")
        isMinifyEnabled = false
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### 3. Menjalankan Aplikasi via Android Studio
1. Buka proyek ini di Android Studio.
2. Tunggu proses Gradle Sync selesai.
3. Pilih target device (emulator atau perangkat HP via ADB debugging).
4. Klik tombol **Run** pada toolbar atau tekan `Shift + F10`.

### 4. Build APK Manual via Terminal
Untuk membuat APK Debug:
```bash
./gradlew assembleDebug
```
Hasil file APK berada di:
`app/build/outputs/apk/debug/app-debug.apk`

Untuk membuat APK Release:
```bash
./gradlew assembleRelease
```

---

## Lisensi dan Hak Cipta

Hak Cipta (c) 2026 Raditya F. Seluruh hak cipta dilindungi undang-undang. Proyek ini dikembangkan khusus untuk operasional Depotify - Warung ZaRa.
