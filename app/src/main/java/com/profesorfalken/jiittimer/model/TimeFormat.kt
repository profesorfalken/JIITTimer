package com.profesorfalken.jiittimer.model

import java.util.Locale

/** Locale-independent duration formatting used by the UI, notification and tests. */
object TimeFormat {
    fun clock(totalSeconds: Long): String {
        val safeSeconds = totalSeconds.coerceAtLeast(0)
        val hours = safeSeconds / 3600
        val minutes = (safeSeconds % 3600) / 60
        val seconds = safeSeconds % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }
    }

    /** Ceiling conversion so a countdown never displays 00:00 before it is over. */
    fun ceilingSeconds(remainingMs: Long): Long =
        if (remainingMs <= 0L) 0L else (remainingMs + 999L) / 1000L
}
