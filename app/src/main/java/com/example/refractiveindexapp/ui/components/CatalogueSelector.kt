package com.example.refractiveindexapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    var pickerOpen by remember(label) { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = selection?.let(entryTitle).orEmpty(),
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = { Text(label) },
            placeholder = { Text("Select $label") },
            readOnly = true,
            enabled = enabled,
            singleLine = true
        )
        // The overlay makes the whole field a reliable touch target.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled) { pickerOpen = true }
        )
    }

    if (pickerOpen) {
        CataloguePickerSheet(
            label = label,
            entries = entries,
            entryTitle = entryTitle,
            entrySubtitle = entrySubtitle,
            entryDivider = entryDivider,
            onSelected = {
                onSelected(it)
                pickerOpen = false
            },
            onDismiss = { pickerOpen = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> CataloguePickerSheet(
    label: String,
    entries: List<T>,
    entryTitle: (T) -> String,
    entrySubtitle: (T) -> String,
    entryDivider: (T) -> String?,
    onSelected: (T) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember(label) { mutableStateOf("") }
    val normalizedQuery = query.trim()
    val visibleEntries = remember(entries, normalizedQuery) {
        if (normalizedQuery.isEmpty()) entries else entries.filter { entry ->
            listOf(entryTitle(entry), entrySubtitle(entry), entryDivider(entry).orEmpty())
                .any { it.contains(normalizedQuery, ignoreCase = true) }
        }
    }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        // Wait until the sheet and its text field are attached before asking
        // Android to show the IME.
        withFrameNanos { }
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            Text(
                text = "Select $label",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleLarge
            )
            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .focusRequester(focusRequester),
                label = { Text("Filter $label") },
                placeholder = { Text("Search name or section") },
                singleLine = true
            )
            if (visibleEntries.isEmpty()) {
                Text(
                    text = "No matching $label entries",
                    modifier = Modifier.padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F)
                ) {
                    itemsIndexed(visibleEntries) { index, entry ->
                        val divider = entryDivider(entry)
                        val previousDivider = visibleEntries.getOrNull(index - 1)?.let(entryDivider)
                        if (divider != null && divider != previousDivider) {
                            HorizontalDivider()
                            Text(
                                text = divider,
                                modifier = Modifier.padding(start = 24.dp, top = 16.dp, bottom = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelected(entry) }
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text(entryTitle(entry), style = MaterialTheme.typography.titleSmall)
                            DatabaseRichText(
                                text = entrySubtitle(entry),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp, bottom = 2.dp),
                                maxLines = 2,
                                linksEnabled = false,
                                onClick = { onSelected(entry) }
                            )
                        }
                    }
                }
            }
        }
    }
}
