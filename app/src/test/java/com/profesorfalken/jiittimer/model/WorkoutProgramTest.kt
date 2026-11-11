package com.profesorfalken.jiittimer.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WorkoutProgramTest {
    @Test
    fun `total time counts rest only between rounds`() {
        val program = WorkoutProgram(
            prepareSeconds = 5,
            workSeconds = 30,
            restSeconds = 10,
            cycles = 3,
            cooldownSeconds = 60,
        )

        assertThat(program.totalSeconds).isEqualTo(5 + 30 * 3 + 10 * 2 + 60)
    }

    @Test
    fun `single round has no rest`() {
        val program = WorkoutProgram(
            prepareSeconds = 0,
            workSeconds = 30,
            restSeconds = 10,
            cycles = 1,
            cooldownSeconds = 0,
        )

        assertThat(program.totalSeconds).isEqualTo(30)
    }

    @Test
    fun `sanitization clamps every field`() {
        val dirty = WorkoutProgram(
            name = "   ",
            prepareSeconds = -10,
            workSeconds = 0,
            restSeconds = -1,
            cycles = 999,
            cooldownSeconds = 9999,
        )

        val safe = dirty.sanitized()

        assertThat(safe.name).isEqualTo(WorkoutProgram.DEFAULT_NAME)
        assertThat(safe.prepareSeconds).isEqualTo(WorkoutProgram.MIN_PREPARE)
        assertThat(safe.workSeconds).isEqualTo(WorkoutProgram.MIN_WORK)
        assertThat(safe.restSeconds).isEqualTo(WorkoutProgram.MIN_REST)
        assertThat(safe.cycles).isEqualTo(WorkoutProgram.MAX_CYCLES)
        assertThat(safe.cooldownSeconds).isEqualTo(WorkoutProgram.MAX_COOLDOWN)
    }

    @Test
    fun `sanitization truncates long names`() {
        val safe = WorkoutProgram(name = "x".repeat(100)).sanitized()

        assertThat(safe.name).hasLength(WorkoutProgram.MAX_NAME_LENGTH)
    }

    @Test
    fun `default program is valid`() {
        val program = WorkoutProgram.default()

        assertThat(program.id).isEqualTo(WorkoutProgram.DEFAULT_ID)
        assertThat(program.totalSeconds).isGreaterThan(0)
    }
}
