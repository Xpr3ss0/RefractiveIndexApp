package com.example.refractiveindexapp.parsing

import org.yaml.snakeyaml.Yaml

/** Shared, material-level metadata from the database's optional `about.yml`. */
data class MaterialAbout(
    val names: List<String> = emptyList(),
    val description: String? = null,
    val links: List<MaterialAboutLink> = emptyList()
)

data class MaterialAboutLink(
    val url: String,
    val text: String? = null
)

class MaterialAboutParser {
    fun parse(text: String?): MaterialAbout? {
        if (text == null) return null

        @Suppress("UNCHECKED_CAST")
        val root = Yaml().load<Map<String, Any?>>(text) ?: return null
        val names = (root["NAMES"] as? List<*>)
            .orEmpty()
            .mapNotNull { it as? String }
        val links = (root["LINKS"] as? List<*>)
            .orEmpty()
            .mapNotNull { item ->
                val link = item as? Map<*, *> ?: return@mapNotNull null
                val url = link["url"] as? String ?: return@mapNotNull null
                MaterialAboutLink(url = url, text = link["text"] as? String)
            }

        return MaterialAbout(
            names = names,
            description = root["ABOUT"] as? String,
            links = links
        ).takeIf { it.names.isNotEmpty() || it.description != null || it.links.isNotEmpty() }
    }
}

/** Boundary between the UI and a material's optional shared description. */
interface MaterialAboutRepository {
    suspend fun load(page: Page): Result<MaterialAbout?>
}

class RemoteMaterialAboutRepository(
    private val parser: MaterialAboutParser = MaterialAboutParser(),
    private val revision: DatabaseRevision = DatabaseRevision.Latest,
    private val downloader: suspend (String) -> String? = ::downloadText
) : MaterialAboutRepository {
    private val cache = mutableMapOf<String, Result<MaterialAbout?>>()

    override suspend fun load(page: Page): Result<MaterialAbout?> {
        val url = RefractiveIndexDatabase.materialAboutUrl(page.dataPath, revision)
        return cache.getOrPut(url) {
            runCatching {
                // An about file is optional, so a missing file simply means no section to show.
                downloader(url)?.let(parser::parse)
            }
        }
    }
}
