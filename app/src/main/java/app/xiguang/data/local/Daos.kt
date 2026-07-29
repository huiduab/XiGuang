package app.xiguang.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders ORDER BY parent_id, sort_order, name")
    fun observeAll(): Flow<List<FolderEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(folder: FolderEntity): Long
}
