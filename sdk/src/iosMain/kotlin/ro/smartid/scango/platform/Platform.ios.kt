package ro.smartid.scango.platform

import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual abstract class PlatformContext

/** iOS needs nothing from the host, so the "context" is a singleton marker. */
object IosContext : PlatformContext()

actual fun nowMillis(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()

actual class ScanFeedback actual constructor(context: PlatformContext) {

    actual fun play(beep: Boolean, vibrate: Boolean) {
        if (beep) AudioServicesPlaySystemSound(TINK_SOUND_ID)
        if (vibrate) AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
    }

    actual fun release() = Unit

    private companion object {
        /** System "Tink" — the closest stock sound to a scanner confirmation beep. */
        const val TINK_SOUND_ID: UInt = 1057u
    }
}
