package com.example.expensetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expensetrack.data.repository.AppRepository
import com.example.expensetrack.export.ExportService

class TransactionViewModelFactory(
    private val repository: AppRepository,
    private val exportService: ExportService
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(repository, exportService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
