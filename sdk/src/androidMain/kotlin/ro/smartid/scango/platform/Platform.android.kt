package ro.smartid.scango.platform

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

actual typealias PlatformContext = Context

actual fun nowMillis(): Long = System.currentTimeMillis()

actual class ScanFeedback actual constructor(context: PlatformContext) {

    private val appContext: Context = context.applicationContext

    private val toneGenerator: ToneGenerator? by lazy {
        runCatching { ToneGenerator(AudioManager.STREAM_NOTIFICATION, TONE_VOLUME) }.getOrNull()
    }

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    actual fun play(beep: Boolean, vibrate: Boolean) {
        if (beep) {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, BEEP_DURATION_MS)
        }
        if (vibrate) {
            val device = vibrator ?: return
            if (!device.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                device.vibrate(VibrationEffect.createOneShot(VIBRATE_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                device.vibrate(VIBRATE_DURATION_MS)
            }
        }
    }

    actual fun release() {
        runCatching { toneGenerator?.release() }
    }

    private companion object {
        const val TONE_VOLUME = 80
        const val BEEP_DURATION_MS = 120
        const val VIBRATE_DURATION_MS = 40L
    }
}
