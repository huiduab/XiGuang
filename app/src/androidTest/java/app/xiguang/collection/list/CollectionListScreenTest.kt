package app.xiguang.collection.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import app.xiguang.domain.model.ContentType
import app.xiguang.domain.model.Platform
import app.xiguang.domain.model.SavedCollection
import app.xiguang.ui.theme.XiguangTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CollectionListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun selecting_collection_opens_its_detail() {
        var selectedId: Long? = null
        composeRule.setContent {
            XiguangTheme {
                CollectionListScreen(
                    state = CollectionListUiState(
                        title = "Design",
                        collections = listOf(
                            SavedCollection(
                                id = 42,
                                canonicalUrl = "https://example.com",
                                originalUrl = "https://example.com",
                                title = "Compose patterns",
                                sharedText = null,
                                platform = Platform.BLOG,
                                contentType = ContentType.LINK,
                                mimeType = null,
                                previewUri = null,
                                folderId = null,
                                folderPath = null,
                                note = null,
                                isRead = false,
                                createdAt = 0,
                                updatedAt = 0,
                            ),
                        ),
                    ),
                    onBack = {},
                    onOpenCollection = { selectedId = it },
                )
            }
        }

        composeRule.onNodeWithText("Compose patterns").assertIsDisplayed().performClick()

        assertEquals(42L, selectedId)
    }
}
