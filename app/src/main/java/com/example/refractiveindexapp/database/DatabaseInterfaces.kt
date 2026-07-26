package com.example.refractiveindexapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface MaterialsDao {
    /*
    Interface that provides access to the local material database.
    AS by the intended use case, materials will be accessed only on material at a time.
    */

    @Insert
    fun addMaterials(vararg materials: Material)

    @Delete
    fun removeMaterials(vararg materials: Material)

    @Update
    fun updateMaterials(vararg materials: Material)

    @Query("SELECT * from materials WHERE material_id = :materialID")
    fun getMaterialFromID(materialID: Int): Material?

    @Query("SELECT * from materials WHERE database_path = :databasePath")
    fun getMaterialFromPath(databasePath: String): Material?

    @Query("SELECT * from materials WHERE shelf = :shelf AND book = :book and page = :page")
    fun getMaterialFromSBP(shelf: String, book: String, page: String): Material?

}

@Dao
interface MaterialNamesDao {
    /*
    Interface for the name containing different aliases of materials.
    */

    @Insert
    fun addMaterialNames(vararg names: MaterialName)

    @Delete
    fun removeMaterialNames(vararg names: MaterialName)

    @Update
    fun updateMaterialNames(vararg names: MaterialName)

    @Query("SELECT * from material_names WHERE material_id = :materialID")
    fun getMaterialNamesFromID(materialID: Int): List<Material>

}

@Dao
interface TabulatedValuesDao {
    /*
    Interface for accessing the tabulated n,k values.
     */
    @Insert
    fun addTabulatedValues(vararg values: TabulatedValue)

    @Delete
    fun removeTabulatedValues(vararg values: TabulatedValue)

    @Update
    fun updateTabulatedValues(vararg values: TabulatedValue)

    @Query("SELECT * from tabulated_values WHERE material_id = :materialID")
    fun getValuesFromID(materialID: Int): List<TabulatedValue>
}

@Dao
interface CoefficientsDao {
    /*
    Interface for obtaining coefficients for dispersion equations from the database.
     */
    @Insert
    fun addCoefficients(vararg coefficients: Coefficient)

    @Delete
    fun removeMaterials(vararg coefficients: Coefficient)

    @Update
    fun updateMaterials(vararg coefficients: Coefficient)

    @Query("SELECT * from dispersion_coefficients WHERE material_id = :materialID AND equation_type = :equationType")
    fun getCoefficientsFromTypeAndID(materialID: Int, equationType: Int): List<Coefficient>
}