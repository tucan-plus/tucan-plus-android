package de.selfmade4u.tucanplus

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasContentDescriptionExactly
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// https://developer.android.com/training/testing/fundamentals/strategies
// https://developer.android.com/training/testing/ui-tests/behavior
// https://developer.android.com/training/testing/ui-tests/screenshot
// https://developer.android.com/training/testing/local-tests/robolectric
// https://developer.android.com/develop/ui/compose/testing
// https://developer.android.com/topic/performance/benchmarking/macrobenchmark-overview
// https://developer.android.com/studio/preview/compose-screenshot-testing
// https://developer.android.com/training/testing/instrumented-tests/androidx-test-libraries/runner
@LargeTest
class ComposeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @OptIn(ExperimentalTestApi::class)
    private fun login() {
        composeTestRule.onNodeWithText("Username")
            .performTextInput(InstrumentationRegistry.getArguments().getString("TUCAN_USERNAME")!!)
        composeTestRule.onNodeWithText("Password")
            .performTextInput(InstrumentationRegistry.getArguments().getString("TUCAN_PASSWORD")!!)
        composeTestRule.onNodeWithText("Login").performClick().assertIsNotEnabled()
        composeTestRule.waitUntilDoesNotExist(isNotEnabled().and(hasText("Login")), 10_000)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun successfulLogin() {
        //composeTestRule.onRoot().printToLog("Nodes")
        login()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun moduleResults() {
        login()
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.onNodeWithText("Modulergebnisse").performClick()
        // wait for loading indicator to be hidden
        composeTestRule.onRoot().printToLog("Nodes")
        composeTestRule.waitUntilDoesNotExist(hasContentDescriptionExactly("Loading"), 30_000)
        composeTestRule.onRoot().printToLog("Nodes")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun myExams() {
        login()
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
        composeTestRule.onNode(hasTextExactly("Meine Prüfungen").and(hasClickAction())).performClick()
        // wait for loading indicator to be hidden
        composeTestRule.onRoot().printToLog("Nodes")
        composeTestRule.waitUntilDoesNotExist(hasContentDescriptionExactly("Loading"), 30_000)
        composeTestRule.onRoot().printToLog("Nodes")
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalUuidApi::class)
    @Test
    fun wrongUsernameAndPassword() {
        // Avoid too many attempts error by using random username
        composeTestRule.onNodeWithText("Username").performTextInput(Uuid.random().toString())
        composeTestRule.onNodeWithText("Password").performTextInput(Uuid.random().toString())
        composeTestRule.onNodeWithText("Login").performClick().assertIsNotEnabled()
        composeTestRule.waitUntilDoesNotExist(isNotEnabled().and(hasText("Login")), 10_000)
        composeTestRule.waitUntilExactlyOneExists(hasText("Falscher Nutzername oder Passwort"), 10_000)
        composeTestRule.onNodeWithText("Login").assertIsEnabled()
    }
}