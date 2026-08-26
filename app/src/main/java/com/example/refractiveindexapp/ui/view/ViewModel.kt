package com.example.refractiveindexapp.ui.view

import androidx.compose.runtime.mutableStateOf
import com.example.refractiveindexapp.parsing.Catalogue
import com.example.refractiveindexapp.parsing.Shelf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.refractiveindexapp.parsing.Book
import com.example.refractiveindexapp.parsing.MaterialModel
import com.example.refractiveindexapp.parsing.MaterialRepository
import com.example.refractiveindexapp.parsing.Page
import com.example.refractiveindexapp.parsing.RemoteMaterialRepository
import com.example.refractiveindexapp.physics.DerivedOpticalConstants
import com.example.refractiveindexapp.physics.DerivedOpticalConstantsCalculator
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

class MainViewModel(
    val catalogue: Catalogue,
    private val materialRepository: MaterialRepository = RemoteMaterialRepository()
) : ViewModel() {

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

    var derivedWavelengthText by mutableStateOf("0.5876")
        private set

    var derivedWavelengthError by mutableStateOf<String?>(null)
        private set

    var derivedOpticalConstants by mutableStateOf<DerivedOpticalConstants?>(null)
        private set

    val dispersionPlotManager = PlotManager(PlotState.defaultFromEmpty()).apply {
        setAxisLabels(xLabel = "Wavelength (µm)", yLabel = "Refractive index")
    }
    val extinctionPlotManager = PlotManager(PlotState.defaultFromEmpty()).apply {
        setAxisLabels(xLabel = "Wavelength (µm)", yLabel = "Extinction coefficient k")
    }

    fun selectPage(page: Page) {
        viewModelScope.launch {
            selectedPage = page
            currentMaterial = null
            derivedOpticalConstants = null
            materialLoadState = MaterialLoadState.Loading
            clearPlots()
            materialRepository.load(page).fold(
                onSuccess = { material ->
                    currentMaterial = material
                    updateOpticalPlots()
                    updateDerivedOpticalConstants()
                    materialLoadState = MaterialLoadState.Loaded
                },
                onFailure = { throwable ->
                    materialLoadState = MaterialLoadState.Failed(
                        throwable.message ?: "Could not load this material."
                    )
                }
            )
        }
    }

    fun selectBook(book: Book) {
        if (selectedBook != book) {
            selectedPage = null
            currentMaterial = null
            materialLoadState = MaterialLoadState.Idle
            clearPlots()
            derivedOpticalConstants = null
        }
        selectedBook = book
    }

    fun selectShelf(shelf: Shelf) {
        if (selectedShelf != shelf) {
            selectedBook = null
            selectedPage = null
            currentMaterial = null
            materialLoadState = MaterialLoadState.Idle
            clearPlots()
            derivedOpticalConstants = null
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
}

class MainViewModelFactory(
    private val catalogue: Catalogue,
    private val materialRepository: MaterialRepository = RemoteMaterialRepository()
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(catalogue, materialRepository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
