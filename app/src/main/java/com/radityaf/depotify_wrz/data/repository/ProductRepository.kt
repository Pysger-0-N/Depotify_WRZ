package com.radityaf.depotify_wrz.data.repository

import com.radityaf.depotify_wrz.data.model.*
import com.radityaf.depotify_wrz.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getProductsResponse(
        category: String? = null,
        stokMenipis: Boolean? = null,
        lowStock: Int? = null,
        search: String? = null,
        page: Int? = null
    ): Result<ProductListApiResponse> {
        return try {
            val response = apiService.getProducts(category, stokMenipis, lowStock, search, page)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Gagal memuat produk (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProducts(
        category: String? = null,
        stokMenipis: Boolean? = null,
        lowStock: Int? = null,
        search: String? = null
    ): Result<List<Product>> {
        return getProductsResponse(category, stokMenipis, lowStock, search).map { it.data }
    }

    suspend fun getProductDetail(id: Int): Result<Product> {
        return try {
            val response = apiService.getProductDetail(id)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Detail produk tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createProduct(request: CreateProductRequest): Result<Product> {
        return try {
            val response = apiService.createProduct(request)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal membuat produk"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(id: Int, request: CreateProductRequest): Result<Product> {
        return try {
            val response = apiService.updateProduct(id, request)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Gagal memperbarui produk"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(id: Int): Result<Boolean> {
        return try {
            val response = apiService.deleteProduct(id)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Gagal menonaktifkan produk"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<Category>> {
        return try {
            val response = apiService.getCategories()
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Gagal memuat kategori"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCategory(code: String, name: String): Result<Category> {
        return try {
            val response = apiService.createCategory(CreateCategoryRequest(code, name))
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Gagal membuat kategori"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCategory(id: Int, code: String, name: String): Result<Category> {
        return try {
            val response = apiService.updateCategory(id, CreateCategoryRequest(code, name))
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Gagal mengupdate kategori"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(id: Int): Result<Boolean> {
        return try {
            val response = apiService.deleteCategory(id)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Kategori tidak dapat dihapus"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProductByBarcode(code: String): Result<Product> {
        return try {
            val response = apiService.getProductByBarcode(code)
            if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Produk tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
