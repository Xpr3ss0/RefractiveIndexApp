package com.example.refractiveindexapp.utils

import android.content.Context
import com.example.refractiveindexapp.parsing.Catalogue
import com.example.refractiveindexapp.parsing.CatalogueParser
import com.example.refractiveindexapp.parsing.CatalogueSnapshot
import com.example.refractiveindexapp.parsing.CatalogueSnapshotSource
import com.example.refractiveindexapp.parsing.DatabaseRevision
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL


fun loadCatalogue(context: Context): Catalogue {

    val text = AssetLoader.load(
        context = context,
        filename = "catalog-nk.yml"
    )

    return CatalogueParser().parse(text)
}

fun loadCuratedCatalogueSnapshot(context: Context): CatalogueSnapshot = CatalogueSnapshot(
    catalogue = loadCatalogue(context),
    revision = DatabaseRevision.Commit(DatabaseRevision.Curated.gitRef),
    source = CatalogueSnapshotSource.Bundled
)

suspend fun downloadTempFile(context: Context, fileUrl: String, prefix: String = "temp_download", suffix: String = ".tmp"): File? {

    return withContext(Dispatchers.IO) {

        try {
            val tempFile = File.createTempFile(
                prefix,
                suffix,
                context.cacheDir
            )

            URL(fileUrl).openStream().use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            tempFile

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

object AssetLoader {
    fun load(context: Context, filename: String): String =
        context.assets
            .open(filename)
            .bufferedReader()
            .use { it.readText() }
}
