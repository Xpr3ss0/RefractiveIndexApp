package com.example.refractiveindexapp.parsing

/** A reproducible upstream database revision. */
sealed interface DatabaseRevision {
    val gitRef: String

    /** The snapshot curated and bundled with this app release. */
    data object Curated : DatabaseRevision {
        override val gitRef: String = RefractiveIndexDatabase.CuratedCommitSha
    }

    /** An immutable Git commit SHA from the refractiveindex.info database. */
    data class Commit(val sha: String) : DatabaseRevision {
        init {
            require(sha.matches(Regex("[0-9a-fA-F]{7,64}"))) {
                "A database commit must be a 7 to 64 character hexadecimal Git SHA."
            }
        }

        override val gitRef: String = sha
    }
}

object RefractiveIndexDatabase {
    const val CuratedCommitSha = "c5c2f188e848453def5970e347399d653df2ffc2"
    private const val RawDatabaseBaseUrl =
        "https://raw.githubusercontent.com/polyanskiy/refractiveindex.info-database"

    fun catalogueUrl(revision: DatabaseRevision = DatabaseRevision.Curated): String =
        "$RawDatabaseBaseUrl/${revision.gitRef}/database/catalog-nk.yml"

    fun materialUrl(
        dataPath: String,
        revision: DatabaseRevision = DatabaseRevision.Curated
    ): String = "$RawDatabaseBaseUrl/${revision.gitRef}/database/data/$dataPath"

    /**
     * Returns the `about.yml` shared by all optical-data pages for a material.
     *
     * Page files live below a data-kind directory, for example
     * `main/BaB2O4/nk/Tamosauskas-e.yml`; the associated description lives at
     * `main/BaB2O4/about.yml`.
     */
    fun materialAboutUrl(
        dataPath: String,
        revision: DatabaseRevision = DatabaseRevision.Curated
    ): String {
        val dataKindDirectory = dataPath.substringBeforeLast('/', missingDelimiterValue = "")
        require(dataKindDirectory.isNotEmpty()) { "A material data path must include a file name." }
        val materialDirectory = dataKindDirectory.substringBeforeLast('/', missingDelimiterValue = "")
        require(materialDirectory.isNotEmpty()) { "A material data path must include a data-kind directory." }
        return "$RawDatabaseBaseUrl/${revision.gitRef}/database/data/$materialDirectory/about.yml"
    }

    val currentCommitUrl: String =
        "https://api.github.com/repos/polyanskiy/refractiveindex.info-database/commits/main"
}

class DatabaseRevisionResolver(
    private val downloader: suspend (String) -> String? = ::downloadText
) {
    suspend fun currentCommit(): Result<DatabaseRevision.Commit> = runCatching {
        val response = downloader(RefractiveIndexDatabase.currentCommitUrl)
            ?: error("Could not contact GitHub.")
        val sha = Regex("\\\"sha\\\"\\s*:\\s*\\\"([0-9a-fA-F]{40})\\\"")
            .find(response)?.groupValues?.get(1)
            ?: error("GitHub did not return a database commit.")
        DatabaseRevision.Commit(sha)
    }
}
