package app.xiguang.ui.folder

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import app.xiguang.R
import app.xiguang.ui.theme.XiguangTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FolderCreationDialogsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun createDialog_submitsEnteredFolderName() {
        var submittedName: String? = null
        composeRule.setContent {
            XiguangTheme {
                NewRootFolderDialog(
                    onDismiss = {},
                    onConfirm = { submittedName = it },
                )
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("Research")
        composeRule.onNodeWithText(text(R.string.create_action)).performClick()

        assertEquals("Research", submittedName)
    }

    private fun text(resId: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(resId)
}
