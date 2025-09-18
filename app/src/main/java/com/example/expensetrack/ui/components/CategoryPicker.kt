package com.example.expensetrack.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import com.example.expensetrack.data.entity.Category

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryPicker(
    categories: List<Category>,
    selectedType: String,
    selectedCategoryId: Long?,
    onSelected: (Long?) -> Unit
) {
    // Only show categories (of type) that have NO children
    val leafCategories = remember(categories, selectedType) {
        val byParent = categories.groupBy { it.parentId }
        categories
            .filter { it.type == selectedType }
            .filter { cat -> byParent[cat.id].isNullOrEmpty() }
    }

    var expanded by remember { mutableStateOf(false) }
    val selected = leafCategories.firstOrNull { it.id == selectedCategoryId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected?.name ?: "Select Category",
            onValueChange = {},
            readOnly = true,
            label = { Text("Subcategory") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            leafCategories.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat.name) },
                    onClick = {
                        onSelected(cat.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCategoryPicker(
    categories: List<Category>,
    parentId: Long?,
    selectedSubId: Long?,
    onSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val children = remember(categories, parentId) { categories.filter { it.parentId == parentId } }
    if (parentId == null || children.isEmpty()) return

    val label = "Subcategory"
    val selectedName = children.firstOrNull { it.id == selectedSubId }?.name ?: "Select $label"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            children.forEach { c ->
                DropdownMenuItem(
                    text = { Text(c.name) },
                    onClick = { onSelected(c.id); expanded = false }
                )
            }
        }
    }
}
