package com.profesorfalken.jiittimer

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.profesorfalken.jiittimer.audio.AndroidCuePlayer
import com.profesorfalken.jiittimer.data.ProgramRepository
import com.profesorfalken.jiittimer.timer.SystemTimeSource
import com.profesorfalken.jiittimer.timer.TimerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val cuePlayer = AndroidCuePlayer(appContext)
    val repository = ProgramRepository(appContext, appScope)
    val timerEngine = TimerEngine(
        timeSource = SystemTimeSource,
        scope = appScope,
        cuePlayer = cuePlayer,
    )

    init {
        appScope.launch {
            repository.state.collect { state ->
                cuePlayer.setEnabled(
                    sound = state.settings.soundEnabled,
                    vibration = state.settings.vibrationEnabled,
                )
            }
        }
    }
}

class JiitTimerApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        Notifications.ensureChannel(this)
        container = AppContainer(this)
    }
}

object Notifications {
    const val CHANNEL_ID = "workout_timer"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = context.getString(R.string.notification_channel_description)
            setShowBadge(false)
        }
        manager.createNotificationChannel(channel)
    }
}
