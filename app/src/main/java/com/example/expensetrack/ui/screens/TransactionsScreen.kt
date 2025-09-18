package com.example.expensetrack.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetrack.data.entity.Account
import com.example.expensetrack.data.entity.Transaction
import com.example.expensetrack.data.entity.TransactionType
import com.example.expensetrack.ui.components.TransactionCard
import com.example.expensetrack.util.DateUtils
import com.example.expensetrack.viewmodel.TransactionViewModel
import java.math.BigDecimal
import java.time.LocalDateTime
import com.example.expensetrack.ui.components.CompactTransactionRow

@Composable
fun TransactionsScreen(
    viewModel: TransactionViewModel
) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedAccountId by viewModel.selectedAccountId.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with account filter and add button
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

        // Account Filter Dropdown
        AccountFilterDropdown(
            accounts = accounts,
            selectedAccountId = selectedAccountId,
            onAccountSelected = { accountId: Long? ->
                viewModel.selectAccount(accountId)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bulk Actions Bar (shown when transactions are selected)
        if (uiState.selectedTransactions.isNotEmpty()) {
            BulkActionsBar(
                selectedCount = uiState.selectedTransactions.size,
                onDeleteSelected = { viewModel.deleteSelectedTransactions() },
                onClearSelection = { viewModel.clearSelection() }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Transactions List
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
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions, key = { it.id }) { transaction ->
                    val account = accounts.find { it.id == transaction.accountId }
                    CompactTransactionRow(
                        date = DateUtils.formatDisplayDateTime(transaction.date),
                        account = account?.name ?: "",
                        category = transaction.category, // or from your model
                        subcategory = transaction.subcategory, // add this field if not present yet
                        amount = transaction.amount,
                        isCredit = transaction.amount.signum() > 0,
                        onClick = { /* details */ },
                        onEdit = { /* edit */ },
                        onDelete = { /* delete */ }
                    )

                }

            }
        }
    }

    @Composable
    fun CompactTransactionRow(
        date: String,
        account: String,
        category: String?,
        subcategory: String?,
        amount: BigDecimal,
        isCredit: Boolean,
        onClick: () -> Unit,
        onEdit: () -> Unit,
        onDelete: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text( // Date
                text = date,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1.2f)
            )
            Column(Modifier.weight(2.5f)) {
                Text(
                    text = buildString {
                        append(account)
                        if (!category.isNullOrBlank()) append(":$category")
                        if (!subcategory.isNullOrBlank()) append(":$subcategory")
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text( // Amount
                text = (if(isCredit) "₹" else "-₹") + amount.abs().toPlainString(),
                color = if (isCredit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1.2f),
                textAlign = TextAlign.End
            )
        }
    }

    // Add/Edit Transaction Dialog
    if (uiState.showAddDialog) {
        AddEditTransactionDialog(
            transaction = uiState.editingTransaction,
            accounts = accounts,
            onDismiss = { viewModel.hideAddTransactionDialog() },
            onSave = { accountId, amount, payee, category, memo, date, type, checkNumber, categoryId ->
                viewModel.saveTransaction(accountId, amount, payee, category, memo, date, type, checkNumber, categoryId)
            },
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage
        )
    }

    // Error/Success Messages
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.exportSuccess) {
        if (uiState.exportSuccess) {
            viewModel.clearError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountFilterDropdown(
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
                onClick = {
                    onAccountSelected(null)
                    expanded = false
                }
            )

            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.name) },
                    onClick = {
                        onAccountSelected(account.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun BulkActionsBar(
    selectedCount: Int,
    onDeleteSelected: () -> Unit,
    onClearSelection: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$selectedCount selected",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )

            Row {
                IconButton(onClick = onDeleteSelected) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Selected")
                }

                IconButton(onClick = onClearSelection) {
                    Icon(Icons.Default.Close, contentDescription = "Clear Selection")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    transaction: Transaction?,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onSave: (Long, BigDecimal, String, String, String, LocalDateTime, TransactionType, String, Long?) -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    var selectedAccountId by remember { mutableLongStateOf(transaction?.accountId ?: accounts.firstOrNull()?.id ?: 0L) }
    var amount by remember { mutableStateOf(transaction?.amount?.abs()?.toPlainString() ?: "") }
    var payee by remember { mutableStateOf(transaction?.payee ?: "") }
    var category by remember { mutableStateOf(transaction?.category ?: "") }
    var memo by remember { mutableStateOf(transaction?.memo ?: "") }
    var selectedType by remember { mutableStateOf(transaction?.type ?: TransactionType.EXPENSE) }
    var checkNumber by remember { mutableStateOf(transaction?.checkNumber ?: "") }
    var selectedDate by remember {
        mutableStateOf(
            transaction?.date ?: if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                LocalDateTime.now()
            } else {
                LocalDateTime.of(2024, 1, 1, 12, 0)
            }
        )
    }

    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (transaction == null) "Add Transaction" else "Edit Transaction")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Account Selection
                ExposedDropdownMenuBox(
                    expanded = accountDropdownExpanded,
                    onExpandedChange = { accountDropdownExpanded = !accountDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = accounts.find { it.id == selectedAccountId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !isLoading
                    )
                    ExposedDropdownMenu(
                        expanded = accountDropdownExpanded,
                        onDismissRequest = { accountDropdownExpanded = false }
                    ) {
                        accounts.forEach { account ->
                            DropdownMenuItem(
                                text = { Text("${account.name} (${account.type.name})") },
                                onClick = {
                                    selectedAccountId = account.id
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Transaction Type
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !isLoading
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false }
                    ) {
                        TransactionType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedType = type
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                // Payee
                OutlinedTextField(
                    value = payee,
                    onValueChange = { payee = it },
                    label = { Text("Payee") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                // Category
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                // Memo
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("Memo (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    enabled = !isLoading
                )

                // Date Selection - Simplified for now
                OutlinedTextField(
                    value = DateUtils.formatDisplayDateTime(selectedDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    trailingIcon = {
                        IconButton(onClick = { /* TODO: Implement date picker */ }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                // Check Number (optional)
                OutlinedTextField(
                    value = checkNumber,
                    onValueChange = { checkNumber = it },
                    label = { Text("Check Number (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = try {
                        BigDecimal(amount)
                    } catch (_: NumberFormatException) {
                        BigDecimal.ZERO
                    }

                    onSave(
                        selectedAccountId,
                        amountValue,
                        payee,
                        category,
                        memo,
                        selectedDate,
                        selectedType,
                        checkNumber,
                        null
                    )
                },
                enabled = !isLoading && amount.isNotBlank() && selectedAccountId != 0L
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}
