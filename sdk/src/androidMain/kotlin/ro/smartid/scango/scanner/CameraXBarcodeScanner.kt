package ro.smartid.scango.scanner

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import ro.smartid.scango.model.BarcodeFormat
import ro.smartid.scango.model.ScannedBarcode
import ro.smartid.scango.platform.nowMillis
import java.util.concurrent.Executors
import com.google.mlkit.vision.barcode.BarcodeScanner as MlKitScanner

/**
 * Android implementation: CameraX for the frames, ML Kit (bundled model, no Play Services
 * dependency) for the decoding.
 *
 * The host owns the [PreviewView]; the SDK never inflates a layout of its own here — that keeps
 * this class usable from Compose, from XML views, and from a React Native native module alike.
 */
class CameraXBarcodeScanner(
    context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    initialFormats: Set<BarcodeFormat> = BarcodeFormat.DEFAULTS,
) : BarcodeScannerController {

    private val appContext = context.applicationContext
    private val analysisExecutor = Executors.newSingleThreadExecutor()

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

    @Volatile
    private var mlKitScanner: MlKitScanner = buildScanner(initialFormats)

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var torchEnabled = false
    private var released = false

    override val isTorchAvailable: Boolean
        get() = camera?.cameraInfo?.hasFlashUnit() == true

    override fun start() {
        if (released) return
        val future = ProcessCameraProvider.getInstance(appContext)
        future.addListener({
            val provider = runCatching { future.get() }.getOrElse {
                _errors.tryEmit(ScanError.CameraUnavailable)
                return@addListener
            }
            cameraProvider = provider
            bind(provider)
        }, ContextCompat.getMainExecutor(appContext))
    }

    private fun bind(provider: ProcessCameraProvider) {
        val preview = Preview.Builder().build().also {
            it.surfaceProvider = previewView.surfaceProvider
        }

        val analysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { it.setAnalyzer(analysisExecutor, ::analyze) }

        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                analysis,
            )
            camera?.cameraControl?.enableTorch(torchEnabled)
        } catch (e: IllegalArgumentException) {
            _errors.tryEmit(ScanError.CameraUnavailable)
        } catch (e: IllegalStateException) {
            _errors.tryEmit(ScanError.Internal(e.message ?: "Failed to bind camera"))
        }
    }

    @OptIn(ExperimentalGetImage::class)
    private fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        mlKitScanner.process(input)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val value = barcode.rawValue ?: continue
                    val format = mlKitFormatToBarcodeFormat(barcode.format) ?: continue
                    _detections.tryEmit(
                        ScannedBarcode(value = value, format = format, scannedAtMillis = nowMillis()),
                    )
                }
            }
            .addOnFailureListener { e ->
                _errors.tryEmit(ScanError.Internal(e.message ?: "Barcode decoding failed"))
            }
            .addOnCompleteListener { imageProxy.close() }
    }

    override fun setEnabledFormats(formats: Set<BarcodeFormat>) {
        // ML Kit options are immutable, so a format change means a new client.
        val previous = mlKitScanner
        mlKitScanner = buildScanner(formats)
        runCatching { previous.close() }
    }

    override fun setTorchEnabled(enabled: Boolean) {
        torchEnabled = enabled
        camera?.cameraControl?.enableTorch(enabled)
    }

    override fun stop() {
        cameraProvider?.unbindAll()
        camera = null
    }

    override fun release() {
        if (released) return
        released = true
        stop()
        runCatching { mlKitScanner.close() }
        analysisExecutor.shutdown()
    }

    private fun buildScanner(formats: Set<BarcodeFormat>): MlKitScanner {
        val requested = formats.ifEmpty { BarcodeFormat.DEFAULTS }.map { it.toMlKitFormat() }
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(requested.first(), *requested.drop(1).toIntArray())
            .build()
        return BarcodeScanning.getClient(options)
    }
}
