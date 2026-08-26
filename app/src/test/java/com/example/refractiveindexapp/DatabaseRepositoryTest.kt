package com.example.refractiveindexapp

import com.example.refractiveindexapp.parsing.CatalogueParser
import com.example.refractiveindexapp.parsing.DatabaseRevision
import com.example.refractiveindexapp.parsing.RefractiveIndexDatabase
import com.example.refractiveindexapp.parsing.RemoteCatalogueRepository
import com.example.refractiveindexapp.settings.AppSettings
import com.example.refractiveindexapp.settings.InMemorySettingsRepository
import com.example.refractiveindexapp.ui.view.CatalogueLoadState
import com.example.refractiveindexapp.ui.view.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.runBlocking

class DatabaseRepositoryTest {

    @Test
    fun `latest revision uses upstream main branch`() {
        assertEquals(
            "https://raw.githubusercontent.com/polyanskiy/refractiveindex.info-database/main/database/catalog-nk.yml",
            RefractiveIndexDatabase.catalogueUrl()
        )
    }

    @Test
    fun `commit revision is used for catalogue and material URLs`() {
        val revision = DatabaseRevision.Commit("0123456789abcdef")
        assertTrue(RefractiveIndexDatabase.catalogueUrl(revision).contains("/0123456789abcdef/"))
        assertTrue(
            RefractiveIndexDatabase.materialUrl("main/SiO2/nk/Malitson.yml", revision)
                .contains("/0123456789abcdef/")
        )
        assertEquals(
            "https://raw.githubusercontent.com/polyanskiy/refractiveindex.info-database/0123456789abcdef/database/data/main/SiO2/about.yml",
            RefractiveIndexDatabase.materialAboutUrl("main/SiO2/nk/Malitson.yml", revision)
        )
    }

    @Test
    fun `remote catalogue repository parses downloaded catalogue`() = runBlocking {
        val repository = RemoteCatalogueRepository(
            parser = CatalogueParser(),
            downloader = {
                """
                - SHELF: main
                  name: Main
                  content: []
                """.trimIndent()
            }
        )

        val result = repository.load(DatabaseRevision.Commit("0123456"))

        assertTrue(result.isSuccess)
        assertEquals("main", result.getOrThrow().entries.single().id)
    }

    @Test
    fun `disabled startup update leaves bundled catalogue ready for selection`() {
        val catalogue = CatalogueParser().parse(
            """
            - SHELF: main
              name: Main
              content: []
            """.trimIndent()
        )

        val viewModel = MainViewModel(
            fallbackCatalogue = catalogue,
            catalogueRepository = RemoteCatalogueRepository(),
            settingsRepository = InMemorySettingsRepository(
                AppSettings(updateCatalogueOnStartup = false)
            )
        )

        assertEquals(CatalogueLoadState.Ready, viewModel.catalogueLoadState)
        assertEquals("main", viewModel.catalogue.entries.single().id)
    }
}
