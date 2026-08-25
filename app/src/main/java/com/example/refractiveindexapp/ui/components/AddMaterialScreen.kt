package com.example.refractiveindexapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.refractiveindexapp.ui.view.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMaterialScreen(
    viewModel: MainViewModel,
    onFinished: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose a material") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Browse the database in three steps. Search also matches the section separators in the catalogue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CatalogueSelector(
                label = "Shelf",
                selection = viewModel.selectedShelf,
                entries = viewModel.catalogue.entries,
                entryTitle = { it.id },
                entrySubtitle = { it.name },
                entryDivider = { it.divider?.title },
                onSelected = viewModel::selectShelf
            )
            CatalogueSelector(
                label = "Book",
                selection = viewModel.selectedBook,
                entries = viewModel.selectedShelf?.content.orEmpty(),
                enabled = viewModel.selectedShelf != null,
                entryTitle = { it.id },
                entrySubtitle = { it.name },
                entryDivider = { it.divider?.title },
                onSelected = viewModel::selectBook
            )
            CatalogueSelector(
                label = "Page",
                selection = viewModel.selectedPage,
                entries = viewModel.selectedBook?.content.orEmpty(),
                enabled = viewModel.selectedBook != null,
                entryTitle = { it.id },
                entrySubtitle = { it.name },
                entryDivider = { it.divider?.title },
                onSelected = viewModel::selectPage
            )
            Spacer(Modifier.weight(1f))
            Button(
                onClick = onFinished,
                enabled = viewModel.selectedPage != null,
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text("View material")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
