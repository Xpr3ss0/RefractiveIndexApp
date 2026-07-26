package com.example.refractiveindexapp.ui.view

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.example.refractiveindexapp.parsing.Catalogue
import com.example.refractiveindexapp.parsing.Shelf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.refractiveindexapp.database.Material
import com.example.refractiveindexapp.parsing.Book
import com.example.refractiveindexapp.parsing.MaterialFileModel
import com.example.refractiveindexapp.parsing.MaterialGatherer
import com.example.refractiveindexapp.parsing.Page
import kotlinx.coroutines.launch

class MainViewModel(val catalogue: Catalogue) : ViewModel() {

    var selectedShelf by mutableStateOf<Shelf?>(null)
        private set

    var selectedBook by mutableStateOf<Book?>(null)
        private set

    var selectedPage by mutableStateOf<Page?>(null)
        private set

    // material
    var currentMaterial by mutableStateOf<MaterialFileModel?>(null)

    fun selectPage(page: Page) {
        viewModelScope.launch {
            selectedPage = page
            currentMaterial = MaterialGatherer(catalogue).pullPageData(page)
        }
    }

    fun selectBook(book: Book) {
        selectedBook = book
    }

    fun selectShelf(shelf: Shelf) {
        selectedShelf = shelf
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