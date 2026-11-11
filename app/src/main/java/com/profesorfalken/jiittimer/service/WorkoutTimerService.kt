package com.profesorfalken.jiittimer.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.profesorfalken.jiittimer.JiitTimerApp
import com.profesorfalken.jiittimer.MainActivity
import com.profesorfalken.jiittimer.Notifications
import com.profesorfalken.jiittimer.R
import com.profesorfalken.jiittimer.model.TimeFormat
import com.profesorfalken.jiittimer.timer.IntervalType
import com.profesorfalken.jiittimer.timer.TimerEngine
import com.profesorfalken.jiittimer.timer.TimerState
import com.profesorfalken.jiittimer.timer.TimerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Keeps the workout alive while the UI is backgrounded or the screen is off.
 * The engine is process-scoped; this service only promotes the process to the
 * foreground, shows the persistent notification and holds a partial wake lock.
 */
class WorkoutTimerService : Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var engine: TimerEngine
    private var observing: Job? = null
    private var foregroundStarted = false
    private var started = false
    private var lastNotificationKey: String? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        engine = (application as JiitTimerApp).container.timerEngine
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                started = true
                val state = engine.state.value
                if (state.status == TimerStatus.Running || state.status == TimerStatus.Paused) {
                    ensureForeground(state)
                    observeEngine()
                } else {
                    stopSelf()
                }
            }
            ACTION_PAUSE -> engine.pause()
            ACTION_RESUME -> engine.resume()
            ACTION_SKIP -> engine.skip()
            ACTION_STOP -> {
                engine.stop()
                stopSelf()
            }
        }

        if (!started) stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        observing?.cancel()
        serviceScope.cancel()
        releaseWakeLock()
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
        super.onDestroy()
    }

    private fun observeEngine() {
        if (observing != null) return
        observing = serviceScope.launch {
            engine.state.collect { state ->
                if (!started) return@collect
                when (state.status) {
                    TimerStatus.Running, TimerStatus.Paused -> {
                        ensureForeground(state)
                        updateWakeLock(state)
                        updateNotification(state)
                    }
                    TimerStatus.Finished, TimerStatus.Idle -> {
                        releaseWakeLock()
                        foregroundStarted = false
                        started = false
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        NotificationManagerCompat.from(this@WorkoutTimerService)
                            .cancel(NOTIFICATION_ID)
                        stopSelf()
                    }
                }
            }
        }
    }

    private fun ensureForeground(state: TimerState) {
        if (foregroundStarted) return

        val notification = buildNotification(state)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        foregroundStarted = true
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(state: TimerState) {
        val key = "${state.status}-${state.currentIndex}-${state.remainingSeconds}-${state.programName}"
        if (key == lastNotificationKey) return
        lastNotificationKey = key

        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) return
        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, buildNotification(state))
    }

    private fun buildNotification(state: TimerState): Notification {
        val interval = state.interval
        val title = when (interval?.type) {
            IntervalType.Prepare -> getString(R.string.notification_prepare)
            IntervalType.Work -> getString(
                R.string.notification_work,
                state.currentCycle ?: 0,
                state.totalCycles,
            )
            IntervalType.Rest -> getString(
                R.string.notification_rest,
                state.currentCycle ?: 0,
                state.totalCycles,
            )
            IntervalType.Cooldown -> getString(R.string.notification_cooldown)
            null -> getString(R.string.app_name)
        }
        val content = getString(
            R.string.notification_text,
            TimeFormat.clock(state.remainingSeconds),
            TimeFormat.clock(state.totalRemainingSeconds),
        )

        val builder = NotificationCompat.Builder(this, Notifications.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_timer)
            .setContentTitle(title)
            .setContentText(content)
            .setContentIntent(contentIntent())
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setProgress(100, (state.totalProgress * 100f).roundToInt(), false)

        if (state.status == TimerStatus.Paused) {
            builder.addAction(
                android.R.drawable.ic_media_play,
                getString(R.string.action_resume),
                serviceAction(ACTION_RESUME, REQUEST_RESUME),
            )
        } else {
            builder.addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.action_pause),
                serviceAction(ACTION_PAUSE, REQUEST_PAUSE),
            )
        }
        builder.addAction(
            android.R.drawable.ic_media_next,
            getString(R.string.action_skip),
            serviceAction(ACTION_SKIP, REQUEST_SKIP),
        )
        builder.addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            getString(R.string.action_stop),
            serviceAction(ACTION_STOP, REQUEST_STOP),
        )

        return builder.build()
    }

    private fun contentIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return PendingIntent.getActivity(
            this,
            REQUEST_CONTENT,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun serviceAction(action: String, requestCode: Int): PendingIntent {
        val intent = Intent(this, WorkoutTimerService::class.java).setAction(action)
        return PendingIntent.getService(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun updateWakeLock(state: TimerState) {
        if (state.status != TimerStatus.Running) {
            releaseWakeLock()
            return
        }

        if (wakeLock == null) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "$packageName:WorkoutTimer",
            ).apply {
                setReferenceCounted(false)
            }
        }

        if (wakeLock?.isHeld != true) {
            val timeout = (state.totalRemainingMs + WAKE_LOCK_GRACE_MS).coerceAtLeast(WAKE_LOCK_GRACE_MS)
            wakeLock?.acquire(timeout)
        }
    }

    private fun releaseWakeLock() {
        wakeLock?.let { lock ->
            if (lock.isHeld) lock.release()
        }
        wakeLock = null
    }

    companion object {
        const val ACTION_START = "com.profesorfalken.jiittimer.action.START"
        const val ACTION_PAUSE = "com.profesorfalken.jiittimer.action.PAUSE"
        const val ACTION_RESUME = "com.profesorfalken.jiittimer.action.RESUME"
        const val ACTION_SKIP = "com.profesorfalken.jiittimer.action.SKIP"
        const val ACTION_STOP = "com.profesorfalken.jiittimer.action.STOP"

        private const val NOTIFICATION_ID = 41
        private const val REQUEST_CONTENT = 10
        private const val REQUEST_PAUSE = 11
        private const val REQUEST_RESUME = 12
        private const val REQUEST_SKIP = 13
        private const val REQUEST_STOP = 14
        private const val WAKE_LOCK_GRACE_MS = 60_000L

        fun start(context: Context) {
            val intent = Intent(context, WorkoutTimerService::class.java).setAction(ACTION_START)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
