package com.example.refractiveindexapp

import com.example.refractiveindexapp.parsing.CatalogueParser
import com.example.refractiveindexapp.parsing.MaterialGatherer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.File

class MaterialParserTest {

    @Test
    fun parseMaterial() {

        val textCatalogue = File("src/test/resources/catalog-nk.yml").readText()

        val catalogue = CatalogueParser().parse(textCatalogue)
        val parser = MaterialGatherer(catalogue)

        val page = parser.gatherSBP(shelfName = "main", bookName = "SiO2", pageName = "Nyakuchena")

        assertNotNull(page)
        assertEquals("Nyakuchena", page?.id)
        assertEquals("main/SiO2/nk/Nyakuchena.yml", page?.dataPath)

    }
}
