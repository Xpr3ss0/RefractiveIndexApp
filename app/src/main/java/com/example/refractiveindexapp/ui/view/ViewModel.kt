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
import com.example.refractiveindexapp.parsing.MaterialGatherer
import com.example.refractiveindexapp.parsing.Page
import dev.xpr3ss0.scientificplot.model.DataSeries
import dev.xpr3ss0.scientificplot.model.PlotStyle
import dev.xpr3ss0.scientificplot.model.SeriesPlot
import dev.xpr3ss0.scientificplot.state.PlotManager
import dev.xpr3ss0.scientificplot.state.PlotState
import kotlinx.coroutines.launch

class MainViewModel(val catalogue: Catalogue) : ViewModel() {

    var selectedShelf by mutableStateOf<Shelf?>(null)
        private set

    var selectedBook by mutableStateOf<Book?>(null)
        private set

    var selectedPage by mutableStateOf<Page?>(null)
        private set

    // material
    var currentMaterial by mutableStateOf<MaterialModel?>(null)

    // plotting data
    var wavelengthPlotData by mutableStateOf<DoubleArray?>(null)
    var indexPlotData by mutableStateOf<DoubleArray?>(null)

    val plotManager = PlotManager(PlotState.defaultFromEmpty())

    fun selectPage(page: Page) {
        viewModelScope.launch {
            selectedPage = page
            currentMaterial = MaterialGatherer(catalogue).pullPageData(page)
            updateRefractiveIndexPlot()
            if (currentMaterial != null && currentMaterial!!.dispersionModel != null) {
                wavelengthPlotData = currentMaterial!!.dispersionModel!!.wavelengthArray()
                indexPlotData = currentMaterial!!.dispersionModel!!.refractiveIndex(wavelengthPlotData!!)
            }
            else {
                wavelengthPlotData = null
                indexPlotData = null
            }
        }
    }

    fun selectBook(book: Book) {
        selectedBook = book
    }

    fun selectShelf(shelf: Shelf) {
        selectedShelf = shelf
    }

    fun updateRefractiveIndexPlot() {
        plotManager.clearPlot()
        currentMaterial?.let {
            when  {
                it.dispersionModel != null -> {
                    val lmdModel = it.dispersionModel.wavelengthArray(1000)
                    val nModel = it.dispersionModel.refractiveIndex(lmdModel)
                    val plot = SeriesPlot.linePlot(
                        dataSeries = DataSeries(lmdModel.toList(), nModel.toList()),
                        name = "dispersion model",
                        color = Color.Blue
                    )
                    plotManager.addPlot(plot)
                }
                it.tabulatedData != null -> {
                    val lmdTab = it.tabulatedData.wavelengthArray
                    val nTab = it.tabulatedData.nArray
                    val kTab = it.tabulatedData.kArray
                    if (nTab != null) {
                        val nSeries = DataSeries(lmdTab, nTab)
                        val nPlot = SeriesPlot.dashedPlot(nSeries, "tabulated n")
                        plotManager.addPlot(nPlot)
                    }
                    if (kTab != null) {
                        val kSeries = DataSeries(lmdTab, kTab)
                        val kPlot = SeriesPlot.dashedPlot(kSeries, "tabulated k")
                        plotManager.addPlot(kPlot)
                    }
                }
            }
        }
    }
}

class MainViewModelFactory(
    private val catalogue: Catalogue
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(catalogue) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}