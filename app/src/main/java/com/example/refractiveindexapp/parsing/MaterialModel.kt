package com.example.refractiveindexapp.parsing

import com.example.refractiveindexapp.physics.DispersionModel

data class MaterialModel(
    val references: String? = null,
    val comments: String? = null,
    val conditions: Conditions? = null,

    // data
    val tabulatedData: TabulatedData? = null,

    // coefficients
    val dispersionData: DispersionData? = null,
    val dispersionModel: DispersionModel? = null

)
data class TabulatedData (
    val type: String,
    val content: String,
    val wavelengthArray: List<Double>,
    val nArray: List<Double>? = null,
    val kArray: List<Double>? = null
)

data class DispersionData (
    val coefficients: String,
    val formulaType: Int,
    val wavelengthRange: String
)


data class Conditions(
    val temperature: Double? = null,
    val pressure: Double? = null,

    // e or o
    val direction: String? = null
)

