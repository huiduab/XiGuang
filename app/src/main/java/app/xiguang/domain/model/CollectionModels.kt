package app.xiguang.domain.model

enum class Platform(val label: String) {
    X("X"),
    WEIBO("微博"),
    XIAOHONGSHU("小红书"),
    DOUYIN("抖音"),
    BILIBILI("B站"),
    ZHIHU("知乎"),
    YOUTUBE("YouTube"),
    BLOG("博客"),
    OTHER("其他"),
}

enum class ContentType {
    LINK,
    TEXT,
    IMAGE,
    DOCUMENT,
}

enum class GroupMode {
    FOLDER,
    PLATFORM,
}

data class SharedPayload(
    val title: String,
    val sharedText: String?,
    val originalUrl: String?,
    val canonicalUrl: String?,
    val platform: Platform,
    val contentType: ContentType,
    val mimeType: String?,
    val previewUri: String?,
    val sharedFromPackage: String?,
)

sealed interface CollectionFilter {
    data object All : CollectionFilter

    data object Unfiled : CollectionFilter

    data class Folder(val folderId: Long) : CollectionFilter

    data class PlatformFilter(val platform: Platform) : CollectionFilter
}

data class SavedCollection(
    val id: Long,
    val canonicalUrl: String?,
    val originalUrl: String?,
    val title: String,
    val sharedText: String?,
    val platform: Platform,
    val contentType: ContentType,
    val mimeType: String?,
    val previewUri: String?,
    val folderId: Long?,
    val folderPath: String?,
    val note: String?,
    val isRead: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

enum class ReadFilter {
    ALL,
    UNREAD,
    READ,
}

data class CollectionSearchOptions(
    val query: String = "",
    val contentType: ContentType? = null,
    val readFilter: ReadFilter = ReadFilter.ALL,
)

data class CollectionEditInput(
    val title: String,
    val note: String?,
    val folderId: Long?,
)

data class FolderOption(
    val id: Long,
    val name: String,
    val path: String,
)

data class ManagedFolder(
    val id: Long,
    val parentId: Long?,
    val name: String,
    val sortOrder: Int,
    val collectionCount: Int,
)

sealed interface FolderMutationResult {
    data object Success : FolderMutationResult

    data object InvalidName : FolderMutationResult

    data object DuplicateName : FolderMutationResult

    data object ParentNotFound : FolderMutationResult

    data object MaximumDepthReached : FolderMutationResult

    data object NotEmpty : FolderMutationResult

    data object NotFound : FolderMutationResult
}

enum class ThemePreference { SYSTEM, LIGHT, DARK }

enum class DefaultOpenMode { IN_APP, EXTERNAL }

data class AppSettings(
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val defaultOpenMode: DefaultOpenMode = DefaultOpenMode.IN_APP,
    val notificationsEnabled: Boolean = false,
)

data class Project(
    val id: Long,
    val name: String,
    val description: String?,
    val sources: List<Source>,
)

data class Source(
    val id: Long,
    val projectId: Long?,
    val name: String,
    val url: String?,
)
