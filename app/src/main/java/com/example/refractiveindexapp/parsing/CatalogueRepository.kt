package com.example.refractiveindexapp.parsing

/** Downloads and parses a refractiveindex.info catalogue revision. */
interface CatalogueRepository {
    suspend fun load(
        revision: DatabaseRevision = DatabaseRevision.Latest
    ): Result<Catalogue>
}

class RemoteCatalogueRepository(
    private val parser: CatalogueParser = CatalogueParser(),
    private val downloader: suspend (String) -> String? = ::downloadText
) : CatalogueRepository {
    override suspend fun load(revision: DatabaseRevision): Result<Catalogue> = runCatching {
        val text = downloader(RefractiveIndexDatabase.catalogueUrl(revision))
            ?: error("Could not download the material catalogue.")
        parser.parse(text)
    }
}
