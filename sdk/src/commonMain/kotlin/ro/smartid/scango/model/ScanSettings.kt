package ro.smartid.scango.model

import kotlinx.serialization.Serializable

/**
 * User-tunable scanner behaviour, edited from the SDK's settings screen and
 * persisted on-device (SharedPreferences on Android, NSUserDefaults on iOS).
 */
@Serializable
data class ScanSettings(
    /** Symbologies the camera actively looks for. Fewer formats = faster, fewer false reads. */
    val enabledFormats: Set<BarcodeFormat> = BarcodeFormat.DEFAULTS,

    /** true: keep scanning after a hit (scan&go). false: return to the host after the first hit. */
    val continuousScan: Boolean = true,

    /** true: the same code can be added to the list several times. */
    val allowDuplicates: Boolean = false,

    /** Short confirmation tone on every accepted scan. */
    val beepOnScan: Boolean = true,

    /** Haptic confirmation on every accepted scan. */
    val vibrateOnScan: Boolean = true,

    /** Turn the torch on as soon as the scanner screen opens. */
    val torchOnStart: Boolean = false,

    /** Guard window in which the same value is ignored, to stop a held-still code from spamming the list. */
    val rescanCooldownMillis: Long = DEFAULT_RESCAN_COOLDOWN_MILLIS,

    /** Stop accepting scans after this many items. 0 = unlimited. */
    val maxItems: Int = 0,
) {
    val isUnlimited: Boolean get() = maxItems <= 0

    companion object {
        const val DEFAULT_RESCAN_COOLDOWN_MILLIS: Long = 1_500L

        val DEFAULT: ScanSettings = ScanSettings()
    }
}
