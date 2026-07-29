package app.xiguang.data.repository

import app.xiguang.data.local.CollectionDao
import app.xiguang.data.local.CollectionEntity
import app.xiguang.data.local.FolderDao
import app.xiguang.data.local.FolderEntity
import app.xiguang.data.file.AttachmentStore
import app.xiguang.domain.model.CollectionFilter
import app.xiguang.domain.model.CollectionEditInput
import app.xiguang.domain.model.CollectionSearchOptions
import app.xiguang.domain.model.ContentType
import app.xiguang.domain.model.FolderOption
import app.xiguang.domain.model.FolderMutationResult
import app.xiguang.domain.model.ManagedFolder
import app.xiguang.domain.model.Platform
import app.xiguang.domain.model.SavedCollection
import app.xiguang.domain.model.SharedPayload
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class CollectionRepository(
    private val collectionDao: CollectionDao,
    private val folderDao: FolderDao,
    private val attachmentStore: AttachmentStore,
) {
    val collections: Flow<List<CollectionEntity>> = collectionDao.observeAll()
    val folders: Flow<List<FolderEntity>> = folderDao.observeAll()

    fun observeCollections(
        filter: CollectionFilter = CollectionFilter.All,
        searchOptions: CollectionSearchOptions = CollectionSearchOptions(),
    ): Flow<List<SavedCollection>> =
        combine(collections, folders) { items, folders ->
            val matchingFolderIds = folderIdsForFilter(filter, folders)
            items.asSequence()
                .filter { item ->
                    when (filter) {
                        CollectionFilter.All -> true
                        CollectionFilter.Unfiled -> item.folderId == null
                        is CollectionFilter.Folder -> item.folderId in matchingFolderIds
                        is CollectionFilter.PlatformFilter -> item.platform == filter.platform.name
                    }
                }
                .filter { item -> item.matches(searchOptions) }
                .map { item -> item.toSavedCollection(folders) }
                .toList()
        }

    fun observeCollection(id: Long): Flow<SavedCollection?> = combine(collections, folders) { items, folders ->
        items.firstOrNull { it.id == id }?.toSavedCollection(folders)
    }

    fun observeFolderOptions(): Flow<List<FolderOption>> = folders.map { allFolders ->
        allFolders.map { folder ->
            FolderOption(
                id = folder.id,
                name = folder.name,
                path = folderPath(folder.id, allFolders) ?: folder.name,
            )
        }
    }

    fun observeManagedFolders(): Flow<List<ManagedFolder>> = combine(collections, folders) { items, allFolders ->
        val counts = items.groupingBy(CollectionEntity::folderId).eachCount()
        allFolders.map { folder ->
            ManagedFolder(
                id = folder.id,
                parentId = folder.parentId,
                name = folder.name,
                sortOrder = folder.sortOrder,
                collectionCount = counts[folder.id] ?: 0,
            )
        }
    }

    suspend fun save(payload: SharedPayload, folderId: Long?): SaveResult {
        val now = System.currentTimeMillis()
        val previewUri = when (payload.contentType) {
            ContentType.IMAGE,
            ContentType.DOCUMENT,
            -> attachmentStore.persist(payload.previewUri, payload.mimeType)

            ContentType.LINK,
            ContentType.TEXT,
            -> payload.previewUri
        }
        val insertedId = collectionDao.insert(
            CollectionEntity(
                canonicalUrl = payload.canonicalUrl,
                originalUrl = payload.originalUrl,
                title = payload.title,
                sharedText = payload.sharedText,
                platform = payload.platform.name,
                contentType = payload.contentType.name,
                mimeType = payload.mimeType,
                previewUri = previewUri,
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

    suspend fun updateCollection(id: Long, input: CollectionEditInput): Boolean =
        collectionDao.updateDetails(
            id = id,
            title = input.title.trim(),
            note = input.note?.trim()?.ifBlank { null },
            folderId = input.folderId,
            updatedAt = System.currentTimeMillis(),
        ) > 0

    suspend fun moveCollections(ids: Set<Long>, folderId: Long?): Int {
        if (ids.isEmpty()) return 0
        return collectionDao.moveByIds(ids.toList(), folderId, System.currentTimeMillis())
    }

    suspend fun updateReadState(ids: Set<Long>, isRead: Boolean): Int {
        if (ids.isEmpty()) return 0
        return collectionDao.updateReadState(ids.toList(), isRead, System.currentTimeMillis())
    }

    suspend fun deleteCollections(ids: Set<Long>): Int {
        if (ids.isEmpty()) return 0
        return collectionDao.deleteByIds(ids.toList())
    }

    suspend fun createFolder(name: String, parentId: Long?): FolderMutationResult {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return FolderMutationResult.InvalidName
        if (parentId != null) {
            val parent = folderDao.findById(parentId) ?: return FolderMutationResult.ParentNotFound
            if (parent.parentId != null) return FolderMutationResult.MaximumDepthReached
        }
        return try {
            val nextSortOrder = folderDao.maxSortOrder(parentId) + 1
            folderDao.insert(
                FolderEntity(
                    parentId = parentId,
                    name = normalizedName,
                    sortOrder = nextSortOrder,
                    createdAt = System.currentTimeMillis(),
                ),
            )
            FolderMutationResult.Success
        } catch (_: android.database.sqlite.SQLiteConstraintException) {
            FolderMutationResult.DuplicateName
        }
    }

    suspend fun renameFolder(id: Long, name: String): FolderMutationResult {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) return FolderMutationResult.InvalidName
        return try {
            if (folderDao.rename(id, normalizedName) > 0) FolderMutationResult.Success else FolderMutationResult.NotFound
        } catch (_: android.database.sqlite.SQLiteConstraintException) {
            FolderMutationResult.DuplicateName
        }
    }

    suspend fun moveFolderWithinSiblings(id: Long, moveUp: Boolean): FolderMutationResult {
        val folder = folderDao.findById(id) ?: return FolderMutationResult.NotFound
        val siblings = folders.first().filter { it.parentId == folder.parentId }.sortedBy(FolderEntity::sortOrder).toMutableList()
        val index = siblings.indexOfFirst { it.id == id }
        val targetIndex = if (moveUp) index - 1 else index + 1
        if (index < 0 || targetIndex !in siblings.indices) return FolderMutationResult.Success
        val moved = siblings.removeAt(index)
        siblings.add(targetIndex, moved)
        folderDao.replaceSiblingOrder(siblings.map(FolderEntity::id))
        return FolderMutationResult.Success
    }

    suspend fun deleteFolderIfEmpty(id: Long): FolderMutationResult {
        val folder = folderDao.findById(id) ?: return FolderMutationResult.NotFound
        if (collectionDao.countInFolder(folder.id) > 0 || folderDao.countChildren(folder.id) > 0) {
            return FolderMutationResult.NotEmpty
        }
        if (folderDao.deleteIfEmpty(folder.id) > 0) return FolderMutationResult.Success
        return if (folderDao.findById(folder.id) == null) FolderMutationResult.NotFound else FolderMutationResult.NotEmpty
    }

    private fun folderIdsForFilter(
        filter: CollectionFilter,
        folders: List<FolderEntity>,
    ): Set<Long> {
        if (filter !is CollectionFilter.Folder) return emptySet()

        val byParent = folders.groupBy(FolderEntity::parentId)
        val pending = ArrayDeque<Long>().apply { add(filter.folderId) }
        val result = mutableSetOf<Long>()
        while (pending.isNotEmpty()) {
            val id = pending.removeFirst()
            if (!result.add(id)) continue
            byParent[id].orEmpty().forEach { child -> pending.add(child.id) }
        }
        return result
    }

    private fun CollectionEntity.toSavedCollection(folders: List<FolderEntity>): SavedCollection = SavedCollection(
        id = id,
        canonicalUrl = canonicalUrl,
        originalUrl = originalUrl,
        title = title,
        sharedText = sharedText,
        platform = Platform.entries.firstOrNull { it.name == platform } ?: Platform.OTHER,
        contentType = ContentType.entries.firstOrNull { it.name == contentType } ?: ContentType.TEXT,
        mimeType = mimeType,
        previewUri = previewUri,
        folderId = folderId,
        folderPath = folderPath(folderId, folders),
        note = note,
        isRead = isRead,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun CollectionEntity.matches(options: CollectionSearchOptions): Boolean {
        val normalizedQuery = options.query.trim()
        val matchesQuery = normalizedQuery.isBlank() || listOfNotNull(title, originalUrl, sharedText, note)
            .any { value -> value.contains(normalizedQuery, ignoreCase = true) }
        val matchesType = options.contentType == null || contentType == options.contentType.name
        val matchesReadState = when (options.readFilter) {
            app.xiguang.domain.model.ReadFilter.ALL -> true
            app.xiguang.domain.model.ReadFilter.UNREAD -> !isRead
            app.xiguang.domain.model.ReadFilter.READ -> isRead
        }
        return matchesQuery && matchesType && matchesReadState
    }

    private fun folderPath(folderId: Long?, folders: List<FolderEntity>): String? {
        if (folderId == null) return null
        val foldersById = folders.associateBy(FolderEntity::id)
        val names = mutableListOf<String>()
        val visited = mutableSetOf<Long>()
        var current = foldersById[folderId]
        while (current != null && visited.add(current.id)) {
            names += current.name
            current = current.parentId?.let(foldersById::get)
        }
        return names.asReversed().joinToString(" / ").ifBlank { null }
    }
}

sealed interface SaveResult {
    data class Created(val id: Long) : SaveResult
    data object AlreadyCollected : SaveResult
}
