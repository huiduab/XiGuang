package app.xiguang.navigation

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import app.xiguang.MainActivity
import app.xiguang.R
import org.junit.Rule
import org.junit.Test

class XiguangNavigationTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun bottomNavigation_switchesSelection_withoutDuplicatingCurrentPage() {
        composeRule.bottomTab(R.string.nav_settings).performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.settings_title)).assertIsDisplayed()
        composeRule.bottomTab(R.string.nav_settings).assertIsSelected()
        composeRule.bottomTab(R.string.nav_settings).performClick()
        composeRule.pressSystemBack()
        composeRule.bottomTab(R.string.nav_collection).assertIsSelected()
    }

    @Test
    fun search_coversBottomBar_andBackReturnsToCollection() {
        composeRule.onNodeWithContentDescription("搜索收藏").performClick()
        composeRule.onNodeWithText(composeRule.activity.getString(R.string.search_query_label)).assertIsDisplayed()
        composeRule.bottomTab(R.string.nav_collection).assertDoesNotExist()
        composeRule.pressSystemBack()
        composeRule.bottomTab(R.string.nav_collection).assertIsSelected()
    }

    private fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.bottomTab(labelRes: Int): SemanticsNodeInteraction =
        onNode(hasContentDescription(activity.getString(labelRes)) and isSelectable())

    private fun AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>.pressSystemBack() {
        activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        waitForIdle()
    }
}
