package com.example.refractiveindexapp

import com.example.refractiveindexapp.parsing.MaterialParser
import com.example.refractiveindexapp.physics.DerivedOpticalConstants
import com.example.refractiveindexapp.physics.DerivedOpticalConstantsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.yaml.snakeyaml.Yaml
import java.io.File
import kotlin.math.abs

class DerivedOpticalConstantsTest {
    @Test
    fun `derived constants agree with local refractiveindex website references`() {
        @Suppress("UNCHECKED_CAST")
        val references = Yaml().load<List<Map<String, Any>>>(
            File("src/test/resources/derived-constants/website-reference.yml").readText()
        )

        references.forEach { reference ->
            val material = MaterialParser().parse(
                File("src/test/resources/derived-constants/${reference.getValue("source")}").readText()
            )!!
            val result = DerivedOpticalConstantsCalculator.calculate(material, (reference.getValue("wavelength") as Number).toDouble())
            @Suppress("UNCHECKED_CAST")
            val expected = reference.getValue("expected") as Map<String, Number>
            expected.forEach { (metric, value) ->
                assertReferenceValue(metric, value.toDouble(), result)
            }
            @Suppress("UNCHECKED_CAST")
            (reference["unavailable"] as? List<String>).orEmpty().forEach { metric ->
                assertFalse("${reference.getValue("id")} $metric should be unavailable", metricValue(metric, result).isAvailable)
            }
        }
    }

    @Test
    fun `invalid wavelength returns explicit unavailable values`() {
        val material = MaterialParser().parse(File("src/test/resources/derived-constants/sio2-arosa.yml").readText())!!
        val result = DerivedOpticalConstantsCalculator.calculate(material, 0.0)
        assertFalse(result.refractiveIndex.isAvailable)
        assertNotNull(result.refractiveIndex.unavailableReason)
    }

    private fun assertReferenceValue(metric: String, expected: Double, result: DerivedOpticalConstants) {
        val actual = metricValue(metric, result).value
        assertNotNull("$metric should be available", actual)
        val tolerance = maxOf(abs(expected) * 0.015, 2e-4)
        assertEquals(metric, expected, actual!!, tolerance)
    }

    private fun metricValue(metric: String, result: DerivedOpticalConstants) = when (metric) {
        "epsilon1" -> result.epsilon1
        "epsilon2" -> result.epsilon2
        "absorption" -> result.absorptionCoefficientCmInverse
        "abbe" -> result.abbeNumber
        "dn_d_lambda" -> result.chromaticDispersionPerMicrometre
        "group_index" -> result.groupIndex
        "gvd" -> result.groupVelocityDispersionFsSquaredPerMm
        "d" -> result.dispersionPsPerNmKm
        else -> error("Unknown metric $metric")
    }
}
