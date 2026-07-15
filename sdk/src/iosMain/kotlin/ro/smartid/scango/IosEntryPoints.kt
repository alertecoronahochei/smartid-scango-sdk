package ro.smartid.scango

import ro.smartid.scango.model.SdkConfig
import ro.smartid.scango.platform.IosContext

/**
 * Swift-friendly wrapper: `IosEntryPointsKt.configureSmartScanSdk(config: ...)`.
 * Saves Swift callers from having to pass the empty iOS context by hand.
 */
fun configureSmartScanSdk(config: SdkConfig = SdkConfig()) {
    SmartScanSdk.configure(IosContext, config)
}
