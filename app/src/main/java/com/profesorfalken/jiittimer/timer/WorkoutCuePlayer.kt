package com.profesorfalken.jiittimer.timer

fun interface TimeSource {
    fun nowMs(): Long
}

interface WorkoutCuePlayer {
    fun onIntervalStarted(type: IntervalType) = Unit
    fun onCountdown(seconds: Long) = Unit
    fun onPaused() = Unit
    fun onResumed() = Unit
    fun onStopped() = Unit
    fun onFinished() = Unit
}

object NoOpCuePlayer : WorkoutCuePlayer
