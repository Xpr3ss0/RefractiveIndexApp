package com.example.refractiveindexapp.parsing

import android.content.Context
import com.example.refractiveindexapp.database.Coefficient
import com.example.refractiveindexapp.database.Material
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.yaml.snakeyaml.Yaml

class MaterialParser {


    fun parse(text: String?): MaterialFileModel? {

        if (text == null) {
            return null
        }

        val yaml = Yaml()

        @Suppress("UNCHECKED_CAST")
        val root = yaml.load<Map<String, Any>>(text)

        var tabulatedData: TabulatedData? = null
        var dispersionModel: DispersionModel? = null

        if ("DATA" in root) {
            @Suppress("UNCHECKED_CAST")
            val data = root["DATA"] as List<Map<String, Any>>
            data.forEach {
                val type: String = it["type"] as String

                when {
                    type.startsWith("formula") -> {
                        dispersionModel = DispersionModel(
                            coefficients = it["coefficients"] as String,
                            formulaType = type.last().digitToInt(),
                            wavelengthRange = it["wavelength_range"] as String
                        )
                    }
                    type.startsWith("tabulated") -> {
                        tabulatedData = TabulatedData(
                            type = type,
                            content = it["data"] as String
                        )
                    }
                }

            }
        }

        return MaterialFileModel(
            references = root["REFERENCES"] as? String,
            comments = root["COMMENTS"] as? String,
            conditions = parseConditions(root["CONDITIONS"]),
            tabulatedData = tabulatedData,
            dispersionModel = dispersionModel
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun parseConditions(node: Any?): Conditions? {

        val map = node as? Map<String, Any> ?: return null

        return Conditions(
            temperature = (map["temperature"] as? Number)?.toDouble(),
            pressure = (map["pressure"] as? Number)?.toDouble(),
            direction = map["direction"] as? String
        )
    }

    /**
     * Parses values like
     *
     * "0.32 2.5"
     *
     * or
     *
     * "0 1.31 0.009 ..."
     */
    private fun parseDoubleList(value: Any?): List<Double>? {

        val string = value as? String ?: return null

        return string
            .trim()
            .split(Regex("\\s+"))
            .mapNotNull { it.toDoubleOrNull() }
    }
}

class MaterialGatherer(val catalogue: Catalogue) {

    fun gatherSBP(shelfName: String, bookName: String, pageName: String) : Page? {

        // get page from specified info
        val shelf = getShelf(catalogue.entries, shelfName) ?: return null
        val bookList = shelf.content ?: return null
        val book = getBook(bookList, bookName) ?: return null
        val pageList = book.content ?: return null
        return getPage(pageList, pageName)


    }

    suspend fun pullPageData(page: Page) : MaterialFileModel? =
        withContext(Dispatchers.IO) {
            // get material yml file and parse
            val url = "https://raw.githubusercontent.com/polyanskiy/refractiveindex.info-database/master/database/data/${page.dataPath}"

            // eventually use different function to download via android context
            val content = downloadText(url)
            MaterialParser().parse(content)
        }


    private fun getShelf(shelfList: List<Shelf>, shelfName: String) : Shelf? {
        shelfList.forEach {
            if (it.id == shelfName) {
                return it
            }
        }
        println("Cannot find shelf $shelfName.")
        return null

    }
    private fun getBook(bookList: List<Book>, bookName: String) : Book? {
        bookList.forEach {
            if (it.id == bookName) {
                return it
            }
        }
        println("Cannot book shelf $bookName.")
        return null
    }
    private fun getPage(getPage: List<Page>, pageName: String) : Page? {
        getPage.forEach {
            if (it.id == pageName) {
                return it
            }
        }
        println("Cannot find page $pageName.")
        return null
    }
}