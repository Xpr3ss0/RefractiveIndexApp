package com.example.refractiveindexapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CatalogueSelector(
    label: String,
    selection: T?,
    entries: List<T>,
    enabled: Boolean = true,
    entryTitle: (T) -> String,
    entrySubtitle: (T) -> String,
    entryDivider: (T) -> String?,
    onSelected: (T) -> Unit
) {
    var expanded by remember(label, enabled) { mutableStateOf(false) }
    var query by remember(label) { mutableStateOf("") }
    val normalizedQuery = query.trim()
    val visibleEntries = remember(entries, normalizedQuery) {
        if (normalizedQuery.isEmpty()) entries else entries.filter { entry ->
            listOf(entryTitle(entry), entrySubtitle(entry), entryDivider(entry).orEmpty())
                .any { it.contains(normalizedQuery, ignoreCase = true) }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selection?.let(entryTitle).orEmpty(),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            label = { Text(label) },
            placeholder = { Text("Select $label") },
            readOnly = true,
            enabled = enabled,
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 360.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    label = { Text("Filter $label") },
                    singleLine = true
                )
                if (visibleEntries.isEmpty()) {
                    Text(
                        text = "No matching $label entries",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                var lastDivider: String? = null
                visibleEntries.forEach { entry ->
                    val divider = entryDivider(entry)
                    if (divider != null && divider != lastDivider) {
                        HorizontalDivider()
                        Text(
                            text = divider,
                            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    lastDivider = divider
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(entryTitle(entry))
                                DatabaseRichText(
                                    text = entrySubtitle(entry),
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 2
                                )
                            }
                        },
                        onClick = {
                            onSelected(entry)
                            expanded = false
                            query = ""
                        }
                    )
                }
            }
        }
    }
}
