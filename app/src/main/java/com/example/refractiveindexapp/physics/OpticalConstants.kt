package com.example.refractiveindexapp.physics

import com.example.refractiveindexapp.parsing.MaterialModel
import com.example.refractiveindexapp.parsing.TabulatedData
import kotlin.math.PI

data class DerivedValue(val value: Double? = null, val unavailableReason: String? = null) {
    val isAvailable: Boolean get() = value != null

    companion object {
        fun unavailable(reason: String) = DerivedValue(unavailableReason = reason)
    }
}

data class DerivedOpticalConstants(
    val wavelengthMicrometres: Double,
    val refractiveIndex: DerivedValue,
    val extinctionCoefficient: DerivedValue,
    val epsilon1: DerivedValue,
    val epsilon2: DerivedValue,
    val absorptionCoefficientCmInverse: DerivedValue,
    val abbeNumber: DerivedValue,
    val chromaticDispersionPerMicrometre: DerivedValue,
    val groupIndex: DerivedValue,
    val groupVelocityDispersionFsSquaredPerMm: DerivedValue,
    val dispersionPsPerNmKm: DerivedValue
)

/** Resolves optical data from the representations currently supported by the database parser. */
class OpticalDataProvider private constructor(
    private val dispersionModel: DispersionModel?,
    private val tabulatedData: TabulatedData?
) {
    companion object {
        fun from(material: MaterialModel) = OpticalDataProvider(material.dispersionModel, material.tabulatedData)
    }

    fun refractiveIndexAt(wavelength: Double): DerivedValue = when {
        dispersionModel != null -> modelValue(wavelength)
        else -> interpolatedValue(tabulatedData?.wavelengthArray, tabulatedData?.nArray, wavelength, "No refractive-index data")
    }

    fun extinctionCoefficientAt(wavelength: Double): DerivedValue =
        interpolatedValue(tabulatedData?.wavelengthArray, tabulatedData?.kArray, wavelength, "No extinction-coefficient data")

    fun firstDerivativeAt(wavelength: Double): DerivedValue = when (val model = dispersionModel) {
        null -> tabulatedSlope(wavelength)
        else -> finiteDifference(wavelength, model, secondDerivative = false)
    }

    fun secondDerivativeAt(wavelength: Double): DerivedValue = dispersionModel?.let {
        finiteDifference(wavelength, it, secondDerivative = true)
    } ?: DerivedValue.unavailable("GVD requires a dispersion formula")

    fun hasFormulaModel(): Boolean = dispersionModel != null

    private fun modelValue(wavelength: Double): DerivedValue {
        val model = dispersionModel ?: return DerivedValue.unavailable("No dispersion model")
        if (wavelength !in model.wavelengthMin..model.wavelengthMax) {
            return DerivedValue.unavailable("Outside formula range")
        }
        val value = model.refractiveIndex(wavelength)
        return if (value.isFinite()) DerivedValue(value) else DerivedValue.unavailable("Formula is undefined here")
    }

    private fun finiteDifference(wavelength: Double, model: DispersionModel, secondDerivative: Boolean): DerivedValue {
        val margin = minOf(wavelength - model.wavelengthMin, model.wavelengthMax - wavelength)
        if (margin <= 0.0) return DerivedValue.unavailable("Too close to formula range boundary")
        val h = minOf(wavelength * 1e-3, margin / 3.0).coerceAtLeast(1e-7)
        if (wavelength - 2 * h < model.wavelengthMin || wavelength + 2 * h > model.wavelengthMax) {
            return DerivedValue.unavailable("Too close to formula range boundary")
        }
        val fm2 = model.refractiveIndex(wavelength - 2 * h)
        val fm1 = model.refractiveIndex(wavelength - h)
        val f0 = model.refractiveIndex(wavelength)
        val fp1 = model.refractiveIndex(wavelength + h)
        val fp2 = model.refractiveIndex(wavelength + 2 * h)
        val value = if (secondDerivative) {
            (-fp2 + 16 * fp1 - 30 * f0 + 16 * fm1 - fm2) / (12 * h * h)
        } else {
            (fm2 - 8 * fm1 + 8 * fp1 - fp2) / (12 * h)
        }
        return if (value.isFinite()) DerivedValue(value) else DerivedValue.unavailable("Formula derivative is undefined here")
    }

    private fun tabulatedSlope(wavelength: Double): DerivedValue {
        val x = tabulatedData?.wavelengthArray ?: return DerivedValue.unavailable("No refractive-index data")
        val y = tabulatedData.nArray ?: return DerivedValue.unavailable("No refractive-index data")
        val segment = segmentAt(x, wavelength) ?: return DerivedValue.unavailable("Outside tabulated range")
        return DerivedValue((y[segment + 1] - y[segment]) / (x[segment + 1] - x[segment]))
    }

    private fun interpolatedValue(x: List<Double>?, y: List<Double>?, wavelength: Double, absentReason: String): DerivedValue {
        if (x == null || y == null || x.size != y.size || x.isEmpty()) return DerivedValue.unavailable(absentReason)
        if (x.size == 1) return if (wavelength == x.first()) DerivedValue(y.first()) else DerivedValue.unavailable("Outside tabulated range")
        val segment = segmentAt(x, wavelength) ?: return DerivedValue.unavailable("Outside tabulated range")
        val fraction = (wavelength - x[segment]) / (x[segment + 1] - x[segment])
        return DerivedValue(y[segment] + fraction * (y[segment + 1] - y[segment]))
    }

    private fun segmentAt(x: List<Double>, wavelength: Double): Int? {
        if (x.size < 2 || wavelength !in x.first()..x.last()) return null
        if (wavelength == x.last()) return x.lastIndex - 1
        for (index in 0 until x.lastIndex) {
            if (wavelength in x[index]..x[index + 1]) return index
        }
        return null
    }
}

