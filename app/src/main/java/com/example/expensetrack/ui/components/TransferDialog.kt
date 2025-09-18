package com.example.expensetrack.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensetrack.data.entity.Account
import com.example.expensetrack.util.DateUtils
import java.math.BigDecimal
import java.time.LocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferDialog(
    accounts: List<Account>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (fromId: Long, toId: Long, amount: BigDecimal, whenAt: LocalDateTime, memo: String?) -> Unit
) {
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    var fromId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var toId by remember { mutableStateOf(accounts.drop(1).firstOrNull()?.id ?: 0L) }
    var amountText by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var whenAt by remember { mutableStateOf(LocalDateTime.now()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
        title = { Text("Transfer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // From
                ExposedDropdownMenuBox(
                    expanded = fromExpanded,
                    onExpandedChange = { fromExpanded = !fromExpanded && !isLoading }
                ) {
                    OutlinedTextField(
                        value = accounts.firstOrNull { it.id == fromId }?.name ?: "Select From",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(fromExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        isError = fromId == 0L
                    )
                    ExposedDropdownMenu(fromExpanded, onDismissRequest = { fromExpanded = false }) {
                        accounts.forEach { a ->
                            DropdownMenuItem(text = { Text(a.name) }, onClick = { fromId = a.id; fromExpanded = false })
                        }
                    }
                }

                // To
                ExposedDropdownMenuBox(
                    expanded = toExpanded,
                    onExpandedChange = { toExpanded = !toExpanded && !isLoading }
                ) {
                    OutlinedTextField(
                        value = accounts.firstOrNull { it.id == toId }?.name ?: "Select To",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(toExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        isError = toId == 0L || toId == fromId
                    )
                    ExposedDropdownMenu(toExpanded, onDismissRequest = { toExpanded = false }) {
                        accounts.forEach { a ->
                            DropdownMenuItem(text = { Text(a.name) }, onClick = { toId = a.id; toExpanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) amountText = it },
                    label = { Text("Amount") },
                    leadingIcon = { Text("₹") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = DateUtils.formatDisplayDateTime(whenAt),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("Memo (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    if (fromId != 0L && toId != 0L && toId != fromId && amt > BigDecimal.ZERO) {
                        onConfirm(fromId, toId, amt, whenAt, memo.ifBlank { null })
                    }
                },
                enabled = fromId != 0L && toId != 0L && toId != fromId && (amountText.toBigDecimalOrNull() ?: BigDecimal.ZERO) > BigDecimal.ZERO && !isLoading
            ) { Text("Transfer") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !isLoading) { Text("Cancel") } }
    )
}
