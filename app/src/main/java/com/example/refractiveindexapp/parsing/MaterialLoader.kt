package com.example.refractiveindexapp.parsing

import com.example.refractiveindexapp.physics.DispersionModel
import com.example.refractiveindexapp.physics.DispersionModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.yaml.snakeyaml.Yaml

class MaterialParser {


    fun parse(text: String?): MaterialModel? {

        if (text == null) {
            return null
        }

        val yaml = Yaml()

        @Suppress("UNCHECKED_CAST")
        val root = yaml.load<Map<String, Any>>(text)

        var tabulatedData: TabulatedData? = null
        var dispersionData: DispersionData? = null
        var dispersionModel: DispersionModel? = null

        if ("DATA" in root) {
            @Suppress("UNCHECKED_CAST")
            val data = root["DATA"] as List<Map<String, Any>>
            data.forEach {
                val type: String = it["type"] as String

                when {
                    type.startsWith("formula") -> {
                        dispersionData = DispersionData(
                            coefficients = it["coefficients"] as String,
                            formulaType = type.last().digitToInt(),
                            wavelengthRange = it["wavelength_range"] as String
                        )
                        dispersionModel = DispersionModelFactory.create(
                            type = dispersionData.formulaType,
                            coefficients = parseDoubleArray(dispersionData.coefficients))
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

        return MaterialModel(
            references = root["REFERENCES"] as? String,
            comments = root["COMMENTS"] as? String,
            conditions = parseConditions(root["CONDITIONS"]),
            tabulatedData = tabulatedData,
            dispersionData = dispersionData,
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
    private fun parseDoubleArray(value: String): DoubleArray {

        return value
            .trim()
            .split(Regex("\\s+"))
            .map { it.toDouble() }
            .toDoubleArray()
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

    suspend fun pullPageData(page: Page) : MaterialModel? =
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