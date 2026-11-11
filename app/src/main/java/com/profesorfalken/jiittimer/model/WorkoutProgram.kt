package com.profesorfalken.jiittimer.model

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * A reusable HIIT workout configuration.
 *
 * Rest is only scheduled between rounds. The old 2018 app also rested after the
 * last round; that was fixed in 2.0 because it extended every session by one
 * unnecessary rest period.
 */
@Serializable
data class WorkoutProgram(
    val id: String = UUID.randomUUID().toString(),
    val name: String = DEFAULT_NAME,
    val prepareSeconds: Int = DEFAULT_PREPARE_SECONDS,
    val workSeconds: Int = DEFAULT_WORK_SECONDS,
    val restSeconds: Int = DEFAULT_REST_SECONDS,
    val cycles: Int = DEFAULT_CYCLES,
    val cooldownSeconds: Int = DEFAULT_COOLDOWN_SECONDS,
) {
    val totalSeconds: Int
        get() = prepareSeconds +
            workSeconds * cycles +
            restSeconds * (cycles - 1).coerceAtLeast(0) +
            cooldownSeconds

    /**
     * Keeps persisted or user-edited values inside the limits the UI promises.
     * This is the single source of truth used before saving or starting.
     */
    fun sanitized(): WorkoutProgram = copy(
        name = name.trim().take(MAX_NAME_LENGTH).ifBlank { DEFAULT_NAME },
        prepareSeconds = prepareSeconds.coerceIn(MIN_PREPARE, MAX_PREPARE),
        workSeconds = workSeconds.coerceIn(MIN_WORK, MAX_WORK),
        restSeconds = restSeconds.coerceIn(MIN_REST, MAX_REST),
        cycles = cycles.coerceIn(MIN_CYCLES, MAX_CYCLES),
        cooldownSeconds = cooldownSeconds.coerceIn(MIN_COOLDOWN, MAX_COOLDOWN),
    )

    companion object {
        const val MAX_NAME_LENGTH = 40
        const val MIN_PREPARE = 0
        const val MAX_PREPARE = 120
        const val MIN_WORK = 5
        const val MAX_WORK = 600
        const val MIN_REST = 0
        const val MAX_REST = 600
        const val MIN_CYCLES = 1
        const val MAX_CYCLES = 50
        const val MIN_COOLDOWN = 0
        const val MAX_COOLDOWN = 600
        const val TIME_STEP_SECONDS = 5

        const val DEFAULT_NAME = "Default HIIT"
        const val DEFAULT_PREPARE_SECONDS = 5
        const val DEFAULT_WORK_SECONDS = 30
        const val DEFAULT_REST_SECONDS = 10
        const val DEFAULT_CYCLES = 8
        const val DEFAULT_COOLDOWN_SECONDS = 60

        const val DEFAULT_ID = "default"

        fun default(): WorkoutProgram = WorkoutProgram(id = DEFAULT_ID).sanitized()
    }
}
