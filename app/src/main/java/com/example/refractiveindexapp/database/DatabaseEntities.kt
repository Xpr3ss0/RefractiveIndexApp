package com.example.refractiveindexapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "materials")
data class Material(
    /*
    Entry of the main table for materials. Contains all core parameters for each available material.
    */

    // basic material info
    @ColumnInfo(name = "name") val name: String? = null,
    @PrimaryKey @ColumnInfo(name = "database_path") val databasePath: String,

    // database location
    @ColumnInfo(name = "shelf") val shelf: String,
    @ColumnInfo(name = "book") val book: String,
    @ColumnInfo(name = "page") val page: String,

    // source info
    @ColumnInfo(name = "references") val references: String?,
    @ColumnInfo(name = "comments") val comments: String?,

    // conditions info
    @ColumnInfo(name = "temperature") val temperature: Double?,
    @ColumnInfo(name = "direction") val direction: String?,
    @ColumnInfo(name = "pressure") val pressure: Double?,


    // data info
    @ColumnInfo(name = "wavelength_min") val wavelengthMin: Double?,
    @ColumnInfo(name = "wavelength_max") val wavelengthMax: Double?,
    @ColumnInfo(name = "equation_type") val equationType: Int?,
    @ColumnInfo(name = "is_tabulated") val isTabulated: Boolean
)

@Entity(tableName = "material_names")
data class MaterialName(
    /*
    Entry of table containing aliases for materials. Each material can appear multiple times.
     */

    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "name_id") val nameID: Int,
    @ColumnInfo(name = "database_path") val databasePath: String,
    @ColumnInfo(name = "alias") val name: String
)

@Entity(tableName = "tabulated_values")
data class TabulatedValue(
    /*
    Entry of containing tabulated n and k values for materials. Each entry is a single data point, but all materials
    with tabulated values are contained in this table.
     */
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "value_index") val valueID: Int,
    @ColumnInfo(name = "database_path") val databasePath: String,
    @ColumnInfo(name = "wavelength_nm") val wavelength: Double,
    @ColumnInfo(name = "n") val n: Double,
    @ColumnInfo(name = "k") val k: Double,
)

@Entity(tableName = "dispersion_coefficients")
data class Coefficient(
    /*
    Entry of table holding coefficients for dispersion formulas.

    Find reference to the formulas and coefficient indices here:
    https://github.com/polyanskiy/refractiveindex.info-database/blob/main/database/doc/Dispersion%20formulas.pdf
     */
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "coefficient_id") val coefficientID: Int,
    @ColumnInfo(name = "database_path") val databasePath: String,
    @ColumnInfo(name = "equation_type") val equationType: Int,
    @ColumnInfo(name = "coefficient_index") val coefficientIndex: Int,
    @ColumnInfo(name = "coefficient_value") val coefficientValue: Double
)
