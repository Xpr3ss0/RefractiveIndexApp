package com.example.refractiveindexapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.refractiveindexapp.settings.ThemePreference
import com.example.refractiveindexapp.settings.ColorSchemePreference
import com.example.refractiveindexapp.settings.DatabaseVersionPolicy
import com.example.refractiveindexapp.ui.view.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateUp: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    TextButton(onClick = onNavigateUp) { Text("Back") }
                },
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
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Data", style = MaterialTheme.typography.titleMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Update catalogue on startup")
                                Text(
                                    "Download the latest material catalogue when the app opens. Turn this off to use the bundled catalogue until a manual refresh is added.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.updateCatalogueOnStartup,
                                onCheckedChange = viewModel::setUpdateCatalogueOnStartup
                            )
                        }
                    }
                }
            }
            item {
                Card {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Appearance", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Choose how the app follows your device's color mode.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ThemePreference.entries.forEach { preference ->
                                FilterChip(
                                    selected = settings.themePreference == preference,
                                    onClick = { viewModel.setThemePreference(preference) },
                                    label = { Text(preference.name) }
                                )
                            }
                        }
                        Text("Color scheme", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ColorSchemePreference.entries.forEach { preference ->
                                FilterChip(selected = settings.colorSchemePreference == preference, onClick = { viewModel.setColorSchemePreference(preference) }, label = { Text(preference.name) })
                            }
                        }
                    }
                }
            }
            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Database version", style = MaterialTheme.typography.titleMedium)
                        Text("Latest follows the upstream main branch. A pinned commit takes effect after restarting the app.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DatabaseVersionPolicy.entries.forEach { policy ->
                                FilterChip(selected = settings.databaseVersionPolicy == policy, onClick = { viewModel.setDatabaseVersionPolicy(policy) }, label = { Text(if (policy == DatabaseVersionPolicy.Latest) "Latest" else "Specific commit") })
                            }
                        }
                        if (settings.databaseVersionPolicy == DatabaseVersionPolicy.SpecificCommit) {
                            OutlinedTextField(value = settings.databaseCommit, onValueChange = viewModel::setDatabaseCommit, label = { Text("Git commit SHA") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            TextButton(onClick = viewModel::pinCurrentDatabaseCommit) { Text("Set to current upstream commit") }
                            viewModel.databaseCommitError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                }
            }
            item {
                Card {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text("Hide unavailable constants")
                            Text("Only show derived optical constants that can be calculated for the selected material.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = settings.hideUnavailableConstants, onCheckedChange = viewModel::setHideUnavailableConstants)
                    }
                }
            }
        }
    }
}
