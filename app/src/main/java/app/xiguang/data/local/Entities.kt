package app.xiguang.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "folders",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["parent_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index("parent_id"),
        Index(value = ["parent_id", "name"], unique = true),
    ],
)
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "parent_id")
    val parentId: Long? = null,
    val name: String,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int = 0,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)

@Entity(
    tableName = "collections",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["canonical_url"], unique = true),
        Index("folder_id"),
        Index("platform"),
        Index("created_at"),
    ],
)
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "canonical_url")
    val canonicalUrl: String? = null,
    @ColumnInfo(name = "original_url")
    val originalUrl: String? = null,
    val title: String,
    @ColumnInfo(name = "shared_text")
    val sharedText: String? = null,
    val platform: String,
    @ColumnInfo(name = "content_type")
    val contentType: String,
    @ColumnInfo(name = "mime_type")
    val mimeType: String? = null,
    @ColumnInfo(name = "preview_uri")
    val previewUri: String? = null,
    @ColumnInfo(name = "folder_id")
    val folderId: Long? = null,
    @ColumnInfo(name = "shared_from_package")
    val sharedFromPackage: String? = null,
    val note: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
)
