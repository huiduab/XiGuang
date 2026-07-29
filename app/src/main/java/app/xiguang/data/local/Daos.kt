package app.xiguang.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Query("SELECT * FROM collections ORDER BY created_at DESC")
    fun observeAll(): Flow<List<CollectionEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: CollectionEntity): Long

    @Query(
        """
        UPDATE collections
        SET folder_id = :folderId, updated_at = :updatedAt
        WHERE canonical_url = :canonicalUrl
        """,
    )
    suspend fun moveExistingByUrl(canonicalUrl: String, folderId: Long?, updatedAt: Long): Int

    @Query(
        """
        UPDATE collections
        SET title = :title, note = :note, folder_id = :folderId, updated_at = :updatedAt
        WHERE id = :id
        """,
    )
    suspend fun updateDetails(id: Long, title: String, note: String?, folderId: Long?, updatedAt: Long): Int

    @Query("UPDATE collections SET folder_id = :folderId, updated_at = :updatedAt WHERE id IN (:ids)")
    suspend fun moveByIds(ids: List<Long>, folderId: Long?, updatedAt: Long): Int

    @Query("UPDATE collections SET is_read = :isRead, updated_at = :updatedAt WHERE id IN (:ids)")
    suspend fun updateReadState(ids: List<Long>, isRead: Boolean, updatedAt: Long): Int

    @Query("DELETE FROM collections WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>): Int

    @Query("SELECT COUNT(*) FROM collections WHERE folder_id = :folderId")
    suspend fun countInFolder(folderId: Long): Int
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY parent_id, sort_order, name")
    fun observeAll(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(folder: FolderEntity): Long

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): FolderEntity?

    @Query("SELECT COUNT(*) FROM folders WHERE parent_id = :parentId")
    suspend fun countChildren(parentId: Long): Int

    @Query("SELECT COALESCE(MAX(sort_order), -1) FROM folders WHERE parent_id IS :parentId")
    suspend fun maxSortOrder(parentId: Long?): Int

    @Query("UPDATE folders SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String): Int

    @Query("UPDATE folders SET sort_order = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int): Int

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteById(id: Long): Int

    @Query(
        """
        DELETE FROM folders
        WHERE id = :id
          AND NOT EXISTS (SELECT 1 FROM folders AS child WHERE child.parent_id = :id)
          AND NOT EXISTS (SELECT 1 FROM collections WHERE folder_id = :id)
        """,
    )
    suspend fun deleteIfEmpty(id: Long): Int

    @Transaction
    suspend fun replaceSiblingOrder(ids: List<Long>) {
        ids.forEachIndexed { index, id -> updateSortOrder(id, index) }
    }
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: ProjectEntity): Long

    @Query("UPDATE projects SET name = :name, description = :description, updated_at = :updatedAt WHERE id = :id")
    suspend fun update(id: Long, name: String, description: String?, updatedAt: Long): Int

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun delete(id: Long): Int
}

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources ORDER BY updated_at DESC")
    fun observeAll(): Flow<List<SourceEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: SourceEntity): Long

    @Query("UPDATE sources SET name = :name, url = :url, project_id = :projectId, updated_at = :updatedAt WHERE id = :id")
    suspend fun update(id: Long, name: String, url: String?, projectId: Long?, updatedAt: Long): Int

    @Query("DELETE FROM sources WHERE id = :id")
    suspend fun delete(id: Long): Int
}
