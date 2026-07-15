package ro.smartid.scango.model

/** Deployment environment the SDK talks to. Kept here so hosts can flip staging/prod without a rebuild. */
enum class SdkEnvironment { PRODUCTION, STAGING }

/**
 * Handed to [ro.smartid.scango.SmartScanSdk.configure] once, at app start.
 *
 * NOTE: never ship a real secret inside a mobile binary — [apiKey] is a *public*
 * client identifier, not a credential. Anything genuinely sensitive stays server-side.
 */
data class SdkConfig(
    val apiKey: String = "",
    val environment: SdkEnvironment = SdkEnvironment.PRODUCTION,
    /** Settings applied on first launch, before the user touches the settings screen. */
    val defaultSettings: ScanSettings = ScanSettings.DEFAULT,
    /** Let the user reach the settings screen from the scanner. Hosts that want a locked-down flow can disable it. */
    val settingsScreenEnabled: Boolean = true,
)
