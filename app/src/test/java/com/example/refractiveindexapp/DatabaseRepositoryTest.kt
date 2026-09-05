package com.example.refractiveindexapp

import com.example.refractiveindexapp.parsing.CatalogueParser
import com.example.refractiveindexapp.parsing.CatalogueSnapshotSource
import com.example.refractiveindexapp.parsing.CatalogueSnapshotStore
import com.example.refractiveindexapp.parsing.DatabaseRevision
import com.example.refractiveindexapp.parsing.PersistentCatalogueSnapshotRepository
import com.example.refractiveindexapp.parsing.RefractiveIndexDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class DatabaseRepositoryTest {
    private val catalogueText = """
        - SHELF: main
          name: Main
          content: []
    """.trimIndent()

    @Test
    fun `default catalogue URL uses curated revision`() {
        assertTrue(RefractiveIndexDatabase.catalogueUrl().contains("/${RefractiveIndexDatabase.CuratedCommitSha}/"))
        assertEquals(RefractiveIndexDatabase.CuratedCommitSha, DatabaseRevision.Curated.gitRef)
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
    fun `persistent repository uses a matching cached catalogue without downloading`() = runBlocking {
        val directory = Files.createTempDirectory("catalogue-cache-test").toFile()
        val revision = DatabaseRevision.Commit("0123456")
        val store = CatalogueSnapshotStore(directory)
        store.write(revision, catalogueText)
        var downloads = 0
        val repository = PersistentCatalogueSnapshotRepository(store) {
            downloads++
            catalogueText
        }

        val result = repository.load(revision)

        assertTrue(result.isSuccess)
        assertEquals(CatalogueSnapshotSource.Cache, result.getOrThrow().source)
        assertEquals(0, downloads)
    }

    @Test
    fun `corrupt cache is replaced by a validated download`() = runBlocking {
        val directory = Files.createTempDirectory("catalogue-cache-test").toFile()
        val revision = DatabaseRevision.Commit("0123456")
        val store = CatalogueSnapshotStore(directory)
        store.write(revision, catalogueText)
        directory.resolve("${revision.sha}.yml").writeText("not valid: [yaml")
        val repository = PersistentCatalogueSnapshotRepository(store) { catalogueText }

        val result = repository.load(revision)

        assertTrue(result.isSuccess)
        assertEquals(CatalogueSnapshotSource.Remote, result.getOrThrow().source)
        assertNotNull(store.read(revision))
    }

    @Test
    fun `store retains at most ten remote snapshots`() {
        val directory = Files.createTempDirectory("catalogue-cache-test").toFile()
        val store = CatalogueSnapshotStore(directory, CatalogueParser())
        repeat(11) { index ->
            store.write(DatabaseRevision.Commit("%07x".format(index + 1)), catalogueText)
        }

        assertTrue(directory.listFiles { file -> file.name.endsWith(".meta") }!!.size <= 10)
        assertFalse(directory.resolve("0000001.yml").exists())
    }
}
