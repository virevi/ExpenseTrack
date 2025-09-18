package com.example.expensetrack.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CompactTransactionRow(
    date: String,
    account: String,
    category: String,
    subcategory: String? = null,
    amount: BigDecimal,
    isCredit: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: date, category/subcategory, account
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                val catLine = if (subcategory.isNullOrBlank()) category else "$category • $subcategory"
                Text(
                    text = catLine,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = account,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right: amount + actions
            Column(
                horizontalAlignment = Alignment.End
            ) {
                val amountColor = if (isCredit)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.error

                Text(
                    text = formatCurrency(amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = amountColor,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}

private fun formatCurrency(value: BigDecimal): String {
    val nf = NumberFormat.getCurrencyInstance(Locale.getDefault())
    return nf.format(value)
}
