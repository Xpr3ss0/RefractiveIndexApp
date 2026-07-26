package com.example.refractiveindexapp

import com.example.refractiveindexapp.parsing.CatalogueParser
import com.example.refractiveindexapp.parsing.Divider
import com.example.refractiveindexapp.parsing.Shelf
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class CatalogueParserTest {

    @Test
    fun parsesCatalogue() {

        val text = File("src/test/resources/catalog-nk.yml")
            .readText()

        val catalogue = CatalogueParser().parse(text)

        println("Number of top-level entries: ${catalogue.entries.size}")

        catalogue.entries.forEach {
            println("Shelf: ${it.id}, name: ${it.name}")
        }

    }
}