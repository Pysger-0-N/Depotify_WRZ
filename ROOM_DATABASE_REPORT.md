# Laporan Teknis: Implementasi dan Cara Kerja Room Database pada Aplikasi Depotify_WRZ

## 1. Pendahuluan
Room Database adalah pustaka (library) pemetaan objek relasional (ORM) bagian dari Android Jetpack yang menyediakan lapisan abstraksi di atas SQLite untuk memungkinkan akses database yang lancar dan tangguh sekaligus memanfaatkan kekuatan penuh SQLite. 

Dalam proyek **Depotify_WRZ**, Room Database diimplementasikan untuk mendukung fitur **Offline-First / Local CRUD**, yang memungkinkan pengguna untuk melakukan pengelolaan data (Create, Read, Update, Delete) secara mandiri di penyimpanan lokal perangkat tanpa harus bergantung pada koneksi server atau database online.

---

## 2. Arsitektur dan Komponen Utama Room Database

Arsitektur Room dalam aplikasi ini dibagi menjadi 3 komponen inti:
1. **Entity (Model Data / Tabel)**: Merepresentasikan tabel di dalam database SQLite lokal.
2. **DAO (Data Access Object)**: Menyediakan metode-metode untuk mengakses, menambah, mengubah, dan menghapus data.
3. **Database (`AppDatabase`)**: Kelas abstrak utama yang mengikat seluruh Entity dan DAO serta menginisialisasi database SQLite.
4. **Dependency Injection (`DatabaseModule`)**: Mengelola pembuatan instance database secara Singleton menggunakan Hilt.

---

### A. Entity (Model Tabel Lokal)
Setiap Entity dianotasikan dengan `@Entity(tableName = "nama_tabel")` untuk mendefinisikan struktur tabel SQLite. Proyek Depotify_WRZ memiliki 4 entitas utama:

1. **`ProductEntity`** (Tabel `products`):
   - Menyimpan data produk depot (gas, galon, dll).
   - Kolom: `id` (Primary Key, AutoGenerate), `name`, `barcode`, `category`, `stock`, `price`.
2. **`CategoryEntity`** (Tabel `categories`):
   - Menyimpan kategori produk.
   - Kolom: `id`, `code`, `name`, `description`.
3. **`VendorEntity`** (Tabel `vendor`):
   - Menyimpan data supplier/vendor.
   - Kolom: `id`, `name`, `phone`, `address`, `isActive`.
4. **`StockMovementEntity`** (Tabel `stock_movements`):
   - Menyimpan histori pergerakan stok (masuk, keluar, penyesuaian).
   - Kolom: `id`, `productId`, `stockType`, `movementType`, `qty`, `endingBalance`, `description`, `createdAt`.

Contoh kode (`ProductEntity.kt`):
```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val barcode: String,
    val category: String,
    val stock: Int,
    val price: Long
)
```

---

### B. DAO (Data Access Object)
DAO adalah antarmuka (`interface`) yang berisi fungsi-fungsi SQL query. Room secara otomatis menghasilkan implementasi dari DAO ini saat proses *compile-time* (sehingga aman dari kesalahan ketik SQL di *runtime*).

Contoh implementasi (`ProductDao.kt`):
```kotlin
@Dao
interface ProductDao {
    // Membaca seluruh data secara reaktif menggunakan Kotlin Flow
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    // Mencari produk berdasarkan ID
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): ProductEntity?

    // Menambah atau mengganti data (Upsert)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    // Memperbarui data produk
    @Update
    suspend fun updateProduct(product: ProductEntity)

    // Menghapus data produk
    @Delete
    suspend fun deleteProduct(product: ProductEntity)

    // Menghapus berdasarkan ID
    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Int)
}
```

---

### C. Database (`AppDatabase`)
Kelas abstrak turunan `RoomDatabase` yang mendeklarasikan seluruh entitas dan menyediakan fungsi pengakses DAO. Anotasi `@Database` mendaftarkan daftar entitas dan versi database.

```kotlin
@Database(
    entities = [
        ProductEntity::class,
        CategoryEntity::class,
        VendorEntity::class,
        StockMovementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun vendorDao(): VendorDao
    abstract fun stockMovementDao(): StockMovementDao
}
```

---

### D. Dependency Injection (`DatabaseModule`)
Penyediaan instance Room Database menggunakan **Dagger Hilt** dengan skema `@Singleton` memastikan hanya ada satu instance koneksi database yang aktif selama aplikasi berjalan, mencegah kebocoran memori (*memory leaks*).

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideProductDao(database: AppDatabase): ProductDao = database.productDao()

    // Provider DAO lainnya...
}
```

---

## 3. Alur Kerja Operasi CRUD Lokal (Offline-First)

1. **Create (Menambah Data)**:
   - Pengguna mengisi form di UI (Jetpack Compose).
   - ViewModel memanggil fungsi suspend DAO (`insertProduct`) di dalam coroutine scope (`viewModelScope.launch`).
   - SQLite menulis data baru ke file `app_database.db` di penyimpanan lokal perangkat.
2. **Read (Membaca Data secara Reaktif)**:
   - DAO mengembalikan data menggunakan Kotlin `Flow<List<Entity>>`.
   - Ketika ada perubahan data di database, `Flow` secara otomatis memancarkan (*emit*) data terbaru ke ViewModel dan UI tanpa perlu melakukan *refresh* manual.
3. **Update (Mengubah Data)**:
   - Menggunakan anotasi `@Update` atau `@Insert(onConflict = REPLACE)`.
   - Data lama berdasarkan Primary Key (`id`) akan diperbarui dengan nilai baru.
4. **Delete (Menghapus Data)**:
   - Menggunakan anotasi `@Delete` atau query `@Query("DELETE FROM ... WHERE id = :id")`.
   - Baris data dengan ID terkait dihapus dari tabel lokal.

---

## 4. Keunggulan Implementasi
- **Sepenuhnya Offline (Tanpa Server)**: Aplikasi dapat diuji dan digunakan secara penuh tanpa koneksi internet atau server backend.
- **Compile-Time Safety**: Query SQL diperiksa oleh kompiler Android Studio saat proses *build*, sehingga meminimalisir *crash* akibat salah penulisan kolom.
- **Reaktivitas Tinggi**: Integrasi antara Room `Flow` dan Jetpack Compose `StateFlow` menciptakan antarmuka yang responsif secara *real-time*.
- **Performa Tinggi**: Operasi tulis/baca dijalankan di latar belakang (*background thread*) menggunakan Kotlin Coroutines, menjaga UI tetap halus dan bebas lag (*jank-free*).
