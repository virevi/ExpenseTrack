package com.example.expensetrack.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.expensetrack.data.entity.Account
import com.example.expensetrack.data.entity.Transaction
import com.example.expensetrack.data.entity.TransactionType
import com.example.expensetrack.ui.theme.ExpenseRed
import com.example.expensetrack.ui.theme.IncomeGreen
import com.example.expensetrack.ui.theme.TransferBlue
import com.example.expensetrack.util.CurrencyUtils
import com.example.expensetrack.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionCard(
    transaction: Transaction,
    account: Account?,
    subcategory: String?,        // if your model tracks subcategory separately, else use transaction.category for leaf
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showDropdown by remember { mutableStateOf(false) }
    val amountColor = when (transaction.type) {
        TransactionType.INCOME -> IncomeGreen
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.TRANSFER -> TransferBlue
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date
        Text(
            DateUtils.formatShortDate(transaction.date),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1.1f)
        )
        // Account:Category:Subcategory chain
        Text(
            buildString {
                append(account?.name ?: "")
                if (!transaction.category.isNullOrBlank()) append(":${transaction.category}")
                if (!subcategory.isNullOrBlank()) append(":$subcategory")
            },
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(2.2f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        // Amount
        Text(
            CurrencyUtils.formatCurrency(transaction.amount, account?.currency ?: "INR"),
            style = MaterialTheme.typography.bodyMedium,
            color = amountColor,
            modifier = Modifier.weight(1.2f),
            textAlign = TextAlign.End
        )
        // Optional action menu
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
                    leadingIcon = { Icon(Icons.Default.Edit, null) }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        showDropdown = false
                        onDelete()
                    },
                    leadingIcon = { Icon(Icons.Default.Delete, null) }
                )
            }
        }
    }
}

@Composable
fun getTransactionIcon(type: TransactionType) = when (type) {
    TransactionType.INCOME -> Icons.Default.Add
    TransactionType.EXPENSE -> Icons.Default.KeyboardArrowDown  // This should work
    TransactionType.TRANSFER -> Icons.Default.SwapHoriz  // This should work too
}

@Composable
fun getTransactionColor(type: TransactionType) = when (type) {
    TransactionType.INCOME -> IncomeGreen
    TransactionType.EXPENSE -> ExpenseRed
    TransactionType.TRANSFER -> TransferBlue
}
