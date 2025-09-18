package com.example.expensetrack.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetrack.data.entity.Account
import com.example.expensetrack.data.entity.AccountType
import com.example.expensetrack.data.entity.Transaction
import com.example.expensetrack.data.entity.TransactionType
import com.example.expensetrack.ui.components.DatePickerDialog
import com.example.expensetrack.ui.components.TransferDialog
import com.example.expensetrack.ui.components.CategoryPicker
import com.example.expensetrack.ui.components.SubCategoryPicker
import com.example.expensetrack.util.CurrencyUtils
import com.example.expensetrack.util.DateUtils
import com.example.expensetrack.viewmodel.AccountViewModel
import com.example.expensetrack.viewmodel.TransactionViewModel
import com.example.expensetrack.viewmodel.CategoryViewModel
import java.math.BigDecimal
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseIntegratedApp(
    accountViewModel: AccountViewModel,
    transactionViewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showTransfer by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ExpenseTrack") },
                actions = {
                    // Transfer button
                    IconButton(onClick = { showTransfer = true }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Transfer")
                    }
                    // Export button
                    IconButton(
                        onClick = {
                            val accountIds = accountViewModel.accounts.value.map { it.id }
                            if (accountIds.isNotEmpty()) {
                                transactionViewModel.exportQif(accountIds)
                            }
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBox, contentDescription = null) },
                    label = { Text("Accounts") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Transactions") },
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        transactionViewModel.selectAccount(null)
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Category, contentDescription = null) },
                    label = { Text("Categories") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        },
        floatingActionButton = { // NEW: FAB owned by outer Scaffold
            if (selectedTab == 2) {
                FloatingActionButton(onClick = {
                    categoryViewModel.cancel()
                    showCategoryDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Category")
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> DatabaseAccountsTab(
                viewModel = accountViewModel,
                modifier = Modifier.padding(innerPadding),
                onNavigateToTransactions = { accountId ->
                    transactionViewModel.selectAccount(accountId)
                    selectedTab = 1
                }
            )
            1 -> DatabaseTransactionsTab(
                viewModel = transactionViewModel,
                categoryViewModel = categoryViewModel,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> CategoryManagerScreen(
                viewModel = categoryViewModel,
                showAddDialog = showCategoryDialog,
                onDismissAddDialog = { showCategoryDialog = false },
                contentPadding = innerPadding
            )
        }
    }

    // Transfer Dialog
    if (showTransfer) {
        val accounts by accountViewModel.accounts.collectAsStateWithLifecycle()
        val uiState by transactionViewModel.uiState.collectAsStateWithLifecycle()
        TransferDialog(
            accounts = accounts,
            isLoading = uiState.isLoading,
            onDismiss = { showTransfer = false },
            onConfirm = { fromId, toId, amount, whenAt, memo ->
                transactionViewModel.createTransfer(fromId, toId, amount, whenAt, memo)
                showTransfer = false
            }
        )
    }
}

@Composable
fun DatabaseAccountsTab(
    viewModel: AccountViewModel,
    modifier: Modifier = Modifier,
    onNavigateToTransactions: (Long) -> Unit
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val balances by viewModel.accountBalances.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Accounts",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            FloatingActionButton(
                onClick = { viewModel.showAddAccountDialog() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Account")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.isLoading && accounts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            if (accounts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Loading accounts...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "If this persists, try restarting the app.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = accounts, key = { it.id }) { account ->
                        val balance = balances[account.id] ?: account.openingBalance
                        DatabaseAccountCard(
                            account = account,
                            balance = balance,
                            onClick = { onNavigateToTransactions(account.id) },
                            onEdit = { viewModel.editAccount(account) },
                            onDelete = { viewModel.deleteAccount(account) }
                        )
                    }
                }
            }
        }
    }

    AddEditAccountDialog(viewModel = viewModel)

    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseAccountCard(
    account: Account,
    balance: BigDecimal,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }

    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = account.type.name.replace("_", " "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Balance: " + CurrencyUtils.formatCurrency(balance, account.currency),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Box {
                IconButton(onClick = { showDropdown = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                }
                DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { showDropdown = false; onEdit() },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { showDropdown = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                    )
                }
            }
        }
    }
}

