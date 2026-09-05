package com.example.refractiveindexapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import com.example.refractiveindexapp.ui.theme.IndexInfoTheme
import com.example.refractiveindexapp.ui.components.AppNavigation
import com.example.refractiveindexapp.ui.view.MainViewModel
import com.example.refractiveindexapp.ui.view.MainViewModelFactory
import com.example.refractiveindexapp.settings.SharedPreferencesSettingsRepository
import com.example.refractiveindexapp.parsing.CatalogueSnapshotStore
import com.example.refractiveindexapp.parsing.PersistentCatalogueSnapshotRepository
import com.example.refractiveindexapp.utils.loadCuratedCatalogueSnapshot
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val curatedSnapshot = loadCuratedCatalogueSnapshot(this)
        val settingsRepository = SharedPreferencesSettingsRepository(this)
        val catalogueSnapshotRepository = PersistentCatalogueSnapshotRepository(
            store = CatalogueSnapshotStore(File(filesDir, "catalogue-snapshots"))
        )

        val viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(
                curatedSnapshot = curatedSnapshot,
                catalogueSnapshotRepository = catalogueSnapshotRepository,
                settingsRepository = settingsRepository
            )
        )[MainViewModel::class.java]

        setContent {

            val settings by settingsRepository.settings.collectAsState()
            IndexInfoTheme(themePreference = settings.themePreference, colorSchemePreference = settings.colorSchemePreference) {

                AppNavigation(viewModel)

            }
        }
    }
}
