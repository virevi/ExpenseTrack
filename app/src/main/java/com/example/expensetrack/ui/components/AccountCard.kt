package com.example.expensetrack.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensetrack.data.entity.Account
import com.example.expensetrack.data.entity.AccountType
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountCard(
    account: Account,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = account.type.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (account.openingBalance != java.math.BigDecimal.ZERO) {
                    Text(
                        text = "Balance: ${account.currency} ${account.openingBalance}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Box {
                IconButton(onClick = { showDropdown = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                }

                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showDropdown = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showDropdown = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun getAccountIcon(type: AccountType) = when (type) {
    AccountType.CASH -> Icons.Default.LocalAtm  // Changed from Money
    AccountType.BANK -> Icons.Default.AccountBalance
    AccountType.CREDIT_CARD -> Icons.Default.CreditCard
}

