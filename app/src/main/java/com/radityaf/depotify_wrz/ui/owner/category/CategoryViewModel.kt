package com.radityaf.depotify_wrz.ui.owner.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.dao.CategoryDao
import com.radityaf.depotify_wrz.data.local.dao.PendingDeleteDao
import com.radityaf.depotify_wrz.data.local.entity.CategoryEntity
import com.radityaf.depotify_wrz.data.local.entity.PendingDeleteEntity
import com.radityaf.depotify_wrz.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val items: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false,
    val selectedCategory: CategoryEntity? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryDao: CategoryDao,
    private val pendingDeleteDao: PendingDeleteDao,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _selectedCategory = MutableStateFlow<CategoryEntity?>(null)

    val uiState: StateFlow<CategoryUiState> = combine(
        categoryDao.getAllCategories(),
        _isLoading,
        _selectedCategory
    ) { items, isLoading, selectedCategory ->
        CategoryUiState(items = items, isLoading = isLoading, selectedCategory = selectedCategory)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoryUiState()
    )

    init {
        refreshFromApi()
    }

    fun refreshFromApi() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = productRepository.getCategories()
            if (result.isSuccess) {
                val categories = result.getOrNull() ?: emptyList()
                categories.forEach { cat ->
                    categoryDao.insertCategory(
                        CategoryEntity(
                            id = cat.id,
                            vendorId = cat.vendor_id,
                            code = cat.code,
                            name = cat.name,
                            description = cat.description,
                            isSynced = true,
                            isDeleted = false
                        )
                    )
                }
            }
            _isLoading.value = false
        }
    }

    fun loadCategory(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedCategory.value = categoryDao.getCategoryById(id)
            _isLoading.value = false
        }
    }

    fun clearSelectedCategory() {
        _selectedCategory.value = null
    }

    fun add(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val code = name.take(3).uppercase() + System.currentTimeMillis().toString().takeLast(4)
            val result = productRepository.createCategory(code, name.trim())
            if (result.isSuccess) {
                val cat = result.getOrNull()
                val entity = CategoryEntity(
                    id = cat?.id ?: 0,
                    vendorId = cat?.vendor_id,
                    code = cat?.code ?: code,
                    name = cat?.name ?: name.trim(),
                    description = cat?.description,
                    isSynced = true
                )
                if (entity.id > 0) {
                    categoryDao.insertCategory(entity)
                } else {
                    categoryDao.insertCategory(CategoryEntity(code = code, name = name.trim(), isSynced = false))
                }
            } else {
                val entity = CategoryEntity(code = code, name = name.trim(), isSynced = false)
                categoryDao.insertCategory(entity)
            }
        }
    }

    fun saveCategory(id: Int?, name: String, code: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val trimmedCode = code.trim().take(10).uppercase()
            val generatedCode = if (trimmedCode.isBlank()) {
                name.take(3).uppercase() + System.currentTimeMillis().toString().takeLast(4)
            } else {
                trimmedCode
            }
            val desc = description.ifBlank { null }

            if (id != null && id > 0) {
                val result = productRepository.updateCategory(id, generatedCode, name.trim(), desc)
                if (result.isSuccess) {
                    val cat = result.getOrNull()
                    val entity = CategoryEntity(
                        id = id,
                        vendorId = cat?.vendor_id,
                        code = cat?.code ?: generatedCode,
                        name = cat?.name ?: name.trim(),
                        description = cat?.description ?: desc,
                        isSynced = true
                    )
                    categoryDao.updateCategory(entity)
                } else {
                    val entity = CategoryEntity(
                        id = id,
                        code = generatedCode,
                        name = name.trim(),
                        description = desc,
                        isSynced = false
                    )
                    categoryDao.updateCategory(entity)
                }
            } else {
                val result = productRepository.createCategory(generatedCode, name.trim(), desc)
                if (result.isSuccess) {
                    val cat = result.getOrNull()
                    val entity = CategoryEntity(
                        id = cat?.id ?: 0,
                        vendorId = cat?.vendor_id,
                        code = cat?.code ?: generatedCode,
                        name = cat?.name ?: name.trim(),
                        description = cat?.description ?: desc,
                        isSynced = true
                    )
                    if (entity.id > 0) {
                        categoryDao.insertCategory(entity)
                    } else {
                        categoryDao.insertCategory(
                            CategoryEntity(code = generatedCode, name = name.trim(), description = desc, isSynced = false)
                        )
                    }
                } else {
                    val entity = CategoryEntity(
                        id = 0,
                        code = generatedCode,
                        name = name.trim(),
                        description = desc,
                        isSynced = false
                    )
                    categoryDao.insertCategory(entity)
                }
            }
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            val res = try {
                productRepository.deleteCategory(id)
            } catch (e: Exception) {
                Result.failure(e)
            }
            if (res.isFailure) {
                if (id > 0) {
                    pendingDeleteDao.insertPendingDelete(
                        PendingDeleteEntity(entityType = "category", serverId = id)
                    )
                }
            }
            categoryDao.deleteCategoryById(id)
        }
    }
}
