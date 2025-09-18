package com.example.expensetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.expensetrack.data.entity.Account
import com.example.expensetrack.data.entity.Transaction
import com.example.expensetrack.data.entity.TransactionType
import com.example.expensetrack.data.repository.AppRepository
import com.example.expensetrack.export.ExportService
import java.math.BigDecimal
import java.time.LocalDateTime

data class TransactionUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showAddDialog: Boolean = false,
    val editingTransaction: Transaction? = null,
    val selectedTransactions: Set<Long> = emptySet(),
    val exportSuccess: Boolean = false
)

class TransactionViewModel(
    private val repository: AppRepository,
    private val exportService: ExportService
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    private val _selectedAccountId = MutableStateFlow<Long?>(null)
    val selectedAccountId: StateFlow<Long?> = _selectedAccountId.asStateFlow()

    init {
        // Load accounts so the AccountPicker has data
        viewModelScope.launch {
            repository.getAllAccounts().collect { list ->
                _accounts.value = list
            }
        }
        // Load transactions (all by default)
        viewModelScope.launch {
            repository.getAllTransactions().collect { list ->
                _transactions.value = list
            }
        }
    }

    fun selectAccount(accountId: Long?) {
        _selectedAccountId.value = accountId
        viewModelScope.launch {
            if (accountId != null) {
                repository.getTransactionsByAccount(accountId).collect { list ->
                    _transactions.value = list
                }
            } else {
                repository.getAllTransactions().collect { list ->
                    _transactions.value = list
                }
            }
        }
    }

    fun showAddTransactionDialog() {
        _uiState.update { it.copy(showAddDialog = true, editingTransaction = null) }
    }

    fun hideAddTransactionDialog() {
        _uiState.update { it.copy(showAddDialog = false, editingTransaction = null) }
    }

    fun editTransaction(transaction: Transaction) {
        _uiState.update { it.copy(showAddDialog = true, editingTransaction = transaction) }
    }

    fun saveTransaction(
        accountId: Long,
        amount: BigDecimal,
        payee: String,
        category: String,
        memo: String,
        date: LocalDateTime,
        type: TransactionType,
        checkNumber: String,
        categoryId: Long?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val finalAmount = if (type == TransactionType.EXPENSE) -amount else amount

                val tx = _uiState.value.editingTransaction?.copy(
                    accountId = accountId,
                    amount = finalAmount,
                    payee = payee,
                    category = category,
                    memo = memo,
                    date = date,
                    type = type,
                    checkNumber = checkNumber,
                    categoryId = categoryId
                ) ?: Transaction(
                    accountId = accountId,
                    amount = finalAmount,
                    payee = payee,
                    category = category,
                    memo = memo,
                    date = date,
                    type = type,
                    checkNumber = checkNumber,
                    categoryId = categoryId
                )

                if (_uiState.value.editingTransaction != null) {
                    repository.updateTransaction(tx)
                } else {
                    repository.insertTransaction(tx)
                }

                hideAddTransactionDialog()
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun exportQif(accountIds: List<Long>) {
        viewModelScope.launch {
            try {
                exportService.exportToQif(accountIds)
                markExportSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    fun toggleTransactionSelection(id: Long) {
        _uiState.update { state ->
            val next = if (state.selectedTransactions.contains(id))
                state.selectedTransactions - id
            else
                state.selectedTransactions + id
            state.copy(selectedTransactions = next)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedTransactions = emptySet()) }
    }

    fun deleteSelectedTransactions() {
        val ids = _uiState.value.selectedTransactions
        if (ids.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                ids.forEach { id -> repository.deleteTransactionById(id) }
                _uiState.update { it.copy(isLoading = false, selectedTransactions = emptySet()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
            }
        }
    }

    fun markExportSuccess() {
        _uiState.update { it.copy(exportSuccess = true) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null, exportSuccess = false) }
    }

    fun createTransfer(fromAccountId: Long, toAccountId: Long, amountAbs: BigDecimal, whenAt: LocalDateTime, memo: String?) {
        viewModelScope.launch {
            try {
                repository.createTransfer(fromAccountId, toAccountId, amountAbs, whenAt, memo)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

}
