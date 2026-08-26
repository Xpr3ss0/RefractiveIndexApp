package com.example.refractiveindexapp

import com.example.refractiveindexapp.parsing.MaterialModel
import com.example.refractiveindexapp.parsing.TabulatedData
import com.example.refractiveindexapp.physics.FresnelCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.atan
import kotlin.math.asin

class FresnelCalculatorTest {
    private fun material(n: Double, k: Double? = null) = MaterialModel(
        tabulatedData = TabulatedData(
            type = if (k == null) "tabulated n" else "tabulated nk",
            content = "test",
            wavelengthArray = listOf(0.5),
            nArray = listOf(n),
            kArray = k?.let(::listOf)
        )
    )

    @Test
    fun `normal-incidence dielectric matches Fresnel reflectance and characteristic angles`() {
        val result = FresnelCalculator.calculate(material(1.5), 0.5, 0.0)

        assertEquals(0.04, result.reflectanceP.value!!, 1e-10)
        assertEquals(0.04, result.reflectanceS.value!!, 1e-10)
        assertEquals(atan(1.5) * 180.0 / Math.PI, result.brewsterAngleDegrees.value!!, 1e-10)
        assertEquals(asin(1.0 / 1.5) * 180.0 / Math.PI, result.reverseCriticalAngleDegrees.value!!, 1e-10)
    }

    @Test
    fun `p reflectance vanishes at Brewster angle for a lossless material`() {
        val angle = atan(1.5) * 180.0 / Math.PI
        val result = FresnelCalculator.calculate(material(1.5), 0.5, angle)

        assertEquals(0.0, result.reflectanceP.value!!, 1e-10)
        assertEquals(null, result.reflectanceS.unavailableReason)
    }

    @Test
    fun `absorbing material returns a Brewster approximation with warning`() {
        val result = FresnelCalculator.calculate(material(1.5, 0.2), 0.5, 30.0)

        assertTrue(result.reflectanceP.isAvailable)
        assertTrue(result.brewsterAngleDegrees.isAvailable)
        assertEquals(atan(1.5) * 180.0 / Math.PI, result.brewsterAngleDegrees.value!!, 1e-10)
        assertTrue(result.brewsterAngleWarning?.contains("lossless approximation") == true)
        assertTrue(result.reverseCriticalAngleDegrees.isAvailable)
        assertEquals(asin(1.0 / 1.5) * 180.0 / Math.PI, result.reverseCriticalAngleDegrees.value!!, 1e-10)
        assertTrue(result.criticalAngleWarning?.contains("lossless approximation") == true)
    }

    @Test
    fun `near-zero extinction does not warn for Brewster angle`() {
        val result = FresnelCalculator.calculate(material(1.5, 1e-6), 0.5, 30.0)

        assertTrue(result.brewsterAngleDegrees.isAvailable)
        assertEquals(null, result.brewsterAngleWarning)
        assertTrue(result.reverseCriticalAngleDegrees.isAvailable)
        assertEquals(null, result.criticalAngleWarning)
    }

    @Test
    fun `right angle is rejected`() {
        val result = FresnelCalculator.calculate(material(1.5), 0.5, 90.0)

        assertFalse(result.reflectanceP.isAvailable)
    }
}
