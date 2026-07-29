package app.xiguang.today

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import app.xiguang.R
import app.xiguang.domain.model.ContentType
import app.xiguang.domain.model.Platform
import app.xiguang.domain.model.SavedCollection
import app.xiguang.ui.theme.XiguangTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TodayScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyState_rendersSummaryAndBothSections() {
        composeRule.setContent {
            XiguangTheme {
                TodayScreen(
                    state = TodayUiState(),
                    onOpenCollection = {},
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.app_name)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.today_reading_title)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.today_added)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.today_unread)).assertIsDisplayed()
    }

    @Test
    fun contentAndRandomRead_openExpectedCollections() {
        val listed = collection(id = 42, title = "今日内容", isRead = true)
        val random = collection(id = 73, title = "随机内容", isRead = false)
        var openedId: Long? = null

        composeRule.setContent {
            XiguangTheme {
                TodayScreen(
                    state = TodayUiState(
                        collections = listOf(listed, random),
                        addedToday = listOf(listed),
                    ),
                    onOpenCollection = { openedId = it },
                    onRandomRead = { openedId = random.id },
                )
            }
        }

        composeRule.onNodeWithText(text(R.string.today_random_read)).performClick()
        assertEquals(73L, openedId)

        composeRule.onNodeWithText("今日内容").assertIsDisplayed().performClick()
        assertEquals(42L, openedId)
    }

    private fun text(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)

    private fun collection(
        id: Long,
        title: String,
        isRead: Boolean,
    ) = SavedCollection(
        id = id,
        canonicalUrl = "https://example.com/$id",
        originalUrl = "https://example.com/$id",
        title = title,
        sharedText = null,
        platform = Platform.BLOG,
        contentType = ContentType.LINK,
        mimeType = null,
        previewUri = null,
        folderId = null,
        folderPath = null,
        note = null,
        isRead = isRead,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis(),
    )
}
