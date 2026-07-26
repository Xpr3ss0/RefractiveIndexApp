package com.example.refractiveindexapp.parsing

import org.yaml.snakeyaml.Yaml
class CatalogueParser() {

    fun parse(text: String): Catalogue {

        val yaml = Yaml()

        @Suppress("UNCHECKED_CAST")
        val root = yaml.load<List<Map<String, Any>>>(text)

        val catalogue = Catalogue(
            parseShelfNodes(root)
        )
        return catalogue

    }


    private fun parseShelfNodes(nodes: List<Map<String, Any>>): List<Shelf> {
        var currentDivider: Divider? = null
        val shelfList = mutableListOf<Shelf>()

        nodes.forEach {
            when {
                "DIVIDER" in it -> {
                    currentDivider = Divider(it["DIVIDER"] as String)
                }

                "SHELF" in it -> {
                    val shelf = Shelf(
                        id = it["SHELF"] as String,
                        name = it["name"] as String,
                        divider = currentDivider
                    )
                    @Suppress("UNCHECKED_CAST")
                    shelf.content = parseBookNodes(it["content"] as List<Map<String, Any>>, shelf)
                    shelfList.add(shelf)
                }

                else ->
                    error("Unknown shelf-level node: $it")
            }
        }
        return shelfList.toList()
    }

    private fun parseBookNodes(nodes: List<Map<String, Any>>, parentShelf: Shelf): List<Book> {
        var currentDivider: Divider? = null
        val bookList = mutableListOf<Book>()

        nodes.forEach {
            when {
                "DIVIDER" in it -> {
                    currentDivider = Divider(it["DIVIDER"] as String)
                }

                "BOOK" in it -> {
                    val book = Book(
                        id = it["BOOK"] as String,
                        name = it["name"] as String,
                        parentShelf = parentShelf,
                        divider = currentDivider
                    )
                    @Suppress("UNCHECKED_CAST")
                    book.content = parsePageNodes(it["content"] as List<Map<String, Any>>, book)
                    bookList.add(book)
                }

                else ->
                    error("Unknown book-level node: $it")
            }
        }
        return bookList.toList()
    }

    private fun parsePageNodes(nodes: List<Map<String, Any>>, parentBook: Book): List<Page> {
        var currentDivider: Divider? = null
        val pageList = mutableListOf<Page>()

        nodes.forEach {
            when {
                "DIVIDER" in it -> {
                    currentDivider = Divider(it["DIVIDER"] as String)
                }

                "PAGE" in it -> {
                    pageList.add(
                        Page(
                            id = it["PAGE"] as String,
                            name = it["name"] as String,
                            dataPath = it["data"] as String,
                            parentBook = parentBook,
                            divider = currentDivider
                        )
                    )
                }

                else ->
                    error("Unknown page-level node: $it")
            }
        }
        return pageList.toList()
    }
}