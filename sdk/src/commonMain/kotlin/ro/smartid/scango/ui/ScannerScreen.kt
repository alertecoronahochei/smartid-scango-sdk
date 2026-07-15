package ro.smartid.scango.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import ro.smartid.scango.model.ScanSettings
import ro.smartid.scango.platform.ScanFeedback
import ro.smartid.scango.session.ScanOutcome
import ro.smartid.scango.session.ScanSession

/**
 * Full-bleed camera with a thin overlay. All scan policy (duplicates, cooldown, limits)
 * is delegated to [ScanSession] — this screen only reacts to the outcome.
 */
@Composable
internal fun ScannerScreen(
    settings: ScanSettings,
    strings: ScanGoStrings,
    session: ScanSession,
    feedback: ScanFeedback,
    onClose: () -> Unit,
) {
    val permission = rememberCameraPermissionState()

    if (!permission.hasPermission) {
        PermissionRequest(
            strings = strings,
            wasDenied = permission.wasDenied,
            onRequest = permission::request,
            onClose = onClose,
        )
        return
    }

    var torchEnabled by remember { mutableStateOf(settings.torchOnStart) }
    var banner by remember { mutableStateOf<String?>(null) }
    var scannedCount by remember { mutableStateOf(session.itemCount) }

    LaunchedEffect(banner) {
        if (banner != null) {
            delay(BANNER_DURATION_MILLIS)
            banner = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            enabledFormats = settings.enabledFormats,
            torchEnabled = torchEnabled,
            onDetected = { value, format ->
                when (val outcome = session.submit(value, format)) {
                    is ScanOutcome.Accepted -> {
                        feedback.play(settings.beepOnScan, settings.vibrateOnScan)
                        banner = outcome.barcode.value
                        scannedCount = session.itemCount
                        if (!settings.continuousScan) onClose()
                    }

                    is ScanOutcome.DuplicateMerged -> {
                        feedback.play(settings.beepOnScan, settings.vibrateOnScan)
                        banner = strings.duplicateIgnored
                    }

                    ScanOutcome.IgnoredLimitReached -> banner = strings.limitReached
                    ScanOutcome.IgnoredFormatDisabled -> banner = strings.formatDisabled
                    ScanOutcome.IgnoredDuplicate,
                    ScanOutcome.IgnoredCooldown,
                    -> Unit
                }
            },
            onError = { error -> banner = "${strings.cameraError}: ${error.message}" },
        )

        ScanReticle(modifier = Modifier.fillMaxSize())

        // Top bar: close + torch
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OverlayIconButton(
                onClick = onClose,
                icon = { Icon(Icons.Filled.Close, contentDescription = strings.close) },
            )
            OverlayIconButton(
                onClick = { torchEnabled = !torchEnabled },
                icon = {
                    Icon(
                        imageVector = if (torchEnabled) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        contentDescription = strings.torch,
                    )
                },
            )
        }

        // Bottom: hint / last result / running count
        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = banner ?: strings.scannerHint,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            )
            if (settings.continuousScan) {
                Button(onClick = onClose, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                    Text(strings.scannedItems(scannedCount))
                }
            }
        }
    }
}

@Composable
private fun ScanReticle(modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.28f)
                .border(2.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(16.dp)),
        )
    }
}

@Composable
private fun OverlayIconButton(onClick: () -> Unit, icon: @Composable () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = Color.Black.copy(alpha = 0.45f),
            contentColor = Color.White,
        ),
        content = icon,
    )
}

@Composable
private fun PermissionRequest(
    strings: ScanGoStrings,
    wasDenied: Boolean,
    onRequest: () -> Unit,
    onClose: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.PhotoCamera,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = strings.permissionTitle,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = if (wasDenied) strings.permissionDeniedHint else strings.permissionMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Button(
                onClick = onRequest,
                enabled = !wasDenied,
                modifier = Modifier.padding(top = 24.dp).fillMaxWidth().height(48.dp),
            ) {
                Text(strings.permissionGrant)
            }
            Button(
                onClick = onClose,
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(48.dp),
            ) {
                Text(strings.back)
            }
        }
    }
}

private const val BANNER_DURATION_MILLIS = 1_600L
