package com.profesorfalken.jiittimer.timer

import com.profesorfalken.jiittimer.model.TimeFormat

enum class TimerStatus {
    Idle,
    Running,
    Paused,
    Finished,
}

data class TimerState(
    val status: TimerStatus = TimerStatus.Idle,
    val programId: String? = null,
    val programName: String = "",
    val intervals: List<WorkoutInterval> = emptyList(),
    val currentIndex: Int = -1,
    val interval: WorkoutInterval? = null,
    val intervalRemainingMs: Long = 0L,
    val intervalDurationMs: Long = 0L,
    val totalRemainingMs: Long = 0L,
    val totalDurationMs: Long = 0L,
    val elapsedMs: Long = 0L,
) {
    val isActive: Boolean
        get() = status == TimerStatus.Running || status == TimerStatus.Paused

    val remainingSeconds: Long
        get() = TimeFormat.ceilingSeconds(intervalRemainingMs)

    val totalRemainingSeconds: Long
        get() = TimeFormat.ceilingSeconds(totalRemainingMs)

    val intervalProgress: Float
        get() = if (intervalDurationMs > 0L) {
            (1f - intervalRemainingMs.toFloat() / intervalDurationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val totalProgress: Float
        get() = if (totalDurationMs > 0L) {
            (elapsedMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    val currentCycle: Int?
        get() = interval?.cycleNumber

    val totalCycles: Int
        get() = interval?.totalCycles ?: 0

    val nextInterval: WorkoutInterval?
        get() = intervals.getOrNull(currentIndex + 1)
}
