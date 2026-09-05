package com.example.refractiveindexapp.parsing

import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class CatalogueSnapshotSource { Bundled, Cache, Remote }

data class CatalogueSnapshot(
    val catalogue: Catalogue,
    val revision: DatabaseRevision.Commit,
    val source: CatalogueSnapshotSource
)

/** Loads immutable catalogue snapshots and persists downloaded revisions in app-private storage. */
interface CatalogueSnapshotRepository {
    suspend fun load(
        revision: DatabaseRevision.Commit,
        protectedRevision: DatabaseRevision.Commit? = null
    ): Result<CatalogueSnapshot>
}

class CatalogueSnapshotStore(
    private val directory: File,
    private val parser: CatalogueParser = CatalogueParser(),
    private val clockMillis: () -> Long = System::currentTimeMillis
) {
    fun read(revision: DatabaseRevision.Commit): CatalogueSnapshot? {
        val catalogueFile = catalogueFile(revision)
        val metadataFile = metadataFile(revision)
        if (!catalogueFile.isFile || !metadataFile.isFile) {
            remove(revision)
            return null
        }
        val metadataSha = runCatching { metadataFile.readLines().firstOrNull() }.getOrNull()
        if (metadataSha != revision.sha) {
            remove(revision)
            return null
        }
        return runCatching {
            val catalogue = parser.parse(catalogueFile.readText())
            val now = clockMillis()
            catalogueFile.setLastModified(now)
            metadataFile.setLastModified(now)
            CatalogueSnapshot(catalogue, revision, CatalogueSnapshotSource.Cache)
        }.getOrElse {
            remove(revision)
            null
        }
    }

    fun write(
        revision: DatabaseRevision.Commit,
        text: String,
        protectedRevision: DatabaseRevision.Commit? = null
    ): CatalogueSnapshot {
        val catalogue = parser.parse(text)
        check(directory.exists() || directory.mkdirs()) { "Could not create catalogue cache directory." }
        prune(excluding = setOfNotNull(revision.sha, protectedRevision?.sha))
        atomicWrite(catalogueFile(revision), text)
        atomicWrite(metadataFile(revision), "${revision.sha}\n${clockMillis()}\n")
        return CatalogueSnapshot(catalogue, revision, CatalogueSnapshotSource.Remote)
    }

    private fun prune(excluding: Set<String>) {
        val metadataFiles = directory.listFiles { file -> file.name.endsWith(METADATA_SUFFIX) }
            ?.sortedBy { it.lastModified() }
            .orEmpty()
        var remaining = metadataFiles.size
        metadataFiles.forEach { metadata ->
            if (remaining < MAX_SNAPSHOTS) return@forEach
            val sha = metadata.name.removeSuffix(METADATA_SUFFIX)
            if (sha !in excluding) {
                runCatching { DatabaseRevision.Commit(sha) }
                    .getOrNull()
                    ?.let(::remove)
                    ?: metadata.delete()
                remaining--
            }
        }
    }

    private fun atomicWrite(target: File, content: String) {
        val temporary = File.createTempFile("catalogue-", ".tmp", directory)
        try {
            temporary.writeText(content)
            check(temporary.renameTo(target)) { "Could not save catalogue cache." }
        } finally {
            if (temporary.exists()) temporary.delete()
        }
    }

    private fun remove(revision: DatabaseRevision.Commit) {
        catalogueFile(revision).delete()
        metadataFile(revision).delete()
    }

    private fun catalogueFile(revision: DatabaseRevision.Commit) = File(directory, "${revision.sha}$CATALOGUE_SUFFIX")
    private fun metadataFile(revision: DatabaseRevision.Commit) = File(directory, "${revision.sha}$METADATA_SUFFIX")

    private companion object {
        const val MAX_SNAPSHOTS = 10
        const val CATALOGUE_SUFFIX = ".yml"
        const val METADATA_SUFFIX = ".meta"
    }
}

class PersistentCatalogueSnapshotRepository(
    private val store: CatalogueSnapshotStore,
    private val parser: CatalogueParser = CatalogueParser(),
    private val downloader: suspend (String) -> String? = ::downloadText
) : CatalogueSnapshotRepository {
    override suspend fun load(
        revision: DatabaseRevision.Commit,
        protectedRevision: DatabaseRevision.Commit?
    ): Result<CatalogueSnapshot> = withContext(Dispatchers.IO) {
        runCatching {
            store.read(revision) ?: run {
                val text = downloader(RefractiveIndexDatabase.catalogueUrl(revision))
                    ?: error("Could not download the catalogue for ${revision.sha.take(12)}.")
                // Parse before writing so malformed upstream data never replaces a valid cache entry.
                parser.parse(text)
                store.write(revision, text, protectedRevision)
            }
        }
    }
}
