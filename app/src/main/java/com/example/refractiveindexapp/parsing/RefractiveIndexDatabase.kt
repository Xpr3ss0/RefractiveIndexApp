package com.example.refractiveindexapp.parsing

/** A reproducible upstream database revision, or the current upstream catalogue. */
sealed interface DatabaseRevision {
    val gitRef: String

    /** The upstream default branch. Its contents may change over time. */
    data object Latest : DatabaseRevision {
        override val gitRef: String = "main"
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
    private const val RawDatabaseBaseUrl =
        "https://raw.githubusercontent.com/polyanskiy/refractiveindex.info-database"

    fun catalogueUrl(revision: DatabaseRevision = DatabaseRevision.Latest): String =
        "$RawDatabaseBaseUrl/${revision.gitRef}/database/catalog-nk.yml"

    fun materialUrl(
        dataPath: String,
        revision: DatabaseRevision = DatabaseRevision.Latest
    ): String = "$RawDatabaseBaseUrl/${revision.gitRef}/database/data/$dataPath"

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
