package app.xiguang.data.repository

import app.xiguang.data.file.AttachmentStore
import app.xiguang.data.local.CollectionDao
import app.xiguang.data.local.CollectionEntity
import app.xiguang.data.local.FolderDao
import app.xiguang.data.local.FolderEntity
import app.xiguang.domain.model.CollectionFilter
import app.xiguang.domain.model.ContentType
import app.xiguang.domain.model.Platform
import app.xiguang.domain.model.ReadFilter
import app.xiguang.domain.model.CollectionSearchOptions
import app.xiguang.domain.model.FolderMutationResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class CollectionRepositoryTest {
    @Test
    fun `parent folder filter includes collections in child folders`() = runBlocking {
        val collections = FakeCollectionDao(
            listOf(
                collection(id = 1, folderId = 10),
                collection(id = 2, folderId = 11),
                collection(id = 3, folderId = null),
            ),
        )
        val folders = FakeFolderDao(
            listOf(
                folder(id = 10, parentId = null, name = "Reading"),
                folder(id = 11, parentId = 10, name = "Design"),
            ),
        )
        val repository = CollectionRepository(collections, folders, NoopAttachmentStore)

        val results = repository.observeCollections(CollectionFilter.Folder(10)).first()

        assertEquals(listOf(1L, 2L), results.map { it.id })
        assertEquals("Reading / Design", results.single { it.id == 2L }.folderPath)
    }

    @Test
    fun `platform filter only returns selected platform`() = runBlocking {
        val collections = FakeCollectionDao(
            listOf(
                collection(id = 1, platform = Platform.BILIBILI),
                collection(id = 2, platform = Platform.ZHIHU),
            ),
        )
        val repository = CollectionRepository(collections, FakeFolderDao(emptyList()), NoopAttachmentStore)

        val results = repository.observeCollections(CollectionFilter.PlatformFilter(Platform.ZHIHU)).first()

        assertEquals(listOf(2L), results.map { it.id })
    }

    @Test
    fun `search options match shared text and unread state`() = runBlocking {
        val collections = FakeCollectionDao(
            listOf(
                collection(id = 1).copy(sharedText = "Compose notes", isRead = false),
                collection(id = 2).copy(sharedText = "Compose notes", isRead = true),
            ),
        )
        val repository = CollectionRepository(collections, FakeFolderDao(emptyList()), NoopAttachmentStore)

        val results = repository.observeCollections(
            searchOptions = CollectionSearchOptions(query = "compose", readFilter = ReadFilter.UNREAD),
        ).first()

        assertEquals(listOf(1L), results.map { it.id })
    }

    @Test
    fun `folder with collections cannot be deleted and third level is rejected`() = runBlocking {
        val folders = FakeFolderDao(
            listOf(
                folder(id = 10, parentId = null, name = "Root"),
                folder(id = 11, parentId = 10, name = "Child"),
            ),
        )
        val repository = CollectionRepository(
            FakeCollectionDao(listOf(collection(id = 1, folderId = 10))),
            folders,
            NoopAttachmentStore,
        )

        assertEquals(FolderMutationResult.NotEmpty, repository.deleteFolderIfEmpty(10))
        assertEquals(FolderMutationResult.MaximumDepthReached, repository.createFolder("Third", 11))
    }

    @Test
    fun `moving a folder changes only its sibling order`() = runBlocking {
        val folders = FakeFolderDao(
            listOf(
                folder(id = 10, parentId = null, name = "First").copy(sortOrder = 0),
                folder(id = 11, parentId = null, name = "Second").copy(sortOrder = 1),
            ),
        )
        val repository = CollectionRepository(FakeCollectionDao(emptyList()), folders, NoopAttachmentStore)

        assertEquals(FolderMutationResult.Success, repository.moveFolderWithinSiblings(11, moveUp = true))
        assertEquals(listOf("Second", "First"), repository.observeManagedFolders().first().sortedBy { it.sortOrder }.map { it.name })
    }

    private fun collection(
        id: Long,
        folderId: Long? = null,
        platform: Platform = Platform.BLOG,
    ) = CollectionEntity(
        id = id,
        canonicalUrl = "https://example.com/$id",
        originalUrl = "https://example.com/$id",
        title = "Entry $id",
        platform = platform.name,
        contentType = ContentType.LINK.name,
        folderId = folderId,
        createdAt = id,
        updatedAt = id,
    )

    private fun folder(id: Long, parentId: Long?, name: String) = FolderEntity(
        id = id,
        parentId = parentId,
        name = name,
        createdAt = id,
    )
}

private object NoopAttachmentStore : AttachmentStore {
    override suspend fun persist(uriText: String?, mimeType: String?): String? = uriText
}

private class FakeCollectionDao(items: List<CollectionEntity>) : CollectionDao {
    private val values = MutableStateFlow(items)

    override fun observeAll(): Flow<List<CollectionEntity>> = values

    override suspend fun insert(item: CollectionEntity): Long = 1

    override suspend fun moveExistingByUrl(canonicalUrl: String, folderId: Long?, updatedAt: Long): Int = 1

    override suspend fun updateDetails(id: Long, title: String, note: String?, folderId: Long?, updatedAt: Long): Int = 1

    override suspend fun moveByIds(ids: List<Long>, folderId: Long?, updatedAt: Long): Int = ids.size

    override suspend fun updateReadState(ids: List<Long>, isRead: Boolean, updatedAt: Long): Int = ids.size

    override suspend fun deleteByIds(ids: List<Long>): Int = ids.size

    override suspend fun countInFolder(folderId: Long): Int = values.value.count { it.folderId == folderId }
}

private class FakeFolderDao(items: List<FolderEntity>) : FolderDao {
    private val values = MutableStateFlow(items)

    override fun observeAll(): Flow<List<FolderEntity>> = values

    override suspend fun insert(folder: FolderEntity): Long = 1

    override suspend fun findById(id: Long): FolderEntity? = values.value.firstOrNull { it.id == id }

    override suspend fun countChildren(parentId: Long): Int = values.value.count { it.parentId == parentId }

    override suspend fun maxSortOrder(parentId: Long?): Int = values.value.filter { it.parentId == parentId }.maxOfOrNull { it.sortOrder } ?: -1

    override suspend fun rename(id: Long, name: String): Int = updateFolder(id) { it.copy(name = name) }

    override suspend fun updateSortOrder(id: Long, sortOrder: Int): Int = updateFolder(id) { it.copy(sortOrder = sortOrder) }

    override suspend fun deleteById(id: Long): Int {
        if (values.value.none { it.id == id }) return 0
        values.value = values.value.filterNot { it.id == id }
        return 1
    }

    override suspend fun deleteIfEmpty(id: Long): Int = deleteById(id)

    private fun updateFolder(id: Long, transform: (FolderEntity) -> FolderEntity): Int {
        val folder = values.value.firstOrNull { it.id == id } ?: return 0
        values.value = values.value.map { current -> if (current.id == id) transform(folder) else current }
        return 1
    }
}