@Composable
fun DatabaseTransactionsTab(
    viewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedAccountId by viewModel.selectedAccountId.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            FloatingActionButton(
                onClick = { viewModel.showAddTransactionDialog() },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (accounts.isNotEmpty()) {
            DatabaseAccountFilter(
                accounts = accounts,
                selectedAccountId = selectedAccountId,
                onAccountSelected = { viewModel.selectAccount(it) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedAccountId != null)
                        "No transactions for this account"
                    else "No transactions yet. Add your first transaction!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items = transactions, key = { it.id }) { transaction ->
                    val account = accounts.find { it.id == transaction.accountId }
                    DatabaseTransactionCard(
                        transaction = transaction,
                        account = account,
                        onEdit = { viewModel.editTransaction(transaction) },
                        onDelete = { viewModel.deleteTransaction(transaction) }
                    )
                }
            }
        }
    }

    AddEditTransactionDialog(
        viewModel = viewModel,
        categoryViewModel = categoryViewModel
    )

    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) { viewModel.clearError() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseAccountFilter(
    accounts: List<Account>,
    selectedAccountId: Long?,
    onAccountSelected: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedAccount = accounts.find { it.id == selectedAccountId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedAccount?.name ?: "All Accounts",
            onValueChange = {},
            readOnly = true,
            label = { Text("Filter by Account") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("All Accounts") },
                onClick = { onAccountSelected(null); expanded = false }
            )
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.name) },
                    onClick = { onAccountSelected(account.id); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseTransactionCard(
    transaction: Transaction,
    account: Account?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.payee.ifBlank { "No description" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (transaction.category.isNotBlank()) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = DateUtils.formatDisplayDateTime(transaction.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (account != null) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.formatCurrency(
                        transaction.amount,
                        account?.currency ?: "INR"
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.amount >= BigDecimal.ZERO)
                        MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )

                Box {
                    IconButton(onClick = { showDropdown = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                    }
                    DropdownMenu(expanded = showDropdown, onDismissRequest = { showDropdown = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { showDropdown = false; onEdit() },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { showDropdown = false; onDelete() },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountDialog(viewModel: AccountViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.showAddDialog) {
        val editingAccount = uiState.editingAccount

        var name by remember { mutableStateOf(editingAccount?.name ?: "") }
        var selectedType by remember { mutableStateOf(editingAccount?.type ?: AccountType.BANK) }
        var openingBalance by remember { mutableStateOf(editingAccount?.openingBalance?.toPlainString() ?: "0.00") }
        var notes by remember { mutableStateOf(editingAccount?.notes ?: "") }
        var expanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { viewModel.hideAddAccountDialog() },
            title = { Text(if (editingAccount == null) "Add Account" else "Edit Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Account Name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        isError = name.isBlank(),
                        supportingText = if (name.isBlank()) { { Text("Account name is required") } } else null
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded && !uiState.isLoading }
                    ) {
                        OutlinedTextField(
                            value = selectedType.name.replace("_", " "),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Account Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            enabled = !uiState.isLoading
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            AccountType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name.replace("_", " ")) },
                                    onClick = { selectedType = type; expanded = false }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = openingBalance,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) openingBalance = it
                        },
                        label = { Text("Opening Balance") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        leadingIcon = { Text("₹") }
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        enabled = !uiState.isLoading
                    )

                    uiState.errorMessage?.let { error ->
                        Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val balance = try { BigDecimal(openingBalance.ifBlank { "0" }) } catch (_: NumberFormatException) { BigDecimal.ZERO }
                        viewModel.saveAccount(name, selectedType, balance, notes)
                    },
                    enabled = !uiState.isLoading && name.isNotBlank()
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideAddAccountDialog() }, enabled = !uiState.isLoading) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    viewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val allCategories by categoryViewModel.all.collectAsStateWithLifecycle()

    if (uiState.showAddDialog) {
        val editingTransaction = uiState.editingTransaction

        var selectedAccountId by remember { mutableLongStateOf(editingTransaction?.accountId ?: accounts.firstOrNull()?.id ?: 0L) }
        var amount by remember { mutableStateOf(editingTransaction?.amount?.abs()?.toPlainString() ?: "") }
        var payee by remember { mutableStateOf(editingTransaction?.payee ?: "") }
        var memo by remember { mutableStateOf(editingTransaction?.memo ?: "") }
        var selectedType by remember { mutableStateOf(editingTransaction?.type ?: TransactionType.EXPENSE) }
        var checkNumber by remember { mutableStateOf(editingTransaction?.checkNumber ?: "") }
        var selectedDate by remember { mutableStateOf(editingTransaction?.date ?: getSafeCurrentDateTime()) }

        // Category state
        var categoryId by remember { mutableStateOf<Long?>(editingTransaction?.categoryId) }
        var subCategoryId by remember { mutableStateOf<Long?>(null) }

        var accountDropdownExpanded by remember { mutableStateOf(false) }
        var typeDropdownExpanded by remember { mutableStateOf(false) }
        var showDatePicker by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { viewModel.hideAddTransactionDialog() },
            title = { Text(if (editingTransaction == null) "Add Transaction" else "Edit Transaction") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                    // Account selection
                    ExposedDropdownMenuBox(
                        expanded = accountDropdownExpanded,
                        onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded && !uiState.isLoading }
                    ) {
                        OutlinedTextField(
                            value = accounts.find { it.id == selectedAccountId }?.name ?: "Select Account",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Account") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            enabled = !uiState.isLoading,
                            isError = selectedAccountId == 0L
                        )
                        ExposedDropdownMenu(expanded = accountDropdownExpanded, onDismissRequest = { accountDropdownExpanded = false }) {
                            accounts.forEach { account ->
                                DropdownMenuItem(
                                    text = { Text("${account.name} (${account.type.name.replace("_", " ")})") },
                                    onClick = { selectedAccountId = account.id; accountDropdownExpanded = false }
                                )
                            }
                        }
                    }

                    // Type selection
                    ExposedDropdownMenuBox(
                        expanded = typeDropdownExpanded,
                        onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded && !uiState.isLoading }
                    ) {
                        OutlinedTextField(
                            value = selectedType.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            enabled = !uiState.isLoading
                        )
                        ExposedDropdownMenu(expanded = typeDropdownExpanded, onDismissRequest = { typeDropdownExpanded = false }) {
                            TransactionType.entries.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type.name) },
                                    onClick = {
                                        selectedType = type
                                        categoryId = null // Reset category when type changes
                                        subCategoryId = null
                                        typeDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Category Picker (filtered by type)
                    if (selectedType != TransactionType.TRANSFER) {
                        CategoryPicker(
                            categories = allCategories,
                            selectedType = selectedType.name,
                            selectedCategoryId = categoryId,
                            onSelected = { id ->
                                categoryId = id
                            }
                        )

                        // Subcategory Picker (if parent category selected)
                        SubCategoryPicker(
                            categories = allCategories,
                            parentId = categoryId,
                            selectedSubId = subCategoryId,
                            onSelected = { id -> subCategoryId = id }
                        )
                    }

                    // Amount
                    OutlinedTextField(
                        value = amount,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) amount = it
                        },
                        label = { Text("Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading,
                        leadingIcon = { Text("₹") },
                        isError = amount.isBlank(),
                        supportingText = if (amount.isBlank()) { { Text("Amount is required") } } else null
                    )

                    OutlinedTextField(
                        value = payee,
                        onValueChange = { payee = it },
                        label = { Text("Payee/Description") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    )

                    OutlinedTextField(
                        value = memo,
                        onValueChange = { memo = it },
                        label = { Text("Memo (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        enabled = !uiState.isLoading
                    )

                    OutlinedTextField(
                        value = DateUtils.formatDisplayDateTime(selectedDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Date") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    )

                    if (selectedType != TransactionType.TRANSFER) {
                        OutlinedTextField(
                            value = checkNumber,
                            onValueChange = { checkNumber = it },
                            label = { Text("Check Number (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading
                        )
                    }

                    uiState.errorMessage?.let { error ->
                        Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amountValue = try { BigDecimal(amount.ifBlank { "0" }) } catch (_: NumberFormatException) { BigDecimal.ZERO }
                        if (amountValue > BigDecimal.ZERO && selectedAccountId != 0L) {
                            val finalCategoryId = subCategoryId ?: categoryId
                            val categoryName = if (finalCategoryId != null) {
                                allCategories.find { it.id == finalCategoryId }?.name ?: ""
                            } else ""

                            viewModel.saveTransaction(
                                selectedAccountId, amountValue, payee, categoryName, memo, selectedDate, selectedType, checkNumber, finalCategoryId
                            )
                        }
                    },
                    enabled = !uiState.isLoading && amount.isNotBlank() && selectedAccountId != 0L
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(16.dp)) else Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideAddTransactionDialog() }, enabled = !uiState.isLoading) { Text("Cancel") }
            }
        )

        if (showDatePicker) {
            DatePickerDialog(
                initialDate = selectedDate,
                onDateSelected = { newDate -> selectedDate = newDate },
                onDismiss = { showDatePicker = false }
            )
        }
    }
}

// Helper function for safe LocalDateTime creation
private fun getSafeCurrentDateTime(): LocalDateTime {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDateTime.now()
    } else {
        @Suppress("NewApi")
        LocalDateTime.of(2024, 1, 1, 12, 0)
    }
}
