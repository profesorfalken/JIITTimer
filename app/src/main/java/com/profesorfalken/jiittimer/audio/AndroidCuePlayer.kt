package com.profesorfalken.jiittimer.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.profesorfalken.jiittimer.R
import com.profesorfalken.jiittimer.timer.IntervalType
import com.profesorfalken.jiittimer.timer.WorkoutCuePlayer

/**
 * Short, low-latency cues through SoundPool. Audio focus is requested for each
 * cue and abandoned shortly afterwards, and vibration patterns differ between
 * work, rest and the final finish signal.
 */
class AndroidCuePlayer(context: Context) : WorkoutCuePlayer {
    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val attributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(attributes)
        .build()

    private val workSound = soundPool.load(appContext, R.raw.beep_work, 1)
    private val restSound = soundPool.load(appContext, R.raw.beep_rest, 1)
    private val prepareSound = soundPool.load(appContext, R.raw.beep_prepare, 1)
    private val countSound = soundPool.load(appContext, R.raw.beep_count, 1)
    private val finishSound = soundPool.load(appContext, R.raw.beep_finish, 1)

    private val focusRequest: AudioFocusRequest? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attributes)
                .setOnAudioFocusChangeListener { }
                .build()
        } else {
            null
        }

    private val abandonFocus = Runnable { abandonAudioFocus() }

    @Volatile
    private var soundEnabled: Boolean = true

    @Volatile
    private var vibrationEnabled: Boolean = true

    fun setEnabled(sound: Boolean, vibration: Boolean) {
        soundEnabled = sound
        vibrationEnabled = vibration
    }

    override fun onIntervalStarted(type: IntervalType) {
        when (type) {
            IntervalType.Prepare -> play(prepareSound, longArrayOf(0, 45))
            IntervalType.Work -> play(workSound, longArrayOf(0, 80, 60, 80))
            IntervalType.Rest -> play(restSound, longArrayOf(0, 55))
            IntervalType.Cooldown -> play(restSound, longArrayOf(0, 45))
        }
    }

    override fun onCountdown(seconds: Long) {
        play(countSound, longArrayOf(0, 22))
    }

    override fun onPaused() {
        play(countSound, longArrayOf(0, 35, 70, 35))
    }

    override fun onResumed() {
        play(countSound, longArrayOf(0, 35))
    }

    override fun onFinished() {
        play(finishSound, longArrayOf(0, 100, 80, 100, 80, 250))
    }

    fun release() {
        handler.removeCallbacks(abandonFocus)
        abandonAudioFocus()
        soundPool.release()
    }

    private fun play(soundId: Int, vibration: LongArray) {
        if (!soundEnabled && !vibrationEnabled) return

        if (soundEnabled) {
            requestAudioFocus()
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
            handler.removeCallbacks(abandonFocus)
            handler.postDelayed(abandonFocus, FOCUS_HOLD_MS)
        }
        if (vibrationEnabled) vibrate(vibration)
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let(audioManager::requestAudioFocus)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
            )
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let(audioManager::abandonAudioFocusRequest)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun vibrate(pattern: LongArray) {
        val vibrator = vibrator() ?: return
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    private fun vibrator(): Vibrator? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            appContext.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    private companion object {
        const val FOCUS_HOLD_MS = 1_500L
    }
}
