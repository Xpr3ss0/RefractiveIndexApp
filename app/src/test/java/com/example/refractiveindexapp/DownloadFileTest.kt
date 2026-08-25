package com.example.refractiveindexapp

import com.example.refractiveindexapp.parsing.downloadFileDirectly
import org.junit.Ignore
import org.junit.Test

@Ignore("Network integration check; run manually rather than as a JVM unit test.")
class DownloadFileTest {

    @Test
    fun downloadFile() {
        val databasePath = "main/Ag/nk/Johnson.yml"
        val url = "https://raw.githubusercontent.com/polyanskiy/refractiveindex.info-database/master/database/data/$databasePath"
        val file = downloadFileDirectly(url)
        val content = file?.readText()

        println(content)
        file?.delete()
    }
}
