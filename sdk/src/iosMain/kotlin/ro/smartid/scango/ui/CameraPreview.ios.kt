package ro.smartid.scango.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.coroutines.launch
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRectZero
import platform.QuartzCore.CATransaction
import platform.UIKit.UIView
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import ro.smartid.scango.model.BarcodeFormat
import ro.smartid.scango.scanner.AvFoundationBarcodeScanner
import ro.smartid.scango.scanner.ScanError

@OptIn(ExperimentalForeignApi::class, ExperimentalComposeUiApi::class)
@Composable
actual fun CameraPreview(
    modifier: Modifier,
    enabledFormats: Set<BarcodeFormat>,
    torchEnabled: Boolean,
    onDetected: (value: String, format: BarcodeFormat) -> Unit,
    onError: (ScanError) -> Unit,
) {
    val scanner = remember { AvFoundationBarcodeScanner(enabledFormats) }
    val containerView = remember { CameraPreviewContainer(scanner.previewLayer) }

    val currentOnDetected by rememberUpdatedState(onDetected)
    val currentOnError by rememberUpdatedState(onError)

    LaunchedEffect(scanner) {
        launch { scanner.detections.collect { currentOnDetected(it.value, it.format) } }
        launch { scanner.errors.collect { currentOnError(it) } }
    }

    LaunchedEffect(enabledFormats) { scanner.setEnabledFormats(enabledFormats) }
    LaunchedEffect(torchEnabled) { scanner.setTorchEnabled(torchEnabled) }

    DisposableEffect(scanner) {
        scanner.start()
        onDispose { scanner.release() }
    }

    UIKitView(
        factory = { containerView },
        modifier = modifier,
        update = { view -> view.setNeedsLayout() },
    )
}

/**
 * A UIView whose only job is to keep the capture preview layer the size of the view —
 * CALayer does not autoresize with its host view.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class CameraPreviewContainer(
    private val previewLayer: AVCaptureVideoPreviewLayer,
) : UIView(frame = CGRectZero.readValue()) {

    init {
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        // Without this, the layer animates into its new frame on every rotation.
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        previewLayer.setFrame(bounds)
        CATransaction.commit()
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState {
    var granted by remember {
        mutableStateOf(
            AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusAuthorized,
        )
    }
    var denied by remember {
        mutableStateOf(
            AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusDenied,
        )
    }

    return remember {
        object : CameraPermissionState {
            override val hasPermission: Boolean get() = granted
            override val wasDenied: Boolean get() = denied

            override fun request() {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { isGranted ->
                    // The AVFoundation callback lands on an arbitrary queue; Compose state is main-thread only.
                    dispatch_async(dispatch_get_main_queue()) {
                        granted = isGranted
                        denied = !isGranted
                    }
                }
            }
        }
    }
}
