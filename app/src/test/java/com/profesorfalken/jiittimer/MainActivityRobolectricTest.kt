package com.profesorfalken.jiittimer

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], application = JiitTimerApp::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MainActivityRobolectricTest {
    @Test
    fun activityStartsWithTheComposeSetupScreen() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()

        assertThat(activity).isNotNull()
        assertThat(activity.isFinishing).isFalse()
        assertThat(activity.window.decorView).isNotNull()

        controller.pause().stop().destroy()
    }
}
