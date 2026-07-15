package ro.smartid.scango

import ro.smartid.scango.model.SdkConfig
import ro.smartid.scango.platform.PlatformContext
import ro.smartid.scango.settings.SettingsRepository
import ro.smartid.scango.settings.createKeyValueStore

/**
 * The one public entry point of the SDK.
 *
 * Host apps call [configure] once (Application.onCreate / AppDelegate) and then either:
 *  - open the ready-made scan&go screens from the `sdk-ui` module, or
 *  - drive [ro.smartid.scango.scanner.BarcodeScannerController] + [ro.smartid.scango.session.ScanSession]
 *    themselves and render their own UI.
 */
object SmartScanSdk {

    private var _config: SdkConfig? = null
    private var _settings: SettingsRepository? = null
    private var _context: PlatformContext? = null

    val isConfigured: Boolean get() = _config != null

    val config: SdkConfig
        get() = _config ?: notConfigured()

    val settings: SettingsRepository
        get() = _settings ?: notConfigured()

    /** The host handle captured in [configure]; the UI module needs it to reach platform services. */
    val platformContext: PlatformContext
        get() = _context ?: notConfigured()

    /** Idempotent: calling it twice with the same config is a no-op, so hosts can be defensive. */
    fun configure(context: PlatformContext, config: SdkConfig = SdkConfig()) {
        if (_config == config && _settings != null) return
        _context = context
        _config = config
        _settings = SettingsRepository(createKeyValueStore(context), config.defaultSettings)
    }

    /** Test/host hook: drop all in-memory state. Does not erase persisted settings. */
    fun reset() {
        _config = null
        _settings = null
        _context = null
    }

    private fun notConfigured(): Nothing =
        throw IllegalStateException("SmartScanSdk.configure(context, config) must be called before using the SDK")
}
