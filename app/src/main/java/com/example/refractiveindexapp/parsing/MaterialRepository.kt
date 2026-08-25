package com.example.refractiveindexapp.parsing

/** Boundary between the UI and the refractiveindex.info data source. */
interface MaterialRepository {
    suspend fun load(page: Page): Result<MaterialModel>
}

class RemoteMaterialRepository(
    private val parser: MaterialParser = MaterialParser()
) : MaterialRepository {
    override suspend fun load(page: Page): Result<MaterialModel> = runCatching {
        val url = "https://raw.githubusercontent.com/polyanskiy/refractiveindex.info-database/master/database/data/${page.dataPath}"
        val text = downloadText(url) ?: error("Could not download this material.")
        parser.parse(text) ?: error("The material file could not be read.")
    }
}
