package com.example.refractiveindexapp.parsing

sealed interface CatalogueEntry

data class Divider(
    val title: String
)

data class Shelf(
    val id: String,
    val name: String,
    val divider: Divider?,
    var content: List<Book>? = null
) : CatalogueEntry

data class Book(
    val id: String,
    val name: String,
    val parentShelf: Shelf,
    val divider: Divider?,
    var content: List<Page>? = null,
) : CatalogueEntry


data class Page(
    val id: String,
    val name: String,
    val dataPath: String,
    val parentBook: Book,
    val divider: Divider?
) : CatalogueEntry

data class Catalogue(
    val entries: List<Shelf>
)