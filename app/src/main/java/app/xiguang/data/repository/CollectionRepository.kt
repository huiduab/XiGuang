package app.xiguang.data.repository

import app.xiguang.data.local.CollectionDao
import app.xiguang.data.local.CollectionEntity
import app.xiguang.data.local.FolderDao
import app.xiguang.data.local.FolderEntity
import app.xiguang.domain.model.SharedPayload
import kotlinx.coroutines.flow.Flow

class CollectionRepository(
    private val collectionDao: CollectionDao,
    private val folderDao: FolderDao,
) {
    val collections: Flow<List<CollectionEntity>> = collectionDao.observeAll()
    val folders: Flow<List<FolderEntity>> = folderDao.observeAll()

    suspend fun save(payload: SharedPayload, folderId: Long?): SaveResult {
        val now = System.currentTimeMillis()
        val insertedId = collectionDao.insert(
            CollectionEntity(
                canonicalUrl = payload.canonicalUrl,
                originalUrl = payload.originalUrl,
                title = payload.title,
                sharedText = payload.sharedText,
                platform = payload.platform.name,
                contentType = payload.contentType.name,
                mimeType = payload.mimeType,
                previewUri = payload.previewUri,
                folderId = folderId,
                sharedFromPackage = payload.sharedFromPackage,
                createdAt = now,
                updatedAt = now,
            ),
        )
        if (insertedId != -1L) return SaveResult.Created(insertedId)

        val canonicalUrl = payload.canonicalUrl
        if (canonicalUrl != null) {
            collectionDao.moveExistingByUrl(canonicalUrl, folderId, now)
        }
        return SaveResult.AlreadyCollected
    }
}

sealed interface SaveResult {
    data class Created(val id: Long) : SaveResult
    data object AlreadyCollected : SaveResult
}
