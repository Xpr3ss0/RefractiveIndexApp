package com.example.refractiveindexapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.refractiveindexapp.parsing.MaterialModel
import com.example.refractiveindexapp.ui.view.MainViewModel
import com.example.refractiveindexapp.ui.view.MaterialLoadState
import dev.xpr3ss0.scientificplot.ScientificPlot

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onAddMaterial: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Refractive index") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MaterialHeader(
                    selection = viewModel.selectedPage?.let {
                        "${it.parentBook.parentShelf.id}  /  ${it.parentBook.id}  /  ${it.id}"
                    },
                    onChooseMaterial = onAddMaterial
                )
            }
            when (val loadState = viewModel.materialLoadState) {
                MaterialLoadState.Idle -> item { EmptyMaterialState() }
                MaterialLoadState.Loading -> item {
                    Card {
                        Column(Modifier.padding(16.dp)) {
                            Text("Loading material data…", style = MaterialTheme.typography.titleMedium)
                            LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 12.dp))
                        }
                    }
                }
                MaterialLoadState.Loaded -> viewModel.currentMaterial?.let { material ->
                    item { MaterialSummary(material) }
                    item { DispersionPlotCard(viewModel) }
                    if (material.tabulatedData?.kArray != null) {
                        item { ExtinctionPlotCard(viewModel) }
                    }
                    material.references?.let { references ->
                        item { ReferenceCard(references) }
                    }
                }
                is MaterialLoadState.Failed -> item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Material could not be loaded", style = MaterialTheme.typography.titleMedium)
                            Text(loadState.message)
                            Button(onClick = { viewModel.selectedPage?.let(viewModel::selectPage) }) {
                                Text("Try again")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialHeader(selection: String?, onChooseMaterial: () -> Unit) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Material", style = MaterialTheme.typography.titleMedium)
            Text(
                text = selection ?: "Choose a material from the refractiveindex.info catalogue.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onChooseMaterial) {
                Text(if (selection == null) "Choose material" else "Change material")
            }
        }
    }
}

@Composable
private fun EmptyMaterialState() {
    Card {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Ready to inspect optical data", style = MaterialTheme.typography.titleMedium)
            Text(
                "Select a page to see its supported dispersion model or tabulated n/k data.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MaterialSummary(material: MaterialModel) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Material data", style = MaterialTheme.typography.titleMedium)
            material.dispersionData?.let { dispersion ->
                DetailRow("Model", "Formula ${dispersion.formulaType}")
                DetailRow("Wavelength range", "${dispersion.wavelengthRange} µm")
            }
            material.tabulatedData?.let { tabulated ->
                DetailRow("Tabulated data", tabulated.type)
                DetailRow("Samples", tabulated.wavelengthArray.size.toString())
            }
            material.conditions?.temperature?.let { DetailRow("Temperature", "$it K") }
            material.conditions?.pressure?.let { DetailRow("Pressure", "$it") }
            material.conditions?.direction?.let { DetailRow("Direction", it) }
            material.comments?.let {
                Text("Notes", style = MaterialTheme.typography.labelLarge)
                DatabaseRichText(it)
            }
        }
    }
}

@Composable
private fun ReferenceCard(references: String) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Reference", style = MaterialTheme.typography.titleMedium)
            DatabaseRichText(
                text = references,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DispersionPlotCard(viewModel: MainViewModel) {
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Dispersion", modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp), style = MaterialTheme.typography.titleMedium)
            ScientificPlot(viewModel.dispersionPlotManager)
        }
    }
}

@Composable
private fun ExtinctionPlotCard(viewModel: MainViewModel) {
    Card {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Extinction coefficient", modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp), style = MaterialTheme.typography.titleMedium)
            ScientificPlot(viewModel.extinctionPlotManager)
        }
    }
}
