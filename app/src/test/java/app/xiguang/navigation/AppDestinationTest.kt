package app.xiguang.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDestinationTest {
    @Test
    fun collectionList_encodesFilterIdentityAndDisplayTitle() {
        val route = AppDestination.collectionList(
            groupKey = "folder-12",
            title = "人工智能 / 模型发布",
        )

        assertEquals(
            "collection/list" +
                "?groupKey=folder-12" +
                "&title=%E4%BA%BA%E5%B7%A5%E6%99%BA%E8%83%BD%20%2F%20" +
                "%E6%A8%A1%E5%9E%8B%E5%8F%91%E5%B8%83",
            route,
        )
    }

    @Test
    fun collectionChildren_keepCollectionAsSelectedTopLevelDestination() {
        assertEquals(
            AppDestination.COLLECTION,
            AppDestination.topLevelRoute(AppDestination.COLLECTION_LIST),
        )
        assertEquals(
            AppDestination.COLLECTION,
            AppDestination.topLevelRoute(AppDestination.COLLECTION_SEARCH),
        )
    }

    @Test
    fun searchCoversBottomBarWhileCategoryListKeepsIt() {
        assertFalse(AppDestination.showsBottomBar(AppDestination.COLLECTION_SEARCH))
        assertTrue(AppDestination.showsBottomBar(AppDestination.COLLECTION_LIST))
    }

    @Test
    fun detailEditorAndReaderCoverBottomBar() {
        assertFalse(AppDestination.showsBottomBar(AppDestination.COLLECTION_DETAIL))
        assertFalse(AppDestination.showsBottomBar(AppDestination.COLLECTION_EDIT))
        assertFalse(AppDestination.showsBottomBar(AppDestination.COLLECTION_READER))
        assertEquals("collection/edit/42", AppDestination.collectionEdit(42))
    }
}
