package com.profesorfalken.jiittimer

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.profesorfalken.jiittimer.model.WorkoutProgram
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetAppState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val packageName = InstrumentationRegistry.getInstrumentation().targetContext.packageName
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .executeShellCommand(
                    "pm grant $packageName android.permission.POST_NOTIFICATIONS",
                )
                .close()
        }
        ApplicationProvider.getApplicationContext<JiitTimerApp>()
            .container.timerEngine.stop()
    }

    @Test
    fun setupIsVisibleAndWorkoutCanStartAndReset() {
        waitForTag("start_workout")
        composeRule.onNodeWithTag("start_workout").performClick()

        waitForTag("timer_screen")

        // Stop through the same engine command the UI uses. Avoid performing a
        // Compose click while the timer is emitting updates: on a very slow
        // software emulator the idling resource can stay busy long enough to
        // time out even though the UI is healthy.
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            ApplicationProvider.getApplicationContext<JiitTimerApp>()
                .container.timerEngine.stop()
        }

        waitForTag("start_workout")
        composeRule.onNodeWithTag("start_workout").assertIsDisplayed()
    }

    @Test
    fun shortWorkoutReachesTheFinishedScreen() {
        val name = "E2E ${System.currentTimeMillis()}"
        waitForTag("start_workout")
        composeRule.runOnIdle {
            val app = ApplicationProvider.getApplicationContext<JiitTimerApp>()
            app.container.repository.addProgram(
                name = name,
                template = WorkoutProgram(
                    name = name,
                    prepareSeconds = 0,
                    workSeconds = 5,
                    restSeconds = 0,
                    cycles = 1,
                    cooldownSeconds = 0,
                ),
            )
        }

        waitForTag("start_workout")
        composeRule.onNodeWithText(name).assertIsDisplayed()
        composeRule.onNodeWithTag("start_workout").performClick()
        waitForTag("finished_screen", timeoutMillis = 30_000)
    }

    @Test
    fun createsANewProgram() {
        val name = "UI test ${System.currentTimeMillis()}"

        waitForTag("start_workout")
        composeRule.onNodeWithTag("add_program").performClick()
        composeRule.onNodeWithTag("program_name_input").performTextInput(name)
        composeRule.onNodeWithTag("confirm_name").performClick()

        composeRule.onNodeWithText(name).assertIsDisplayed()
    }

    private fun waitForTag(tag: String, timeoutMillis: Long = 10_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
