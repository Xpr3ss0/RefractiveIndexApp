package com.example.refractiveindexapp

import com.example.refractiveindexapp.database.Material
import com.example.refractiveindexapp.parsing.CatalogueParser
import com.example.refractiveindexapp.parsing.MaterialGatherer
import com.example.refractiveindexapp.parsing.MaterialFileModel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class MaterialParserTest {

    @Test
    fun parseMaterial() {

        val textCatalogue = File("src/test/resources/catalog-nk.yml").readText()

        val catalogue = CatalogueParser().parse(textCatalogue)
        val parser = MaterialGatherer(catalogue)

        val material : Material? = parser.gatherSBP(shelfName = "main", bookName = "SiO2", pageName = "Nyakuchena")

        println("Number of shelves in catalog: ${catalogue.entries.size}")
        println()

        assert(material!=null)

        println("material comments: ${material?.comments}")
        println("material references: ${material?.references}")
        println("material is tabulated: ${material?.isTabulated}")
        println("material equation type: ${material?.equationType}")
        println("material temperature: ${material?.temperature} K")

    }
}