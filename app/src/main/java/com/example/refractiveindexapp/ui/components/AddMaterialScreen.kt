package com.example.refractiveindexapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.refractiveindexapp.parsing.Book
import com.example.refractiveindexapp.parsing.Catalogue
import com.example.refractiveindexapp.parsing.Page
import com.example.refractiveindexapp.parsing.Shelf
import com.example.refractiveindexapp.ui.view.MainViewModel

@Composable
fun AddMaterialScreen(
    viewModel: MainViewModel,
    onFinished: () -> Unit
) {

    var step by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.padding(80.dp)
    ) {

        when(step) {

            0 -> {

                Selector<Shelf>(
                    "Select shelf",
                    entries = viewModel.catalogue.entries,
                    displayName = {shelf -> shelf.id},
                    displaySelection = { viewModel.selectedShelf?.id ?: "Select shelf" }
                ) {
                    viewModel.selectShelf(it)
                }

                Button(
                    enabled = viewModel.selectedShelf != null,
                    onClick = { step++ }
                ) {
                    Text("Next")
                }
            }


            1 -> {

                Selector<Book>(
                    "Select shelf",
                    entries = viewModel.selectedShelf?.content,
                    displayName = {book -> book.id},
                    displaySelection = { viewModel.selectedBook?.id ?: "Select book" }
                ) {
                    viewModel.selectBook(it)
                }

                Row {

                    Button(
                        onClick = { step-- }
                    ) {
                        Text("Back")
                    }

                    Button(
                        enabled = viewModel.selectedBook != null,
                        onClick = { step++ }
                    ) {
                        Text("Next")
                    }
                }
            }


            2 -> {

                Selector<Page>(
                    "Select page",
                    entries = viewModel.selectedBook?.content,
                    displayName = {page -> page.id},
                    displaySelection = { viewModel.selectedPage?.id ?: "Select page" }
                ) {
                    viewModel.selectPage(it) // will also load material
                }

                Row {

                    Button(
                        onClick = { step-- }
                    ) {
                        Text("Back")
                    }

                    Button(
                        enabled = viewModel.selectedPage != null,
                        onClick = {
                            onFinished()
                        }
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Selector(
    title: String,
    entries: List<T>?,
    displaySelection: () -> String,
    displayName: (T) -> String,
    onSelected: (T) -> Unit
) {

    var expanded by remember {
        mutableStateOf(false)
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {

        TextField(
            value = displaySelection(),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
        )


        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            entries?.forEach { entry ->

                DropdownMenuItem(
                    text = {
                        Text(displayName(entry))
                    },
                    onClick = {
                        expanded = false
                        onSelected(entry)
                    }
                )
            }
        }
    }
}