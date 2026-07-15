package ro.smartid.scango.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ro.smartid.scango.SmartScanSdk
import ro.smartid.scango.model.ScannedBarcode
import ro.smartid.scango.platform.ScanFeedback
import ro.smartid.scango.session.ScanSession

private enum class Route { HOME, SCANNER, SETTINGS }

/**
 * The whole scan&go flow in a single composable: results list -> camera -> settings.
 *
 * The host only has to say what happens when the user is done.
 *
 * @param onFinish called with the collected barcodes when the user taps "finish"
 *   (or with an empty list if they back out).
 * @param strings override to ship the SDK in another language.
 * @param useSdkTheme false = inherit the host's MaterialTheme instead of the SDK's own.
 */
@Composable
fun ScanGoScreen(
    onFinish: (List<ScannedBarcode>) -> Unit,
    modifier: Modifier = Modifier,
    strings: ScanGoStrings = ScanGoStrings(),
    useSdkTheme: Boolean = true,
    darkTheme: Boolean = isSystemInDarkTheme(),
) {
    check(SmartScanSdk.isConfigured) {
        "SmartScanSdk.configure(context, config) must be called before showing ScanGoScreen()"
    }

    val settingsRepository = SmartScanSdk.settings
    val settings by settingsRepository.settings.collectAsState()

    val session = remember { ScanSession(settings) }
    LaunchedEffect(settings) { session.applySettings(settings) }

    val items by session.items.collectAsState()

    val feedback = remember { ScanFeedback(SmartScanSdk.platformContext) }
    DisposableEffect(Unit) { onDispose { feedback.release() } }

    var route by remember { mutableStateOf(Route.HOME) }

    val content: @Composable () -> Unit = {
        when (route) {
            Route.HOME -> HomeScreen(
                items = items,
                strings = strings,
                settingsEnabled = SmartScanSdk.config.settingsScreenEnabled,
                onStartScan = { route = Route.SCANNER },
                onOpenSettings = { route = Route.SETTINGS },
                onClear = session::clear,
                onRemove = session::remove,
                onFinish = { onFinish(session.result()) },
            )

            Route.SCANNER -> ScannerScreen(
                settings = settings,
                strings = strings,
                session = session,
                feedback = feedback,
                onClose = { route = Route.HOME },
            )

            Route.SETTINGS -> SettingsScreen(
                settings = settings,
                strings = strings,
                onSettingsChanged = settingsRepository::save,
                onResetDefaults = settingsRepository::resetToDefaults,
                onBack = { route = Route.HOME },
            )
        }
    }

    if (useSdkTheme) {
        ScanGoTheme(darkTheme) {
            Surface(modifier = modifier) { content() }
        }
    } else {
        Surface(modifier = modifier) { content() }
    }
}
