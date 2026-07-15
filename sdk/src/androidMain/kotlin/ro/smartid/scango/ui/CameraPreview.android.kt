package ro.smartid.scango.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.launch
import ro.smartid.scango.model.BarcodeFormat
import ro.smartid.scango.scanner.CameraXBarcodeScanner
import ro.smartid.scango.scanner.ScanError

@Composable
actual fun CameraPreview(
    modifier: Modifier,
    enabledFormats: Set<BarcodeFormat>,
    torchEnabled: Boolean,
    onDetected: (value: String, format: BarcodeFormat) -> Unit,
    onError: (ScanError) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val scanner = remember {
        CameraXBarcodeScanner(context, lifecycleOwner, previewView, enabledFormats)
    }

    // The callbacks may change between recompositions; the collector must not restart because of it.
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

    AndroidView(factory = { previewView }, modifier = modifier)
}

@Composable
actual fun rememberCameraPermissionState(): CameraPermissionState {
    val context = LocalContext.current

    var granted by remember {
        mutableStateOf(
            context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var denied by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
        granted = result
        denied = !result
    }

    return remember {
        object : CameraPermissionState {
            override val hasPermission: Boolean get() = granted
            override val wasDenied: Boolean get() = denied
            override fun request() {
                launcher.launch(Manifest.permission.CAMERA)
            }
        }
    }
}
