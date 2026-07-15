package ro.smartid.scango.session

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ro.smartid.scango.model.BarcodeFormat
import ro.smartid.scango.model.ScanSettings
import ro.smartid.scango.model.ScannedBarcode
import ro.smartid.scango.platform.nowMillis

/** What the session decided to do with a raw detection. */
sealed interface ScanOutcome {
    data class Accepted(val barcode: ScannedBarcode) : ScanOutcome
    data class DuplicateMerged(val barcode: ScannedBarcode) : ScanOutcome
    data object IgnoredDuplicate : ScanOutcome
    data object IgnoredCooldown : ScanOutcome
    data object IgnoredFormatDisabled : ScanOutcome
    data object IgnoredLimitReached : ScanOutcome
}

/**
 * The brain of scan&go: turns a firehose of raw detections into the clean list the
 * host gets back. Pure Kotlin, no camera, no UI — which is exactly why it is unit-testable
 * on the JVM without a device.
 */
class ScanSession(
    settings: ScanSettings = ScanSettings.DEFAULT,
    /** Injectable so the cooldown logic can be unit-tested without sleeping. */
    private val clock: () -> Long = ::nowMillis,
) {

    private val _items = MutableStateFlow<List<ScannedBarcode>>(emptyList())
    val items: StateFlow<List<ScannedBarcode>> = _items.asStateFlow()

    private var settings: ScanSettings = settings
    private val lastSeenAt = mutableMapOf<String, Long>()

    val itemCount: Int get() = _items.value.size

    fun applySettings(settings: ScanSettings) {
        this.settings = settings
    }

    /** Feed one raw detection. Returns what happened, so the UI knows whether to beep. */
    fun submit(value: String, format: BarcodeFormat): ScanOutcome {
        val now = clock()

        if (format !in settings.enabledFormats) return ScanOutcome.IgnoredFormatDisabled

        val previousSighting = lastSeenAt[value]
        if (previousSighting != null && now - previousSighting < settings.rescanCooldownMillis) {
            return ScanOutcome.IgnoredCooldown
        }
        lastSeenAt[value] = now

        val existingIndex = _items.value.indexOfFirst { it.value == value }
        if (existingIndex >= 0 && !settings.allowDuplicates) {
            // Keep one row, but tell the user we saw it again.
            val existing = _items.value[existingIndex]
            val merged = existing.copy(count = existing.count + 1, scannedAtMillis = now)
            _items.value = _items.value.toMutableList().also { it[existingIndex] = merged }
            return ScanOutcome.DuplicateMerged(merged)
        }

        if (!settings.isUnlimited && _items.value.size >= settings.maxItems) {
            return ScanOutcome.IgnoredLimitReached
        }

        val barcode = ScannedBarcode(value = value, format = format, scannedAtMillis = now)
        _items.value = _items.value + barcode
        return ScanOutcome.Accepted(barcode)
    }

    fun remove(barcode: ScannedBarcode) {
        _items.value = _items.value.filterNot { it.value == barcode.value && it.scannedAtMillis == barcode.scannedAtMillis }
        lastSeenAt.remove(barcode.value)
    }

    fun clear() {
        _items.value = emptyList()
        lastSeenAt.clear()
    }

    /** The payload handed back to the host when the user finishes. */
    fun result(): List<ScannedBarcode> = _items.value
}
