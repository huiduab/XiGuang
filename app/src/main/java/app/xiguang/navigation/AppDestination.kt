package app.xiguang.navigation

import java.net.URLEncoder

internal object AppDestination {
    const val TODAY = "today"
    const val PROJECTS = "projects"
    const val COLLECTION = "collection"
    const val COLLECTION_LIST = "collection/list?groupKey={groupKey}&title={title}"
    const val COLLECTION_SEARCH = "collection/search"
    const val COLLECTION_DETAIL = "collection/detail/{collectionId}"
    const val COLLECTION_READER = "collection/reader/{collectionId}"
    const val COLLECTION_EDIT = "collection/edit/{collectionId}"
    const val FOLDER_MANAGEMENT = "collection/folders"
    const val SETTINGS = "settings"

    fun collectionList(groupKey: String, title: String): String =
        "collection/list" +
            "?groupKey=${encodeQueryValue(groupKey)}" +
            "&title=${encodeQueryValue(title)}"

    fun collectionDetail(collectionId: Long): String = "collection/detail/$collectionId"

    fun collectionReader(collectionId: Long): String = "collection/reader/$collectionId"

    fun collectionEdit(collectionId: Long): String = "collection/edit/$collectionId"

    fun topLevelRoute(route: String?): String? = when {
        route == TODAY -> TODAY
        route == PROJECTS -> PROJECTS
        route == SETTINGS -> SETTINGS
        route?.startsWith(COLLECTION) == true -> COLLECTION
        else -> null
    }

    fun showsBottomBar(route: String?): Boolean =
        route != null && route != COLLECTION_SEARCH && route != COLLECTION_DETAIL && route != COLLECTION_READER && route != COLLECTION_EDIT && route != FOLDER_MANAGEMENT

    private fun encodeQueryValue(value: String): String =
        URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")
}
