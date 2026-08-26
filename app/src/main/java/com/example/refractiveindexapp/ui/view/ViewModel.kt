package com.example.refractiveindexapp.ui.view

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.referentialEqualityPolicy
import com.example.refractiveindexapp.parsing.Catalogue
import com.example.refractiveindexapp.parsing.CatalogueRepository
import com.example.refractiveindexapp.parsing.DatabaseRevision
import com.example.refractiveindexapp.parsing.DatabaseRevisionResolver
import com.example.refractiveindexapp.parsing.RemoteCatalogueRepository
import com.example.refractiveindexapp.parsing.Shelf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.refractiveindexapp.parsing.Book
import com.example.refractiveindexapp.parsing.MaterialModel
import com.example.refractiveindexapp.parsing.MaterialAbout
import com.example.refractiveindexapp.parsing.MaterialAboutRepository
import com.example.refractiveindexapp.parsing.MaterialRepository
import com.example.refractiveindexapp.parsing.Page
import com.example.refractiveindexapp.parsing.RemoteMaterialAboutRepository
import com.example.refractiveindexapp.parsing.RemoteMaterialRepository
import com.example.refractiveindexapp.physics.DerivedOpticalConstants
import com.example.refractiveindexapp.physics.DerivedOpticalConstantsCalculator
import com.example.refractiveindexapp.physics.FresnelCalculator
import com.example.refractiveindexapp.physics.FresnelResult
import com.example.refractiveindexapp.physics.OpticalDataProvider
import com.example.refractiveindexapp.settings.InMemorySettingsRepository
import com.example.refractiveindexapp.settings.SettingsRepository
import com.example.refractiveindexapp.settings.ThemePreference
import com.example.refractiveindexapp.settings.ColorSchemePreference
import com.example.refractiveindexapp.settings.DatabaseVersionPolicy
import dev.xpr3ss0.scientificplot.model.DataSeries
import dev.xpr3ss0.scientificplot.model.SeriesPlot
import dev.xpr3ss0.scientificplot.state.PlotManager
import dev.xpr3ss0.scientificplot.state.PlotState
import kotlinx.coroutines.launch

sealed interface MaterialLoadState {
    data object Idle : MaterialLoadState
    data object Loading : MaterialLoadState
    data object Loaded : MaterialLoadState
    data class Failed(val message: String) : MaterialLoadState
}

sealed interface CatalogueLoadState {
    data object Ready : CatalogueLoadState
    data object Loading : CatalogueLoadState
    data object Loaded : CatalogueLoadState
    data class UsingBundledCatalogue(val message: String) : CatalogueLoadState
}

sealed interface MaterialAboutLoadState {
    data object Idle : MaterialAboutLoadState
    data object Loading : MaterialAboutLoadState
    data object Loaded : MaterialAboutLoadState
    data object Unavailable : MaterialAboutLoadState
}

