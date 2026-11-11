package com.profesorfalken.jiittimer.timer

import com.profesorfalken.jiittimer.model.TimeFormat
import com.profesorfalken.jiittimer.model.WorkoutProgram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Drift-free workout timer.
 *
 * The engine stores an absolute start timestamp and derives every displayed
 * value from elapsed time, never from accumulated ticks. That means a delayed
 * tick (main thread blocked, Doze, notification update) catches up instead of
 * extending the workout. Pauses are represented by excluding paused wall time,
 * and skips shift the virtual elapsed position.
 */
class TimerEngine(
    private val timeSource: TimeSource,
    private val scope: CoroutineScope,
    private val cuePlayer: WorkoutCuePlayer = NoOpCuePlayer,
    private val tickMs: Long = DEFAULT_TICK_MS,
) {
    private data class Session(
        val program: WorkoutProgram,
        val intervals: List<WorkoutInterval>,
        val totalMs: Long,
        val startedAtMs: Long,
        var pausedAtMs: Long? = null,
        var pausedTotalMs: Long = 0L,
        var skippedMs: Long = 0L,
        var lastIntervalIndex: Int = -1,
        var lastCueSecond: Long = Long.MAX_VALUE,
    )

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private var session: Session? = null
    private var ticker: Job? = null

    @Synchronized
    fun start(program: WorkoutProgram) {
        val safeProgram = program.sanitized()
        val intervals = WorkoutTimeline.build(safeProgram)
        val totalMs = WorkoutTimeline.totalDurationMs(intervals)
        if (intervals.isEmpty() || totalMs <= 0L) return

        ticker?.cancel()
        ticker = null

        val now = timeSource.nowMs()
        val newSession = Session(
            program = safeProgram,
            intervals = intervals,
            totalMs = totalMs,
            startedAtMs = now,
        )
        session = newSession
        newSession.lastIntervalIndex = 0
        newSession.lastCueSecond = initialCueSecond(intervals.first().durationMs)
        _state.value = snapshot(newSession, now)
        cuePlayer.onIntervalStarted(intervals.first().type)
        startTicker()
    }

    @Synchronized
    fun pause() {
        val current = session ?: return
        if (current.pausedAtMs != null) return

        val now = timeSource.nowMs()
        if (currentElapsedMs(current, now) >= current.totalMs) {
            finish()
            return
        }

        current.pausedAtMs = now
        ticker?.cancel()
        ticker = null
        _state.value = snapshot(current, now)
        cuePlayer.onPaused()
    }

    @Synchronized
    fun resume() {
        val current = session ?: return
        val pausedAt = current.pausedAtMs ?: return

        val now = timeSource.nowMs()
        current.pausedTotalMs += (now - pausedAt).coerceAtLeast(0L)
        current.pausedAtMs = null
        if (currentElapsedMs(current, now) >= current.totalMs) {
            finish()
            return
        }

        _state.value = snapshot(current, now)
        cuePlayer.onResumed()
        startTicker()
    }

    @Synchronized
    fun skip() {
        val current = session ?: return
        val now = timeSource.nowMs()
        val elapsed = currentElapsedMs(current, now)
        if (elapsed >= current.totalMs) {
            finish()
            return
        }

        val position = WorkoutTimeline.locate(current.intervals, elapsed) ?: run {
            finish()
            return
        }
        current.skippedMs += position.intervalRemainingMs

        if (currentElapsedMs(current, now) >= current.totalMs) {
            finish()
            return
        }

        val nextPosition = WorkoutTimeline.locate(
            current.intervals,
            currentElapsedMs(current, now),
        ) ?: run {
            finish()
            return
        }

        current.lastIntervalIndex = nextPosition.index
        current.lastCueSecond = initialCueSecond(nextPosition.interval.durationMs)
        _state.value = snapshot(current, now, nextPosition)
        cuePlayer.onIntervalStarted(nextPosition.interval.type)
    }

    @Synchronized
    fun stop() {
        val wasActive = _state.value.isActive
        ticker?.cancel()
        ticker = null
        session = null
        _state.value = TimerState()
        if (wasActive) cuePlayer.onStopped()
    }

    @Synchronized
    fun tick() {
        val current = session ?: return
        if (current.pausedAtMs != null) return

        val now = timeSource.nowMs()
        val elapsed = currentElapsedMs(current, now)
        if (elapsed >= current.totalMs) {
            finish()
            return
        }

        val position = WorkoutTimeline.locate(current.intervals, elapsed) ?: run {
            finish()
            return
        }

        if (position.index != current.lastIntervalIndex) {
            current.lastIntervalIndex = position.index
            current.lastCueSecond = initialCueSecond(position.interval.durationMs)
            cuePlayer.onIntervalStarted(position.interval.type)
        }

        val remainingSeconds = TimeFormat.ceilingSeconds(position.intervalRemainingMs)
        if (remainingSeconds in 1..3 && remainingSeconds < current.lastCueSecond) {
            current.lastCueSecond = remainingSeconds
            cuePlayer.onCountdown(remainingSeconds)
        }

        // Publish only when something the UI displays changes. The 100 ms ticker
        // is still used for accurate cues; emitting 10 state updates per second
        // kept Compose permanently busy and made UI tests time out.
        val next = snapshot(current, now, position)
        val previous = _state.value
        if (
            previous.status != next.status ||
            previous.currentIndex != next.currentIndex ||
            previous.remainingSeconds != next.remainingSeconds ||
            previous.totalRemainingSeconds != next.totalRemainingSeconds
        ) {
            _state.value = next
        }
    }

    private fun startTicker() {
        ticker?.cancel()
        ticker = scope.launch {
            while (isActive) {
                tick()
                if (_state.value.status != TimerStatus.Running) break
                delay(tickMs)
            }
        }
    }

    private fun finish() {
        val current = session ?: return
        ticker?.cancel()
        ticker = null
        session = null

        val lastIndex = current.intervals.lastIndex
        val lastInterval = current.intervals[lastIndex]
        _state.value = TimerState(
            status = TimerStatus.Finished,
            programId = current.program.id,
            programName = current.program.name,
            intervals = current.intervals,
            currentIndex = lastIndex,
            interval = lastInterval,
            intervalRemainingMs = 0L,
            intervalDurationMs = lastInterval.durationMs,
            totalRemainingMs = 0L,
            totalDurationMs = current.totalMs,
            elapsedMs = current.totalMs,
        )
        cuePlayer.onFinished()
    }

    private fun snapshot(session: Session, now: Long): TimerState {
        val position = WorkoutTimeline.locate(
            session.intervals,
            currentElapsedMs(session, now),
        ) ?: return _state.value
        return snapshot(session, now, position)
    }

    private fun snapshot(
        session: Session,
        now: Long,
        position: TimelinePosition,
    ): TimerState = TimerState(
        status = if (session.pausedAtMs != null) TimerStatus.Paused else TimerStatus.Running,
        programId = session.program.id,
        programName = session.program.name,
        intervals = session.intervals,
        currentIndex = position.index,
        interval = position.interval,
        intervalRemainingMs = position.intervalRemainingMs,
        intervalDurationMs = position.interval.durationMs,
        totalRemainingMs = position.totalRemainingMs,
        totalDurationMs = session.totalMs,
        elapsedMs = currentElapsedMs(session, now),
    )

    private fun currentElapsedMs(session: Session, now: Long): Long {
        val effectiveNow = session.pausedAtMs ?: now
        return (effectiveNow - session.startedAtMs - session.pausedTotalMs + session.skippedMs)
            .coerceAtLeast(0L)
    }

    private fun initialCueSecond(durationMs: Long): Long {
        val seconds = TimeFormat.ceilingSeconds(durationMs)
        return if (durationMs <= 3000L) seconds + 1L else seconds
    }

    companion object {
        const val DEFAULT_TICK_MS = 100L
    }
}
