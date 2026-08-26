package com.example.refractiveindexapp

import com.example.refractiveindexapp.parsing.MaterialAboutParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MaterialAboutParserTest {
    @Test
    fun `parses names html description and links`() {
        val about = MaterialAboutParser().parse(
            """
            NAMES:
              - Barium borate
              - BaB<sub>2</sub>O<sub>4</sub>
            ABOUT: |
              A nonlinear optical crystal.
            LINKS:
              - url: https://example.org/bbo
                text: BBO reference
            """.trimIndent()
        )

        assertNotNull(about)
        assertEquals(listOf("Barium borate", "BaB<sub>2</sub>O<sub>4</sub>"), about?.names)
        assertEquals("A nonlinear optical crystal.\n", about?.description)
        assertEquals("https://example.org/bbo", about?.links?.single()?.url)
        assertEquals("BBO reference", about?.links?.single()?.text)
    }
}
