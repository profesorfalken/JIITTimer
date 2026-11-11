package com.profesorfalken.jiittimer.timer

import com.profesorfalken.jiittimer.model.WorkoutProgram

enum class IntervalType {
    Prepare,
    Work,
    Rest,
    Cooldown,
}

data class WorkoutInterval(
    val type: IntervalType,
    val durationMs: Long,
    val cycleNumber: Int? = null,
    val totalCycles: Int = 0,
)

data class TimelinePosition(
    val index: Int,
    val interval: WorkoutInterval,
    val intervalElapsedMs: Long,
    val intervalRemainingMs: Long,
    val totalRemainingMs: Long,
)

object WorkoutTimeline {
    /**
     * Builds the immutable timeline for a program. A rest interval is inserted
     * only between work rounds, never after the last one.
     */
    fun build(program: WorkoutProgram): List<WorkoutInterval> {
        val safeProgram = program.sanitized()
        val intervals = mutableListOf<WorkoutInterval>()

        if (safeProgram.prepareSeconds > 0) {
            intervals += WorkoutInterval(
                type = IntervalType.Prepare,
                durationMs = safeProgram.prepareSeconds * 1000L,
            )
        }

        for (cycle in 1..safeProgram.cycles) {
            intervals += WorkoutInterval(
                type = IntervalType.Work,
                durationMs = safeProgram.workSeconds * 1000L,
                cycleNumber = cycle,
                totalCycles = safeProgram.cycles,
            )
            if (cycle < safeProgram.cycles && safeProgram.restSeconds > 0) {
                intervals += WorkoutInterval(
                    type = IntervalType.Rest,
                    durationMs = safeProgram.restSeconds * 1000L,
                    cycleNumber = cycle,
                    totalCycles = safeProgram.cycles,
                )
            }
        }

        if (safeProgram.cooldownSeconds > 0) {
            intervals += WorkoutInterval(
                type = IntervalType.Cooldown,
                durationMs = safeProgram.cooldownSeconds * 1000L,
            )
        }

        return intervals
    }

    fun totalDurationMs(intervals: List<WorkoutInterval>): Long =
        intervals.sumOf { it.durationMs }

    /**
     * Locates an absolute session elapsed time within the timeline. Values past
     * the end are clamped so callers can use this while catching up after a
     * delayed tick.
     */
    fun locate(intervals: List<WorkoutInterval>, elapsedMs: Long): TimelinePosition? {
        if (intervals.isEmpty()) return null

        val total = totalDurationMs(intervals)
        val elapsed = elapsedMs.coerceIn(0L, total)
        var start = 0L

        intervals.forEachIndexed { index, interval ->
            val end = start + interval.durationMs
            if (elapsed < end || index == intervals.lastIndex) {
                val within = (elapsed - start).coerceIn(0L, interval.durationMs)
                return TimelinePosition(
                    index = index,
                    interval = interval,
                    intervalElapsedMs = within,
                    intervalRemainingMs = (interval.durationMs - within).coerceAtLeast(0L),
                    totalRemainingMs = (total - elapsed).coerceAtLeast(0L),
                )
            }
            start = end
        }

        return null
    }
}
