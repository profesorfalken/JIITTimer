package com.profesorfalken.jiittimer.timer

import com.google.common.truth.Truth.assertThat
import com.profesorfalken.jiittimer.model.WorkoutProgram
import org.junit.Test

class WorkoutTimelineTest {
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

    @Test
    fun `builds prepare work rest and cooldown in order`() {
        val timeline = WorkoutTimeline.build(
            program(prepare = 5, work = 30, rest = 10, cycles = 3, cooldown = 60),
        )

        assertThat(timeline.map { it.type }).containsExactly(
            IntervalType.Prepare,
            IntervalType.Work,
            IntervalType.Rest,
            IntervalType.Work,
            IntervalType.Rest,
            IntervalType.Work,
            IntervalType.Cooldown,
        ).inOrder()
        assertThat(WorkoutTimeline.totalDurationMs(timeline)).isEqualTo(175_000L)
    }

    @Test
    fun `does not add rest after the final round`() {
        val timeline = WorkoutTimeline.build(program(work = 30, rest = 10, cycles = 3))

        assertThat(timeline.count { it.type == IntervalType.Work }).isEqualTo(3)
        assertThat(timeline.count { it.type == IntervalType.Rest }).isEqualTo(2)
        assertThat(timeline.last().type).isEqualTo(IntervalType.Work)
    }

    @Test
    fun `omits optional intervals when zero`() {
        val timeline = WorkoutTimeline.build(
            program(prepare = 0, work = 20, rest = 0, cycles = 2, cooldown = 0),
        )

        assertThat(timeline.map { it.type }).containsExactly(
            IntervalType.Work,
            IntervalType.Work,
        ).inOrder()
    }

    @Test
    fun `locates boundaries correctly`() {
        val timeline = WorkoutTimeline.build(program(work = 10, rest = 5, cycles = 2))

        assertThat(WorkoutTimeline.locate(timeline, 0)?.index).isEqualTo(0)
        assertThat(WorkoutTimeline.locate(timeline, 9_999)?.index).isEqualTo(0)
        assertThat(WorkoutTimeline.locate(timeline, 9_999)?.intervalRemainingMs).isEqualTo(1L)
        assertThat(WorkoutTimeline.locate(timeline, 10_000)?.index).isEqualTo(1)
        assertThat(WorkoutTimeline.locate(timeline, 15_000)?.index).isEqualTo(2)
        assertThat(WorkoutTimeline.locate(timeline, 25_000)?.index).isEqualTo(2)
        assertThat(WorkoutTimeline.locate(timeline, 25_000)?.intervalRemainingMs).isEqualTo(0L)
        assertThat(WorkoutTimeline.locate(timeline, 25_000)?.totalRemainingMs).isEqualTo(0L)
    }

    @Test
    fun `clamps negative elapsed time`() {
        val timeline = WorkoutTimeline.build(program(work = 10))
        val position = WorkoutTimeline.locate(timeline, -500)

        assertThat(position?.index).isEqualTo(0)
        assertThat(position?.intervalRemainingMs).isEqualTo(10_000L)
    }

    @Test
    fun `returns null for empty timeline`() {
        assertThat(WorkoutTimeline.locate(emptyList(), 0)).isNull()
    }
}
