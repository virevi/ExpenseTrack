package com.example.expensetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.expensetrack.data.repository.AppRepository

class AccountViewModelFactory(
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            return AccountViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
