package com.example.refractiveindexapp.parsing
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL



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

suspend fun downloadText(fileUrl: String): String? =
    withContext(Dispatchers.IO) {
        try {
            URL(fileUrl)
                .openStream()
                .bufferedReader()
                .use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

fun downloadFileDirectly(fileUrl: String): File? {
    return try {
        // Creates a temp file in computer's local temp directory
        val tempFile = File.createTempFile("jvm_download_", ".tmp")

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

