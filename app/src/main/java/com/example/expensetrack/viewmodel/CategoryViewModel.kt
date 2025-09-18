package com.example.expensetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.expensetrack.data.entity.Category
import com.example.expensetrack.data.repository.AppRepository

data class CategoryUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val editing: Category? = null
)

class CategoryViewModel(private val repo: AppRepository): ViewModel() {
    val all: StateFlow<List<Category>> =
        repo.getAllCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _ui = MutableStateFlow(CategoryUiState())
    val ui: StateFlow<CategoryUiState> = _ui.asStateFlow()

    fun save(name: String, type: String, parentId: Long?) {
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true) }
            try {
                if (_ui.value.editing == null) {
                    repo.insertCategory(Category(name = name, type = type, parentId = parentId))
                } else {
                    val e = _ui.value.editing!!.copy(name = name, type = type, parentId = parentId)
                    repo.updateCategory(e)
                }
                _ui.value = CategoryUiState()
            } catch (e: Exception) {
                _ui.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }
    fun edit(c: Category) { _ui.update { it.copy(editing = c) } }
    fun cancel() { _ui.value = CategoryUiState() }
    fun delete(c: Category) { viewModelScope.launch { repo.deleteCategory(c) } }
}
