package com.example.refractiveindexapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp

private const val DatabaseRepositoryUrl = "https://github.com/polyanskiy/refractiveindex.info-database"
private const val DatabaseDoiUrl = "https://doi.org/10.1038/s41597-023-02898-2"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    val uriHandler = LocalUriHandler.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Refractive Index", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "A mobile-first interface for exploring refractive-index data and optical calculations.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Material data", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Material data is sourced from the refractiveindex.info database. " +
                                "The database is dedicated to the public domain under CC0 1.0.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "This independent app is not affiliated with or endorsed by refractiveindex.info or M. N. Polyanskiy.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { uriHandler.openUri(DatabaseRepositoryUrl) }) {
                            Text("Open data repository")
                        }
                    }
                }
            }
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Citation", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Polyanskiy, M. N. Refractiveindex.info database of optical constants. " +
                                "Scientific Data 11, 94 (2024).",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { uriHandler.openUri(DatabaseDoiUrl) }) {
                            Text("Open publication")
                        }
                    }
                }
            }
        }
    }
}
