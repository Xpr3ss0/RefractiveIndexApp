package com.example.refractiveindexapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.refractiveindexapp.ui.view.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onAddMaterial: () -> Unit
) {


    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMaterial
            ) {
                Text("+")
            }
        }
    )
    { it ->
        Column(modifier = Modifier.padding(it)) {

            Text(viewModel.selectedPage?.dataPath ?: "no page selected")
            Text(viewModel.currentMaterial?.references ?: "no material loaded")
            // VerticalDivider(thickness = 10.dp)
            Text(viewModel.currentMaterial?.dispersionData?.formulaType?.toString() ?: "no dispersion coefficients available.")
            Text(viewModel.currentMaterial?.dispersionData?.coefficients ?: "...")
            Text(viewModel.currentMaterial?.tabulatedData?.type ?: "no tabulated data available.")

            DispersionPlot(viewModel.wavelengthPlotData, viewModel.indexPlotData)
        }

    }
}