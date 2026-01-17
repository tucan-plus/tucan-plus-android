package de.selfmade4u.tucanplus

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescriptionExactly
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.hasTextExactly
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.printToLog
import androidx.test.platform.app.InstrumentationRegistry
import de.mannodermaus.junit5.compose.ComposeContext
import de.mannodermaus.junit5.compose.createAndroidComposeExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComposeTest {

    @JvmField
    @RegisterExtension
    @OptIn(ExperimentalTestApi::class)
    val composeTestRule = createAndroidComposeExtension<MainActivity>()

    @OptIn(ExperimentalTestApi::class)
    private fun ComposeContext.login() {
        onNodeWithText("Username")
            .performTextInput(InstrumentationRegistry.getArguments().getString("TUCAN_USERNAME")!!)
        onNodeWithText("Password")
            .performTextInput(InstrumentationRegistry.getArguments().getString("TUCAN_PASSWORD")!!)
        onNodeWithText("Login").performClick().assertIsNotEnabled()
        waitUntilDoesNotExist(isNotEnabled().and(hasText("Login")), 10_000)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun successfulLogin() {
        //composeTestRule.onRoot().printToLog("Nodes")
        composeTestRule.use {
            login()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun moduleResults() {
        composeTestRule.use {
            login()
            onNodeWithContentDescription("Menu").performClick()
            onNodeWithText("Modulergebnisse").performClick()
            // wait for loading indicator to be hidden
            onRoot().printToLog("Nodes")
            waitUntilDoesNotExist(hasContentDescriptionExactly("Loading"), 30_000)
            onRoot().printToLog("Nodes")
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun myExams() {
        composeTestRule.use {
            login()
            onNodeWithContentDescription("Menu").performClick()
            onNode(hasTextExactly("Meine Prüfungen").and(hasClickAction())).performClick()
            // wait for loading indicator to be hidden
            onRoot().printToLog("Nodes")
            waitUntilDoesNotExist(hasContentDescriptionExactly("Loading"), 30_000)
            onRoot().printToLog("Nodes")
        }

    }

    @OptIn(ExperimentalTestApi::class, ExperimentalUuidApi::class)
    @Test
    fun wrongUsernameAndPassword() {
        // Avoid too many attempts error by using random username
        composeTestRule.use {
            onNodeWithText("Username").performTextInput(Uuid.random().toString())
            onNodeWithText("Password").performTextInput(Uuid.random().toString())
            onNodeWithText("Login").performClick().assertIsNotEnabled()
            waitUntilDoesNotExist(isNotEnabled().and(hasText("Login")), 10_000)
            waitUntilExactlyOneExists(
                hasText("Falscher Nutzername oder Passwort"),
                10_000
            )
            onNodeWithText("Login").assertIsEnabled()
        }
    }
}