class MainViewModel(
    fallbackCatalogue: Catalogue,
    private val materialRepository: MaterialRepository = RemoteMaterialRepository(),
    private val materialAboutRepository: MaterialAboutRepository = RemoteMaterialAboutRepository(),
    private val catalogueRepository: CatalogueRepository? = null,
    private val databaseRevision: DatabaseRevision = DatabaseRevision.Latest,
    private val settingsRepository: SettingsRepository = InMemorySettingsRepository()
) : ViewModel() {

    val settings = settingsRepository.settings
    var databaseCommitError by mutableStateOf<String?>(null)
        private set

    // Catalogue entries link back to their parent shelf/book, forming a cyclic graph.
    // Structural equality would recurse through that graph when a refreshed catalogue is assigned.
    var catalogue by mutableStateOf(fallbackCatalogue, referentialEqualityPolicy())
        private set

    var catalogueLoadState by mutableStateOf<CatalogueLoadState>(
        if (catalogueRepository == null || !settingsRepository.settings.value.updateCatalogueOnStartup) {
            CatalogueLoadState.Ready
        } else {
            CatalogueLoadState.Loading
        }
    )
        private set

    init {
        if (catalogueRepository != null && settings.value.updateCatalogueOnStartup) refreshCatalogue()
    }

    var selectedShelf by mutableStateOf<Shelf?>(null)
        private set

    var selectedBook by mutableStateOf<Book?>(null)
        private set

    var selectedPage by mutableStateOf<Page?>(null)
        private set

    // material
    var currentMaterial by mutableStateOf<MaterialModel?>(null)
        private set

    var materialLoadState by mutableStateOf<MaterialLoadState>(MaterialLoadState.Idle)
        private set

    var materialAbout by mutableStateOf<MaterialAbout?>(null)
        private set

    var materialAboutLoadState by mutableStateOf<MaterialAboutLoadState>(MaterialAboutLoadState.Idle)
        private set

    var derivedWavelengthText by mutableStateOf("0.5876")
        private set

    var derivedWavelengthError by mutableStateOf<String?>(null)
        private set

    var derivedOpticalConstants by mutableStateOf<DerivedOpticalConstants?>(null)
        private set

    var fresnelAngleText by mutableStateOf("0")
        private set

    var fresnelAngleError by mutableStateOf<String?>(null)
        private set

    var fresnelResult by mutableStateOf<FresnelResult?>(null)
        private set

    val wavelengthSliderRange: ClosedFloatingPointRange<Double>?
        get() = currentMaterial?.let { OpticalDataProvider.from(it).refractiveIndexRange() }

    val dispersionPlotManager = PlotManager(PlotState.defaultFromEmpty()).apply {
        setAxisLabels(xLabel = "Wavelength (µm)", yLabel = "Refractive index")
    }
    val extinctionPlotManager = PlotManager(PlotState.defaultFromEmpty()).apply {
        setAxisLabels(xLabel = "Wavelength (µm)", yLabel = "Extinction coefficient k")
    }

    fun refreshCatalogue() {
        val repository = catalogueRepository ?: return
        viewModelScope.launch {
            catalogueLoadState = CatalogueLoadState.Loading
            repository.load(databaseRevision).fold(
                onSuccess = { downloadedCatalogue ->
                    catalogue = downloadedCatalogue
                    clearSelection()
                    catalogueLoadState = CatalogueLoadState.Loaded
                },
                onFailure = { throwable ->
                    catalogueLoadState = CatalogueLoadState.UsingBundledCatalogue(
                        throwable.message ?: "Could not update the material catalogue."
                    )
                }
            )
        }
    }

    fun setUpdateCatalogueOnStartup(enabled: Boolean) {
        settingsRepository.setUpdateCatalogueOnStartup(enabled)
    }

    fun setThemePreference(preference: ThemePreference) {
        settingsRepository.setThemePreference(preference)
    }
    fun setColorSchemePreference(preference: ColorSchemePreference) = settingsRepository.setColorSchemePreference(preference)
    fun setHideUnavailableConstants(hide: Boolean) = settingsRepository.setHideUnavailableConstants(hide)
    fun setDatabaseVersionPolicy(policy: DatabaseVersionPolicy) = settingsRepository.setDatabaseVersionPolicy(policy)
    fun setDatabaseCommit(commit: String) = settingsRepository.setDatabaseCommit(commit.trim())
    fun pinCurrentDatabaseCommit() {
        viewModelScope.launch {
            DatabaseRevisionResolver().currentCommit().fold(
                onSuccess = { commit ->
                    settingsRepository.setDatabaseCommit(commit.sha)
                    settingsRepository.setDatabaseVersionPolicy(DatabaseVersionPolicy.SpecificCommit)
                    databaseCommitError = null
                },
                onFailure = { databaseCommitError = it.message ?: "Could not resolve the current database commit." }
            )
        }
    }

    fun selectPage(page: Page) {
        viewModelScope.launch {
            selectedPage = page
            currentMaterial = null
            materialAbout = null
            materialAboutLoadState = MaterialAboutLoadState.Loading
            derivedOpticalConstants = null
            fresnelResult = null
            materialLoadState = MaterialLoadState.Loading
            clearPlots()
            materialRepository.load(page).fold(
                onSuccess = { material ->
                    currentMaterial = material
                    updateOpticalPlots()
                    updateDerivedOpticalConstants()
                    updateFresnel()
                    materialLoadState = MaterialLoadState.Loaded
                },
                onFailure = { throwable ->
                    materialLoadState = MaterialLoadState.Failed(
                        throwable.message ?: "Could not load this material."
                    )
                }
            )
        }
        viewModelScope.launch {
            materialAboutRepository.load(page).fold(
                onSuccess = { about ->
                    if (selectedPage == page) {
                        materialAbout = about
                        materialAboutLoadState = if (about == null) {
                            MaterialAboutLoadState.Unavailable
                        } else {
                            MaterialAboutLoadState.Loaded
                        }
                    }
                },
                onFailure = {
                    if (selectedPage == page) materialAboutLoadState = MaterialAboutLoadState.Unavailable
                }
            )
        }
    }

    fun selectBook(book: Book) {
        if (selectedBook != book) {
            selectedPage = null
            currentMaterial = null
            materialAbout = null
            materialAboutLoadState = MaterialAboutLoadState.Idle
            materialLoadState = MaterialLoadState.Idle
            clearPlots()
            derivedOpticalConstants = null
            fresnelResult = null
        }
        selectedBook = book
    }

    private fun clearSelection() {
        selectedShelf = null
        selectedBook = null
        selectedPage = null
        currentMaterial = null
        materialAbout = null
        materialAboutLoadState = MaterialAboutLoadState.Idle
        materialLoadState = MaterialLoadState.Idle
        derivedOpticalConstants = null
        fresnelResult = null
        clearPlots()
    }

    fun selectShelf(shelf: Shelf) {
        if (selectedShelf != shelf) {
            selectedBook = null
            selectedPage = null
            currentMaterial = null
            materialAbout = null
            materialAboutLoadState = MaterialAboutLoadState.Idle
            materialLoadState = MaterialLoadState.Idle
            clearPlots()
            derivedOpticalConstants = null
            fresnelResult = null
        }
        selectedShelf = shelf
    }

    private fun updateOpticalPlots() {
        clearPlots()
        currentMaterial?.let {
            it.dispersionModel?.let { model ->
                val wavelengths = model.wavelengthArray(1000)
                dispersionPlotManager.addPlot(
                    SeriesPlot.linePlot(
                        dataSeries = DataSeries(wavelengths.toList(), model.refractiveIndex(wavelengths).toList()),
                        name = "dispersion model",
                        color = Color(0xFF1565C0)
                    )
                )
            }
            it.tabulatedData?.let { tabulated ->
                tabulated.nArray?.let { values ->
                    dispersionPlotManager.addPlot(
                        SeriesPlot.dashedPlot(
                            DataSeries(tabulated.wavelengthArray, values),
                            name = "tabulated n",
                            color = Color(0xFF00897B)
                        )
                    )
                }
                tabulated.kArray?.let { values ->
                    extinctionPlotManager.addPlot(
                        SeriesPlot.dashedPlot(
                            DataSeries(tabulated.wavelengthArray, values),
                            name = "tabulated k",
                            color = Color(0xFFAD1457)
                        )
                    )
                }
            }
        }
    }

    private fun clearPlots() {
        dispersionPlotManager.clearPlot()
        extinctionPlotManager.clearPlot()
    }

    fun updateDerivedWavelength(value: String) {
        derivedWavelengthText = value
        updateDerivedOpticalConstants()
        updateFresnel()
    }

    fun updateFresnelAngle(value: String) {
        fresnelAngleText = value
        updateFresnel()
    }

    private fun updateDerivedOpticalConstants() {
        val wavelength = derivedWavelengthText.replace(',', '.').toDoubleOrNull()
        if (wavelength == null || !wavelength.isFinite() || wavelength <= 0.0) {
            derivedWavelengthError = "Enter a positive wavelength in µm"
            derivedOpticalConstants = null
            return
        }
        derivedWavelengthError = null
        derivedOpticalConstants = currentMaterial?.let {
            DerivedOpticalConstantsCalculator.calculate(it, wavelength)
        }
    }

    private fun updateFresnel() {
        val wavelength = derivedWavelengthText.replace(',', '.').toDoubleOrNull()
        val angle = fresnelAngleText.replace(',', '.').toDoubleOrNull()
        if (wavelength == null || !wavelength.isFinite() || wavelength <= 0.0) {
            fresnelResult = null
            return
        }
        if (angle == null || !angle.isFinite() || angle !in 0.0..<90.0) {
            fresnelAngleError = "Enter an angle from 0° to below 90°"
            fresnelResult = null
            return
        }
        fresnelAngleError = null
        fresnelResult = currentMaterial?.let {
            FresnelCalculator.calculate(it, wavelength, angle)
        }
    }
}

class MainViewModelFactory(
    private val fallbackCatalogue: Catalogue,
    private val databaseRevision: DatabaseRevision = DatabaseRevision.Latest,
    private val materialRepository: MaterialRepository = RemoteMaterialRepository(revision = databaseRevision),
    private val materialAboutRepository: MaterialAboutRepository = RemoteMaterialAboutRepository(revision = databaseRevision),
    private val catalogueRepository: CatalogueRepository = RemoteCatalogueRepository(),
    private val settingsRepository: SettingsRepository = InMemorySettingsRepository()
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(
                fallbackCatalogue = fallbackCatalogue,
                materialRepository = materialRepository,
                materialAboutRepository = materialAboutRepository,
                catalogueRepository = catalogueRepository,
                databaseRevision = databaseRevision,
                settingsRepository = settingsRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
