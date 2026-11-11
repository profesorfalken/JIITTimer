package com.profesorfalken.jiittimer.timer

import android.os.SystemClock

object SystemTimeSource : TimeSource {
    override fun nowMs(): Long = SystemClock.elapsedRealtime()
}
