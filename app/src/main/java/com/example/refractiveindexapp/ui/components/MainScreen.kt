package com.example.refractiveindexapp.ui.components

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import com.example.refractiveindexapp.database.Material
import com.example.refractiveindexapp.parsing.Catalogue
import com.example.refractiveindexapp.parsing.MaterialGatherer
import com.example.refractiveindexapp.parsing.Page
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
        }

    }
}