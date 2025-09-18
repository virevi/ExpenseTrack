package com.example.expensetrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.expensetrack.data.entity.Account
import com.example.expensetrack.data.entity.AccountType
import com.example.expensetrack.data.repository.AppRepository
import java.math.BigDecimal
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

data class AccountUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showAddDialog: Boolean = false,
    val editingAccount: Account? = null
)

class AccountViewModel(private val repository: AppRepository) : ViewModel() {

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _uiState = MutableStateFlow(AccountUiState())
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                repository.getAllAccounts().collect { accountsList ->
                    _accounts.value = accountsList
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun showAddAccountDialog() {
        _uiState.value = _uiState.value.copy(
            showAddDialog = true,
            editingAccount = null
        )
    }

    fun hideAddAccountDialog() {
        _uiState.value = _uiState.value.copy(
            showAddDialog = false,
            editingAccount = null
        )
    }

    fun editAccount(account: Account) {
        _uiState.value = _uiState.value.copy(
            showAddDialog = true,
            editingAccount = account
        )
    }

    fun saveAccount(name: String, type: AccountType, balance: BigDecimal, notes: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val account = _uiState.value.editingAccount?.copy(
                    name = name,
                    type = type,
                    openingBalance = balance,
                    notes = notes
                ) ?: Account(
                    name = name,
                    type = type,
                    openingBalance = balance,
                    notes = notes,
                    currency = "INR"
                )

                if (_uiState.value.editingAccount != null) {
                    repository.updateAccount(account)
                } else {
                    repository.insertAccount(account)
                }

                hideAddAccountDialog()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            try {
                repository.deleteAccount(account)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    val accountBalances: StateFlow<Map<Long, java.math.BigDecimal>> =
        combine(
            repository.getAllAccounts(),        // Flow<List<Account>>
            repository.getAllTransactions()     // Flow<List<Transaction>>
        ) { accounts, txs ->
            // Build a map { accountId -> openingBalance + sum(amounts) }
            accounts.associate { acc ->
                val delta = txs.asSequence()
                    .filter { it.accountId == acc.id }
                    .fold(java.math.BigDecimal.ZERO) { sum, t -> sum + t.amount }
                acc.id to (acc.openingBalance + delta)
            }
        }
            .stateIn(                                   // Convert to hot StateFlow for the UI
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyMap()
            )
}
