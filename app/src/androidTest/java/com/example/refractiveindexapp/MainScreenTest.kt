package com.example.refractiveindexapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.refractiveindexapp.parsing.MaterialModel
import com.example.refractiveindexapp.parsing.MaterialRepository
import com.example.refractiveindexapp.parsing.TabulatedData
import com.example.refractiveindexapp.ui.components.MainScreen
import com.example.refractiveindexapp.ui.view.MainViewModel
import com.example.refractiveindexapp.ui.view.MaterialLoadState
import com.example.refractiveindexapp.utils.loadCatalogue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun startsMainScreenWithTamosauskasExtraordinaryMaterialSelected() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val catalogue = loadCatalogue(context)
        val shelf = catalogue.entries.single { it.id == "main" }
        val book = shelf.content.orEmpty().single { it.id == "BaB2O4" }
        // The catalogue/database spelling is Tamosauskas-e (not Tomasauskas-e).
        val page = book.content.orEmpty().single { it.id == "Tamosauskas-e" }
        val viewModel = MainViewModel(
            catalogue = catalogue,
            materialRepository = SelectedMaterialRepository
        )

        viewModel.selectShelf(shelf)
        viewModel.selectBook(book)
        viewModel.selectPage(page)

        composeTestRule.setContent {
            MainScreen(viewModel = viewModel, onAddMaterial = {})
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.materialLoadState == MaterialLoadState.Loaded
        }

        composeTestRule.onNodeWithText("main  /  BaB2O4  /  Tamosauskas-e").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dispersion").assertIsDisplayed()
        composeTestRule.onNodeWithText("Extinction coefficient").assertIsDisplayed()
    }

    private object SelectedMaterialRepository : MaterialRepository {
        override suspend fun load(page: com.example.refractiveindexapp.parsing.Page): Result<MaterialModel> =
            Result.success(
                MaterialModel(
                    references = "Tamošauskas et al. 2018",
                    tabulatedData = TabulatedData(
                        type = "tabulated nk",
                        content = "instrumented-test fixture",
                        wavelengthArray = listOf(0.188, 0.25, 0.5, 1.0),
                        nArray = listOf(1.68, 1.66, 1.60, 1.55),
                        kArray = listOf(0.004, 0.002, 0.001, 0.0005)
                    )
                )
            )
    }
}
