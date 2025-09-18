package com.example.expensetrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.expensetrack.data.entity.Category
import com.example.expensetrack.viewmodel.CategoryViewModel

@Composable
fun CategoryManagerScreen(
    viewModel: CategoryViewModel,
    showAddDialog: Boolean,
    onDismissAddDialog: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val categories by viewModel.all.collectAsStateWithLifecycle()
    val ui by viewModel.ui.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
    ) {
        if (categories.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No categories yet. Tap + to add.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Spacer(Modifier.height(4.dp)) }
                items(categories, key = { it.id }) { cat ->
                    CategoryRow(
                        category = cat,
                        onEdit = { viewModel.edit(cat) },
                        onDelete = { viewModel.delete(cat) }
                    )
                }
            }
        }
    }

    val shouldShow = showAddDialog || ui.editing != null
    if (shouldShow) {
        AddEditCategoryDialog(
            all = categories,
            uiLoading = ui.isLoading,
            editing = ui.editing,
            onDismiss = {
                onDismissAddDialog()
                viewModel.cancel()
            },
            onSave = { name, type, parentId ->
                viewModel.save(name, type, parentId)
                onDismissAddDialog()
            }
        )
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(category.name, style = MaterialTheme.typography.titleMedium)
                val chips = buildList {
                    add(category.type)
                    add(if (category.parentId == null) "Category" else "Subcategory")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    chips.forEach { c -> AssistChip(onClick = {}, label = { Text(c) }) }
                }
            }
            Row {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditCategoryDialog(
    all: List<Category>,
    uiLoading: Boolean,
    editing: Category?,
    onDismiss: () -> Unit,
    onSave: (String, String, Long?) -> Unit
) {
    var name by remember { mutableStateOf(editing?.name ?: "") }
    var type by remember { mutableStateOf(editing?.type ?: "EXPENSE") }
    var parentId by remember { mutableStateOf(editing?.parentId) }

    var typeExpanded by remember { mutableStateOf(false) }
    var parentExpanded by remember { mutableStateOf(false) }
    val topLevel = remember(all, type) { all.filter { it.parentId == null && it.type == type } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing == null) "Add Category" else "Edit Category") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiLoading
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded && !uiLoading }
                ) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        enabled = !uiLoading
                    )
                    ExposedDropdownMenu(typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        listOf("EXPENSE", "INCOME").forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = {
                                    type = t
                                    if (parentId != null && all.firstOrNull { it.id == parentId }?.type != t) {
                                        parentId = null
                                    }
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = parentExpanded,
                    onExpandedChange = { parentExpanded = !parentExpanded && !uiLoading }
                ) {
                    OutlinedTextField(
                        value = topLevel.firstOrNull { it.id == parentId }?.name ?: "None",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Parent (optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(parentExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        enabled = !uiLoading
                    )
                    ExposedDropdownMenu(parentExpanded, onDismissRequest = { parentExpanded = false }) {
                        DropdownMenuItem(text = { Text("None") }, onClick = { parentId = null; parentExpanded = false })
                        topLevel.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = { parentId = cat.id; parentExpanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name.trim(), type, parentId) },
                enabled = name.isNotBlank() && !uiLoading
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !uiLoading) { Text("Cancel") } }
    )
}