object DerivedOpticalConstantsCalculator {
    private const val C_LINE = 0.6562725
    private const val D_LINE = 0.5875618
    private const val F_LINE = 0.4861327
    private const val SPEED_OF_LIGHT_UM_PER_FS = 0.299792458

    fun calculate(material: MaterialModel, wavelengthMicrometres: Double): DerivedOpticalConstants {
        val provider = OpticalDataProvider.from(material)
        if (!wavelengthMicrometres.isFinite() || wavelengthMicrometres <= 0.0) {
            val invalid = DerivedValue.unavailable("Enter a positive wavelength")
            return DerivedOpticalConstants(wavelengthMicrometres, invalid, invalid, invalid, invalid, invalid, invalid, invalid, invalid, invalid, invalid)
        }
        val n = provider.refractiveIndexAt(wavelengthMicrometres)
        val k = provider.extinctionCoefficientAt(wavelengthMicrometres)
        val complexAvailable = n.value != null && k.value != null
        val epsilon1 = if (complexAvailable) DerivedValue(n.value!! * n.value!! - k.value!! * k.value!!) else DerivedValue.unavailable("Requires n and k")
        val epsilon2 = if (complexAvailable) DerivedValue(2 * n.value!! * k.value!!) else DerivedValue.unavailable("Requires n and k")
        val absorption = if (complexAvailable) DerivedValue(4 * PI * k.value!! * 1e4 / wavelengthMicrometres) else DerivedValue.unavailable("Requires n and k")

        val firstDerivative = provider.firstDerivativeAt(wavelengthMicrometres)
        val groupIndex = if (n.value != null && firstDerivative.value != null) {
            DerivedValue(n.value!! - wavelengthMicrometres * firstDerivative.value!!)
        } else DerivedValue.unavailable(firstDerivative.unavailableReason ?: "Requires refractive-index derivative")

        val abbe = if (provider.hasFormulaModel()) {
            val nC = provider.refractiveIndexAt(C_LINE).value
            val nD = provider.refractiveIndexAt(D_LINE).value
            val nF = provider.refractiveIndexAt(F_LINE).value
            if (nC != null && nD != null && nF != null) DerivedValue((nD - 1) / (nF - nC)) else DerivedValue.unavailable("Formula does not cover C, d and F lines")
        } else DerivedValue.unavailable("Abbe number requires a dispersion formula")

        val secondDerivative = provider.secondDerivativeAt(wavelengthMicrometres)
        val gvd = secondDerivative.value?.let { derivative ->
            DerivedValue(wavelengthMicrometres * wavelengthMicrometres * wavelengthMicrometres * derivative /
                (2 * PI * SPEED_OF_LIGHT_UM_PER_FS * SPEED_OF_LIGHT_UM_PER_FS) * 1_000)
        } ?: DerivedValue.unavailable(secondDerivative.unavailableReason ?: "GVD unavailable")
        val d = gvd.value?.let { gvdValue ->
            DerivedValue(-2 * PI * SPEED_OF_LIGHT_UM_PER_FS * gvdValue / (wavelengthMicrometres * wavelengthMicrometres))
        } ?: DerivedValue.unavailable(gvd.unavailableReason ?: "GVD unavailable")

        return DerivedOpticalConstants(wavelengthMicrometres, n, k, epsilon1, epsilon2, absorption, abbe, firstDerivative, groupIndex, gvd, d)
    }
}
