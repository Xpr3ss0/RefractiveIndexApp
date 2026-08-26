package com.example.refractiveindexapp.physics

import com.example.refractiveindexapp.parsing.MaterialModel
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.math.sqrt

data class FresnelResult(
    val wavelengthMicrometres: Double,
    val incidenceAngleDegrees: Double,
    val reflectanceP: DerivedValue,
    val reflectanceS: DerivedValue,
    val reflectanceUnpolarized: DerivedValue,
    val phasePDegrees: DerivedValue,
    val phaseSDegrees: DerivedValue,
    val brewsterAngleDegrees: DerivedValue,
    val reverseCriticalAngleDegrees: DerivedValue
)

private data class Complex(val real: Double, val imaginary: Double) {
    operator fun plus(other: Complex) = Complex(real + other.real, imaginary + other.imaginary)
    operator fun minus(other: Complex) = Complex(real - other.real, imaginary - other.imaginary)
    operator fun times(other: Complex) = Complex(
        real * other.real - imaginary * other.imaginary,
        real * other.imaginary + imaginary * other.real
    )
    operator fun div(other: Complex): Complex {
        val denominator = other.real * other.real + other.imaginary * other.imaginary
        return Complex(
            (real * other.real + imaginary * other.imaginary) / denominator,
            (imaginary * other.real - real * other.imaginary) / denominator
        )
    }
    operator fun times(scale: Double) = Complex(real * scale, imaginary * scale)
    fun magnitudeSquared() = real * real + imaginary * imaginary
    fun phaseDegrees() = atan2(imaginary, real) * 180.0 / PI
    fun squareRoot(): Complex {
        val magnitude = hypot(real, imaginary)
        val rootReal = sqrt((magnitude + real) / 2.0)
        val rootImaginary = if (imaginary < 0.0) -sqrt((magnitude - real) / 2.0) else sqrt((magnitude - real) / 2.0)
        return if (rootReal < 0.0) Complex(-rootReal, -rootImaginary) else Complex(rootReal, rootImaginary)
    }
}

object FresnelCalculator {
    private const val LOSSLESS_K_TOLERANCE = 1e-10

    fun calculate(material: MaterialModel, wavelengthMicrometres: Double, incidenceAngleDegrees: Double): FresnelResult {
        if (!wavelengthMicrometres.isFinite() || wavelengthMicrometres <= 0.0) {
            return unavailable(wavelengthMicrometres, incidenceAngleDegrees, "Enter a positive wavelength")
        }
        if (!incidenceAngleDegrees.isFinite() || incidenceAngleDegrees !in 0.0..<90.0) {
            return unavailable(wavelengthMicrometres, incidenceAngleDegrees, "Enter an angle from 0° to below 90°")
        }
        val provider = OpticalDataProvider.from(material)
        val nValue = provider.refractiveIndexAt(wavelengthMicrometres)
        val n = nValue.value ?: return unavailable(wavelengthMicrometres, incidenceAngleDegrees, nValue.unavailableReason ?: "No refractive-index data")
        val k = provider.extinctionCoefficientAt(wavelengthMicrometres).value ?: 0.0
        val targetIndex = Complex(n, k)
        val incidentAngle = incidenceAngleDegrees * PI / 180.0
        val cosIncident = cos(incidentAngle)
        val sinIncident = sin(incidentAngle)
        val sinTransmitted = Complex(sinIncident, 0.0) / targetIndex
        val cosTransmitted = (Complex(1.0, 0.0) - sinTransmitted * sinTransmitted).squareRoot()
        val incidentCosine = Complex(cosIncident, 0.0)
        val rs = (incidentCosine - targetIndex * cosTransmitted) / (incidentCosine + targetIndex * cosTransmitted)
        val rp = (targetIndex * incidentCosine - cosTransmitted) / (targetIndex * incidentCosine + cosTransmitted)
        val reflectanceP = DerivedValue(rp.magnitudeSquared())
        val reflectanceS = DerivedValue(rs.magnitudeSquared())
        val lossless = abs(k) <= LOSSLESS_K_TOLERANCE
        val brewster = if (lossless && n > 0.0) DerivedValue(atan(n) * 180.0 / PI) else DerivedValue.unavailable("Brewster angle requires a lossless material")
        val critical = if (!lossless) {
            DerivedValue.unavailable("Critical angle requires a lossless material")
        } else if (n < 1.0) {
            DerivedValue.unavailable("Material index must be at least 1")
        } else {
            DerivedValue(asin(1.0 / n) * 180.0 / PI)
        }
        return FresnelResult(
            wavelengthMicrometres,
            incidenceAngleDegrees,
            reflectanceP,
            reflectanceS,
            DerivedValue((reflectanceP.value!! + reflectanceS.value!!) / 2.0),
            DerivedValue(rp.phaseDegrees()),
            DerivedValue(rs.phaseDegrees()),
            brewster,
            critical
        )
    }

    private fun unavailable(wavelength: Double, angle: Double, reason: String): FresnelResult {
        val unavailable = DerivedValue.unavailable(reason)
        return FresnelResult(wavelength, angle, unavailable, unavailable, unavailable, unavailable, unavailable, unavailable, unavailable)
    }
}
