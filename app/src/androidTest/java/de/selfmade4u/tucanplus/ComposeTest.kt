package de.selfmade4u.tucanplus

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
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

    @After
    fun clearDatastore() {
        runBlocking(Dispatchers.Main) {
            composeTestRule.activity.credentialSettingsDataStore.updateData { _ ->
                OptionalCredentialSettings(
                    null
                )
            }
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun successfulLogin() {
        //composeTestRule.onRoot().printToLog("Nodes")
        // In the Android Studio run configuration add these parameters
        composeTestRule.onNodeWithText("Username")
            .performTextInput(InstrumentationRegistry.getArguments().getString("TUCAN_USERNAME")!!)
        composeTestRule.onNodeWithText("Password")
            .performTextInput(InstrumentationRegistry.getArguments().getString("TUCAN_PASSWORD")!!)
        composeTestRule.onNodeWithText("Login").performClick().assertIsNotEnabled()
        composeTestRule.waitUntilDoesNotExist(isNotEnabled().and(hasText("Login")), 10_000)
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalUuidApi::class)
    @Test
    fun wrongUsernameAndPassword() {
        // Avoid too many attempts by using random username
        composeTestRule.onNodeWithText("Username").performTextInput(Uuid.random().toString())
        composeTestRule.onNodeWithText("Password").performTextInput(Uuid.random().toString())
        composeTestRule.onNodeWithText("Login").performClick().assertIsNotEnabled()
        composeTestRule.waitUntilDoesNotExist(isNotEnabled().and(hasText("Login")), 10_000)
        composeTestRule.waitUntilExactlyOneExists(hasText("Falscher Nutzername oder Passwort"), 10_000)
        composeTestRule.onNodeWithText("Login").assertIsEnabled()
    }
}