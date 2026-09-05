package com.example.refractiveindexapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.refractiveindexapp.parsing.MaterialModel
import com.example.refractiveindexapp.parsing.MaterialRepository
import com.example.refractiveindexapp.parsing.CatalogueSnapshot
import com.example.refractiveindexapp.parsing.CatalogueSnapshotSource
import com.example.refractiveindexapp.parsing.CatalogueSnapshotRepository
import com.example.refractiveindexapp.parsing.DatabaseRevision
import com.example.refractiveindexapp.parsing.TabulatedData
import com.example.refractiveindexapp.ui.components.MainScreen
import com.example.refractiveindexapp.ui.view.MainViewModel
import com.example.refractiveindexapp.ui.view.MaterialLoadState
import com.example.refractiveindexapp.ui.view.CatalogueLoadState
import com.example.refractiveindexapp.utils.loadCatalogue
import com.example.refractiveindexapp.settings.DatabaseVersionPolicy
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
            curatedSnapshot = CatalogueSnapshot(
                catalogue,
                DatabaseRevision.Commit(DatabaseRevision.Curated.gitRef),
                CatalogueSnapshotSource.Bundled
            ),
            materialRepository = SelectedMaterialRepository
        )

        viewModel.selectShelf(shelf)
        viewModel.selectBook(book)
        viewModel.selectPage(page)

        composeTestRule.setContent {
            MainScreen(viewModel = viewModel, onAddMaterial = {}, onAbout = {}, onSettings = {})
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.materialLoadState == MaterialLoadState.Loaded
        }

        composeTestRule.onNodeWithText("main  /  BaB2O4  /  Tamosauskas-e").assertIsDisplayed()
        composeTestRule.onNodeWithText("Derived optical constants").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun specificCommitAppliesImmediatelyAndMaterialUsesItsRevision() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val catalogue = loadCatalogue(context)
        val page = catalogue.entries.single { it.id == "main" }
            .content.orEmpty().single { it.id == "BaB2O4" }
            .content.orEmpty().single { it.id == "Tamosauskas-e" }
        val curated = CatalogueSnapshot(
            catalogue,
            DatabaseRevision.Commit(DatabaseRevision.Curated.gitRef),
            CatalogueSnapshotSource.Bundled
        )
        val requestedRevision = DatabaseRevision.Commit("0123456")
        val revisionsUsedForMaterials = mutableListOf<DatabaseRevision>()
        val viewModel = MainViewModel(
            curatedSnapshot = curated,
            materialRepository = object : MaterialRepository {
                override suspend fun load(page: com.example.refractiveindexapp.parsing.Page, revision: DatabaseRevision): Result<MaterialModel> {
                    revisionsUsedForMaterials += revision
                    return Result.success(MaterialModel())
                }
            },
            catalogueSnapshotRepository = object : CatalogueSnapshotRepository {
                override suspend fun load(
                    revision: DatabaseRevision.Commit,
                    protectedRevision: DatabaseRevision.Commit?
                ) = Result.success(CatalogueSnapshot(catalogue, revision, CatalogueSnapshotSource.Cache))
            }
        )

        viewModel.setDatabaseCommit(requestedRevision.sha)
        viewModel.setDatabaseVersionPolicy(DatabaseVersionPolicy.SpecificCommit)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.catalogueLoadState == CatalogueLoadState.Loaded
        }
        viewModel.selectPage(page)
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            revisionsUsedForMaterials.singleOrNull() == requestedRevision
        }
    }

    private object SelectedMaterialRepository : MaterialRepository {
        override suspend fun load(
            page: com.example.refractiveindexapp.parsing.Page,
            revision: DatabaseRevision
        ): Result<MaterialModel> =
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
