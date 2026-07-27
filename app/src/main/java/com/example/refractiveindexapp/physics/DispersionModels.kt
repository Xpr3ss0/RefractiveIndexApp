package com.example.refractiveindexapp.physics

import kotlin.math.sqrt
import kotlin.math.pow

interface DispersionModel {

    val coefficients: DoubleArray

    val wavelengthMin: Double
    val wavelengthMax: Double

    fun refractiveIndex(wavelength: Double): Double

    fun refractiveIndex(wavelengths: DoubleArray): DoubleArray {
        return DoubleArray(wavelengths.size) { i ->
            refractiveIndex(wavelengths[i])
        }
    }
    fun wavelengthArray(points: Int = 1000) : DoubleArray {
        val step = (wavelengthMax - wavelengthMin) / (points - 1)
        return DoubleArray(points) { i ->
            wavelengthMin + i * step
        }
    }
}

abstract class BaseDispersionModel(
    rawCoefficients: DoubleArray,
    coefficientCount: Int
) : DispersionModel {

    override val coefficients =
        DoubleArray(coefficientCount).apply {
            rawCoefficients.copyInto(this)
        }
}

object DispersionModelFactory {
    fun create(type: Int, coefficients: DoubleArray, wavelengthMin: Double, wavelengthMax: Double): DispersionModel? {
        return when(type) {
            1 -> Sellmeier(coefficients, wavelengthMin, wavelengthMax)
            2 -> Sellmeier2(coefficients, wavelengthMin, wavelengthMax)
            3 -> Polynomial(coefficients, wavelengthMin, wavelengthMax)
            4 -> RefractiveIndexINFO(coefficients, wavelengthMin, wavelengthMax)
            5 -> Cauchy(coefficients, wavelengthMin, wavelengthMax)
            6 -> Gases(coefficients, wavelengthMin, wavelengthMax)
            7 -> Herzberger(coefficients, wavelengthMin, wavelengthMax)
            8 -> Retro(coefficients, wavelengthMin, wavelengthMax)
            9 -> Exotic(coefficients, wavelengthMin, wavelengthMax)
            else -> null
        }
    }
}

class Sellmeier(
    coefficients: DoubleArray,
    override val wavelengthMin: Double,
    override val wavelengthMax: Double,
) : BaseDispersionModel(
    coefficients,
    17
) {

    override fun refractiveIndex(wavelength: Double): Double {
        var res = coefficients[0]
        val lmd2 = wavelength * wavelength
        for (i in 2..16 step 2) {
            res += coefficients[i] * lmd2 / (lmd2 - coefficients[i+1]*coefficients[i+1])
        }
        val n2 = res + 1
        return sqrt(n2)
    }
}

class Sellmeier2(
    coefficients: DoubleArray,
    override val wavelengthMin: Double,
    override val wavelengthMax: Double,
) : BaseDispersionModel(
    coefficients,
    17
) {

    override fun refractiveIndex(wavelength: Double): Double {
        var res = coefficients[0]
        val lmd2 = wavelength * wavelength
        for (i in 1..15 step 2) {
            res += coefficients[i] * lmd2 / (lmd2 - coefficients[i+1])
        }
        val n2 = res + 1
        return sqrt(n2)
    }
}

class Polynomial(
    coefficients: DoubleArray,
    override val wavelengthMin: Double,
    override val wavelengthMax: Double,
) : BaseDispersionModel(
    coefficients,
    17
) {

    override fun refractiveIndex(wavelength: Double): Double {
        var res = coefficients[0]
        for (i in 1..15 step 2) {
            res += coefficients[i] * wavelength.pow(coefficients[i+1])
        }
        val n2 = res
        return sqrt(n2)
    }
}

class RefractiveIndexINFO(
    coefficients: DoubleArray,
    override val wavelengthMin: Double,
    override val wavelengthMax: Double,
) : BaseDispersionModel(
    coefficients,
    17
) {

    override fun refractiveIndex(wavelength: Double): Double {
        var res = coefficients[0]
        val lmd2 = wavelength.pow(2)
        for (i in 1..5 step 4) {
            res += coefficients[i] * wavelength.pow(coefficients[i+1]) / (lmd2 - coefficients[i+2].pow(coefficients[i+3]))
        }
        for (i in 9..15 step 2) {
            res += coefficients[i] * wavelength.pow(coefficients[i+1])
        }
        val n2 = res
        return sqrt(n2)
    }
}

class Cauchy(
    coefficients: DoubleArray,
    override val wavelengthMin: Double,
    override val wavelengthMax: Double,
) : BaseDispersionModel(
    coefficients,
    11
) {

    override fun refractiveIndex(wavelength: Double): Double {
        var res = coefficients[0]
        for (i in 1..9 step 2) {
            res += coefficients[i] * wavelength.pow(coefficients[i+1])
        }
        return res
    }
}

class Gases(
    coefficients: DoubleArray,
    override val wavelengthMin: Double,
    override val wavelengthMax: Double,
) : BaseDispersionModel(
    coefficients,
    11
) {

    override fun refractiveIndex(wavelength: Double): Double {
        var res = coefficients[0]
        for (i in 1..15 step 2) {
            res += coefficients[i] / (coefficients[i+1] - wavelength.pow(-2))
        }
        val n = res + 1
        return n
    }
}

class Herzberger(
    coefficients: DoubleArray,
    override val wavelengthMin: Double,
    override val wavelengthMax: Double,
) : BaseDispersionModel(
    coefficients,
    6
) {

    override fun refractiveIndex(wavelength: Double): Double {
        var res = coefficients[0]
        val lmd2 = wavelength.pow(2)
        res += coefficients[1] / (lmd2 - 0.028)
        res += coefficients[2] * (1 / lmd2 - 0.028).pow(2)

        for (i in 3..5) {
            res += coefficients[i] * wavelength.pow((i - 3) * 2)
        }
        return res
    }
}

class Retro(
    coefficients: DoubleArray,
    override val wavelengthMin: Double,
    override val wavelengthMax: Double,
) : BaseDispersionModel(
    coefficients,
    4
) {

    override fun refractiveIndex(wavelength: Double): Double {
        var res = coefficients[0]
        val lmd2 = wavelength.pow(2)
        res += coefficients[1] * lmd2 / (lmd2 - coefficients[2])
        res += coefficients[3] * lmd2
        val n2 = (2*res + 1) / (1 - res)
        return sqrt(n2)
    }
}

class Exotic(
    coefficients: DoubleArray,
    override val wavelengthMin: Double,
    override val wavelengthMax: Double,
) : BaseDispersionModel(
    coefficients,
    6
) {

    override fun refractiveIndex(wavelength: Double): Double {
        val lmd2 = wavelength.pow(2)
        var res = coefficients[0]
        res += coefficients[1] / (lmd2 - coefficients[2])
        res += coefficients[3] * (wavelength - coefficients[4]) / ((wavelength - coefficients[4]).pow(2) + coefficients[5])
        val n2 = res
        return sqrt(n2)
    }
}