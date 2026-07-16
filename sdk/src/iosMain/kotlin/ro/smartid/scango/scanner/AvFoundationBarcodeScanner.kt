package ro.smartid.scango.scanner

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureTorchModeOff
import platform.AVFoundation.AVCaptureTorchModeOn
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.hasTorch
import platform.AVFoundation.requestAccessForMediaType
import platform.AVFoundation.setTorchMode
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_queue_create
import ro.smartid.scango.model.BarcodeFormat
import ro.smartid.scango.model.ScannedBarcode
import ro.smartid.scango.platform.nowMillis

/**
 * iOS implementation: AVFoundation's built-in machine-readable-code detector.
 *
 * No third-party dependency and no ML model shipped in the binary — the OS does the decoding.
 * The host presents [previewLayer]; `sdk-ui` wraps it in a `UIKitView`, but a plain UIKit
 * app can add it as a sublayer just as well.
 *
 * Requires `NSCameraUsageDescription` in the host's Info.plist.
 */
@OptIn(ExperimentalForeignApi::class)
class AvFoundationBarcodeScanner(
    initialFormats: Set<BarcodeFormat> = BarcodeFormat.DEFAULTS,
) : BarcodeScannerController {

    private val session = AVCaptureSession().apply {
        if (canSetSessionPreset(AVCaptureSessionPresetHigh)) sessionPreset = AVCaptureSessionPresetHigh
    }

    private val metadataOutput = AVCaptureMetadataOutput()
    private val sessionQueue = dispatch_queue_create("ro.smartid.scango.session", null)

    private var device: AVCaptureDevice? = null
    private var enabledFormats: Set<BarcodeFormat> = initialFormats
    private var configured = false
    private var released = false
    private var torchEnabled = false

    /** Add this layer to the view hierarchy to show the camera feed. */
    val previewLayer: AVCaptureVideoPreviewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
        videoGravity = AVLayerVideoGravityResizeAspectFill
    }

    private val _detections = MutableSharedFlow<ScannedBarcode>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val detections: Flow<ScannedBarcode> = _detections.asSharedFlow()

    private val _errors = MutableSharedFlow<ScanError>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val errors: Flow<ScanError> = _errors.asSharedFlow()

    override val isTorchAvailable: Boolean
        get() = device?.hasTorch() == true

    private val delegate = MetadataObjectsDelegate { value, format ->
        _detections.tryEmit(ScannedBarcode(value = value, format = format, scannedAtMillis = nowMillis()))
    }

    override fun start() {
        if (released) return
        when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> configureAndRun()
            AVAuthorizationStatusNotDetermined -> AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                if (granted) {
                    dispatch_async(dispatch_get_main_queue()) { configureAndRun() }
                } else {
                    _errors.tryEmit(ScanError.PermissionDenied)
                }
            }
            else -> _errors.tryEmit(ScanError.PermissionDenied)
        }
    }

    private fun configureAndRun() {
        if (released) return
        if (!configured && !configureSession()) return
        if (!session.isRunning()) {
            dispatch_async(sessionQueue) {
                // startRunning can raise an Objective-C exception (surfaced to Kotlin as a
                // foreign exception). Uncaught on this background queue it would abort the whole
                // host app, so we catch it and report it instead.
                try {
                    session.startRunning()
                } catch (t: Throwable) {
                    _errors.tryEmit(ScanError.Internal(t.message ?: "camera failed to start"))
                }
            }
        }
        setTorchEnabled(torchEnabled)
    }

    private fun configureSession(): Boolean {
        return try {
            val captureDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
            if (captureDevice == null) {
                _errors.tryEmit(ScanError.CameraUnavailable)
                return false
            }
            device = captureDevice

            val input = AVCaptureDeviceInput.deviceInputWithDevice(captureDevice, null)
            if (input == null) {
                _errors.tryEmit(ScanError.CameraUnavailable)
                return false
            }

            session.beginConfiguration()
            if (session.canAddInput(input)) session.addInput(input)
            if (session.canAddOutput(metadataOutput)) {
                session.addOutput(metadataOutput)
                // The delegate must be set before the types, and the types only after addOutput —
                // AVFoundation throws if you ask for a type the output does not yet support.
                metadataOutput.setMetadataObjectsDelegate(delegate, queue = dispatch_get_main_queue())
            } else {
                session.commitConfiguration()
                _errors.tryEmit(ScanError.CameraUnavailable)
                return false
            }
            session.commitConfiguration()

            applyFormats(enabledFormats)
            configured = true
            true
        } catch (t: Throwable) {
            // Never let an AVFoundation exception abort the host app — surface it as an error.
            _errors.tryEmit(ScanError.Internal(t.message ?: "camera configuration failed"))
            false
        }
    }

    private fun applyFormats(formats: Set<BarcodeFormat>) {
        if (!configured && session.outputs.isEmpty()) return
        try {
            val available = metadataOutput.availableMetadataObjectTypes.filterIsInstance<String>().toSet()
            val requested = formats.ifEmpty { BarcodeFormat.DEFAULTS }
                .map { it.toAvMetadataType() }
                .filter { it in available }
                .distinct()
            if (requested.isNotEmpty()) {
                metadataOutput.metadataObjectTypes = requested
            }
        } catch (t: Throwable) {
            _errors.tryEmit(ScanError.Internal(t.message ?: "unsupported barcode formats"))
        }
    }

    override fun setEnabledFormats(formats: Set<BarcodeFormat>) {
        enabledFormats = formats
        if (configured) applyFormats(formats)
    }

    override fun setTorchEnabled(enabled: Boolean) {
        torchEnabled = enabled
        val captureDevice = device ?: return
        if (!captureDevice.hasTorch()) return
        try {
            if (captureDevice.lockForConfiguration(null)) {
                captureDevice.setTorchMode(if (enabled) AVCaptureTorchModeOn else AVCaptureTorchModeOff)
                captureDevice.unlockForConfiguration()
            }
        } catch (t: Throwable) {
            _errors.tryEmit(ScanError.Internal(t.message ?: "torch toggle failed"))
        }
    }

    override fun stop() {
        if (session.isRunning()) {
            dispatch_async(sessionQueue) { session.stopRunning() }
        }
    }

    override fun release() {
        if (released) return
        released = true
        setTorchEnabled(false)
        stop()
        metadataOutput.setMetadataObjectsDelegate(null, queue = null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private class MetadataObjectsDelegate(
    private val onDetected: (value: String, format: BarcodeFormat) -> Unit,
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection,
    ) {
        for (item in didOutputMetadataObjects) {
            val code = item as? AVMetadataMachineReadableCodeObject ?: continue
            val value = code.stringValue ?: continue
            val format = avMetadataTypeToBarcodeFormat(code.type) ?: continue
            onDetected(value, format)
        }
    }
}
