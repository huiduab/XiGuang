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
