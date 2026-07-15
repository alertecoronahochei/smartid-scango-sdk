package ro.smartid.scango.platform

/**
 * The handle the SDK needs from the host to reach platform services.
 *
 * On Android this *is* `android.content.Context` (a typealias, so hosts just pass their
 * Application). On iOS nothing is needed, so it is an empty marker ([IosContext]).
 *
 * It must be declared `abstract`: an actual typealias cannot widen the modality of the
 * expect declaration, and `android.content.Context` is itself abstract.
 */
expect abstract class PlatformContext

/** Wall-clock time, used to stamp scans. */
expect fun nowMillis(): Long

/** Short beep / haptic tick fired after an accepted scan. */
expect class ScanFeedback(context: PlatformContext) {
    fun play(beep: Boolean, vibrate: Boolean)
    fun release()
}
