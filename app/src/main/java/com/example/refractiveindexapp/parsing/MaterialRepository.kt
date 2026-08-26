package com.example.refractiveindexapp.parsing

/** Boundary between the UI and the refractiveindex.info data source. */
interface MaterialRepository {
    suspend fun load(page: Page): Result<MaterialModel>
}

class RemoteMaterialRepository(
    private val parser: MaterialParser = MaterialParser(),
    private val revision: DatabaseRevision = DatabaseRevision.Latest,
    private val downloader: suspend (String) -> String? = ::downloadText
) : MaterialRepository {
    override suspend fun load(page: Page): Result<MaterialModel> = runCatching {
        val text = downloader(RefractiveIndexDatabase.materialUrl(page.dataPath, revision))
            ?: error("Could not download this material.")
        parser.parse(text) ?: error("The material file could not be read.")
    }
}
