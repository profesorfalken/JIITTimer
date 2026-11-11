package com.profesorfalken.jiittimer.timer

import com.google.common.truth.Truth.assertThat
import com.profesorfalken.jiittimer.model.WorkoutProgram
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerEngineTest {
    private fun program(
        prepare: Int = 0,
        work: Int = 10,
        rest: Int = 0,
        cycles: Int = 1,
        cooldown: Int = 0,
    ) = WorkoutProgram(
        id = "test",
        name = "Test",
        prepareSeconds = prepare,
        workSeconds = work,
        restSeconds = rest,
        cycles = cycles,
        cooldownSeconds = cooldown,
    )

    private fun TestScope.engine(cues: WorkoutCuePlayer = NoOpCuePlayer) = TimerEngine(
        timeSource = { testScheduler.currentTime },
        scope = backgroundScope,
        cuePlayer = cues,
    )

    @Test
    fun `counts down from the full interval and finishes at zero`() = runTest {
        val engine = engine()
        engine.start(program(work = 10))

        assertThat(engine.state.value.status).isEqualTo(TimerStatus.Running)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(10)

        advanceTimeBy(1_000)
        runCurrent()
        assertThat(engine.state.value.remainingSeconds).isEqualTo(9)

        advanceTimeBy(9_000)
        runCurrent()
        assertThat(engine.state.value.status).isEqualTo(TimerStatus.Finished)
        assertThat(engine.state.value.totalRemainingMs).isEqualTo(0L)
        assertThat(engine.state.value.elapsedMs).isEqualTo(10_000L)
        assertThat(engine.state.value.currentIndex).isEqualTo(0)
    }

    @Test
    fun `walks through prepare work rest and cooldown`() = runTest {
        val engine = engine()
        engine.start(program(prepare = 2, work = 5, rest = 2, cycles = 2, cooldown = 1))

        assertThat(engine.state.value.interval?.type).isEqualTo(IntervalType.Prepare)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(2)

        advanceTimeBy(2_000)
        runCurrent()
        assertThat(engine.state.value.interval?.type).isEqualTo(IntervalType.Work)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(5)

        advanceTimeBy(5_000)
        runCurrent()
        assertThat(engine.state.value.interval?.type).isEqualTo(IntervalType.Rest)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(2)

        advanceTimeBy(2_000)
        runCurrent()
        assertThat(engine.state.value.interval?.type).isEqualTo(IntervalType.Work)
        assertThat(engine.state.value.currentCycle).isEqualTo(2)

        advanceTimeBy(5_000)
        runCurrent()
        assertThat(engine.state.value.interval?.type).isEqualTo(IntervalType.Cooldown)

        advanceTimeBy(1_000)
        runCurrent()
        assertThat(engine.state.value.status).isEqualTo(TimerStatus.Finished)
    }

    @Test
    fun `pause freezes the elapsed time and resume continues from there`() = runTest {
        val engine = engine()
        engine.start(program(work = 10))

        advanceTimeBy(1_000)
        runCurrent()
        engine.pause()

        assertThat(engine.state.value.status).isEqualTo(TimerStatus.Paused)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(9)

        advanceTimeBy(5_000)
        runCurrent()
        assertThat(engine.state.value.status).isEqualTo(TimerStatus.Paused)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(9)

        engine.resume()
        advanceTimeBy(2_000)
        runCurrent()

        assertThat(engine.state.value.status).isEqualTo(TimerStatus.Running)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(7)
    }

    @Test
    fun `skip advances to the next interval immediately`() = runTest {
        val engine = engine()
        engine.start(program(work = 10, rest = 10, cycles = 2))

        advanceTimeBy(2_000)
        runCurrent()
        assertThat(engine.state.value.remainingSeconds).isEqualTo(8)

        engine.skip()

        assertThat(engine.state.value.interval?.type).isEqualTo(IntervalType.Rest)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(10)
        assertThat(engine.state.value.totalRemainingSeconds).isEqualTo(20)

        advanceTimeBy(10_000)
        runCurrent()
        assertThat(engine.state.value.interval?.type).isEqualTo(IntervalType.Work)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(10)

        engine.skip()
        assertThat(engine.state.value.status).isEqualTo(TimerStatus.Finished)
    }

    @Test
    fun `skip while paused keeps the session paused`() = runTest {
        val engine = engine()
        engine.start(program(work = 10, rest = 10, cycles = 2))

        advanceTimeBy(2_000)
        runCurrent()
        engine.pause()
        engine.skip()

        assertThat(engine.state.value.status).isEqualTo(TimerStatus.Paused)
        assertThat(engine.state.value.interval?.type).isEqualTo(IntervalType.Rest)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(10)

        engine.resume()
        assertThat(engine.state.value.status).isEqualTo(TimerStatus.Running)
    }

    @Test
    fun `stop resets the state and cannot be resumed`() = runTest {
        val engine = engine()
        engine.start(program(work = 10))

        advanceTimeBy(2_000)
        runCurrent()
        engine.stop()

        assertThat(engine.state.value.status).isEqualTo(TimerStatus.Idle)
        assertThat(engine.state.value.remainingSeconds).isEqualTo(0)

        advanceTimeBy(10_000)
        runCurrent()
        assertThat(engine.state.value.status).isEqualTo(TimerStatus.Idle)
    }

    @Test
    fun `emits interval, countdown and finish cues`() = runTest {
        val cues = RecordingCuePlayer()
        val engine = engine(cues)
        engine.start(program(work = 10))

        assertThat(cues.events).contains("start:Work")

        advanceTimeBy(7_000)
        runCurrent()
        advanceTimeBy(1_000)
        runCurrent()
        advanceTimeBy(1_000)
        runCurrent()
        advanceTimeBy(1_000)
        runCurrent()

        assertThat(cues.events).containsAtLeast(
            "start:Work",
            "countdown:3",
            "countdown:2",
            "countdown:1",
            "finished",
        ).inOrder()
    }

    private class RecordingCuePlayer : WorkoutCuePlayer {
        val events = mutableListOf<String>()

        override fun onIntervalStarted(type: IntervalType) {
            events += "start:${type.name}"
        }

        override fun onCountdown(seconds: Long) {
            events += "countdown:$seconds"
        }

        override fun onPaused() {
            events += "paused"
        }

        override fun onResumed() {
            events += "resumed"
        }

        override fun onStopped() {
            events += "stopped"
        }

        override fun onFinished() {
            events += "finished"
        }
    }
}
