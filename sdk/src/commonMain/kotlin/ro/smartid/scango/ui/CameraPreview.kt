package ro.smartid.scango.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import ro.smartid.scango.model.BarcodeFormat
import ro.smartid.scango.scanner.ScanError

/**
 * The live camera feed with barcode decoding attached.
 *
 * This is the one composable that cannot be shared: Android renders a CameraX `PreviewView`,
 * iOS an `AVCaptureVideoPreviewLayer`. Everything above it — list, settings, dedupe, feedback —
 * is common code.
 */
@Composable
expect fun CameraPreview(
    modifier: Modifier,
    enabledFormats: Set<BarcodeFormat>,
    torchEnabled: Boolean,
    onDetected: (value: String, format: BarcodeFormat) -> Unit,
    onError: (ScanError) -> Unit,
)

/** Camera permission, as seen by the shared UI. */
@Stable
interface CameraPermissionState {
    val hasPermission: Boolean
    val wasDenied: Boolean
    fun request()
}

@Composable
expect fun rememberCameraPermissionState(): CameraPermissionState
