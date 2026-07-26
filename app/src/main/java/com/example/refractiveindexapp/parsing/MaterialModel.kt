package com.example.refractiveindexapp.parsing

data class MaterialFileModel(
    val references: String? = null,
    val comments: String? = null,
    val conditions: Conditions? = null,

    // data
    val tabulatedData: TabulatedData? = null,

    // coefficients
    val dispersionModel: DispersionModel? = null

)
data class TabulatedData (
    val type: String,
    val content: String
)

data class DispersionModel (
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

