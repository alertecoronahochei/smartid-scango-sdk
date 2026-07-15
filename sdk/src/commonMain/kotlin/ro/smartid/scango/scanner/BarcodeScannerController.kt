package ro.smartid.scango.scanner

import kotlinx.coroutines.flow.Flow
import ro.smartid.scango.model.BarcodeFormat
import ro.smartid.scango.model.ScannedBarcode

/** Why the camera stopped producing results. */
sealed class ScanError(val message: String) {
    /** The host has not been granted (or the user revoked) camera access. */
    data object PermissionDenied : ScanError("Camera permission was denied")

    /** No usable camera on this device / in this simulator. */
    data object CameraUnavailable : ScanError("No camera is available on this device")

    /** Anything the camera stack threw at us. */
    data class Internal(val cause: String) : ScanError(cause)
}

/**
 * Raw camera + decoder. Emits *every* detection, unfiltered — deduplication,
 * cooldowns and the enabled-format policy live in [ro.smartid.scango.session.ScanSession].
 *
 * Hosts that want their own UI can drive this directly; the SDK's own screens
 * ([ro.smartid.scango.ui]) are just one consumer of it.
 */
interface BarcodeScannerController {
    val detections: Flow<ScannedBarcode>
    val errors: Flow<ScanError>

    val isTorchAvailable: Boolean

    fun start()
    fun stop()
    fun setEnabledFormats(formats: Set<BarcodeFormat>)
    fun setTorchEnabled(enabled: Boolean)
    fun release()
}
