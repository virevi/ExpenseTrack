package com.example.expensetrack.ui.screens

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetrack.data.entity.Account
import com.example.expensetrack.data.entity.AccountType
import com.example.expensetrack.ui.components.AccountCard
import com.example.expensetrack.viewmodel.AccountViewModel
import java.math.BigDecimal

@Composable
fun AccountsScreen(
    viewModel: AccountViewModel,
    onNavigateToTransactions: (Long) -> Unit
) {
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
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

        if (accounts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No accounts yet. Add your first account!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = accounts,
                    key = { account -> account.id }
                ) { account ->
                    AccountCard(
                        account = account,
                        onClick = { onNavigateToTransactions(account.id) },
                        onEdit = { viewModel.editAccount(account) },
                        onDelete = { viewModel.deleteAccount(account) }
                    )
                }
            }
        }
    }

    // Add/Edit Account Dialog
    if (uiState.showAddDialog) {
        AddEditAccountDialog(
            account = uiState.editingAccount,
            onDismiss = { viewModel.hideAddAccountDialog() },
            onSave = { name, type, balance, notes ->
                viewModel.saveAccount(name, type, balance, notes)
            },
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage
        )
    }

    // Error Snackbar
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
            viewModel.clearError()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountDialog(
    account: Account?,
    onDismiss: () -> Unit,
    onSave: (String, AccountType, BigDecimal, String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    var name by remember { mutableStateOf(account?.name ?: "") }
    var selectedType by remember { mutableStateOf(account?.type ?: AccountType.BANK) }
    var openingBalance by remember {
        mutableStateOf(account?.openingBalance?.toPlainString() ?: "0.00")
    }
    var notes by remember { mutableStateOf(account?.notes ?: "") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (account == null) "Add Account" else "Edit Account")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedType.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Account Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        enabled = !isLoading
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        AccountType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.name) },
                                onClick = {
                                    selectedType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = openingBalance,
                    onValueChange = { openingBalance = it },
                    label = { Text("Opening Balance") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
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
                    val balance = try {
                        BigDecimal(openingBalance)
                    } catch (e: NumberFormatException) {
                        BigDecimal.ZERO
                    }
                    onSave(name, selectedType, balance, notes)
                },
                enabled = !isLoading && name.isNotBlank()
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
