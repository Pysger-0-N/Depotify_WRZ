package com.radityaf.depotify_wrz.ui.owner.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radityaf.depotify_wrz.data.local.dao.CategoryDao
import com.radityaf.depotify_wrz.data.local.entity.CategoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val items: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val categoryDao: CategoryDao
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<CategoryUiState> = combine(
        categoryDao.getAllCategories(),
        _isLoading
    ) { items, isLoading ->
        CategoryUiState(items = items, isLoading = isLoading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoryUiState()
    )

    fun add(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val code = name.take(3).uppercase() + System.currentTimeMillis().toString().takeLast(4)
            val entity = CategoryEntity(code = code, name = name.trim())
            categoryDao.insertCategory(entity)
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            categoryDao.deleteCategoryById(id)
        }
    }
}
