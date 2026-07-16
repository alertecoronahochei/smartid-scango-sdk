package ro.smartid.scango

import platform.UIKit.UIViewController
import ro.smartid.scango.model.ScannedBarcode
import ro.smartid.scango.model.SdkConfig
import ro.smartid.scango.platform.IosContext
import ro.smartid.scango.ui.ScanGoStrings
import ro.smartid.scango.ui.ScanGoViewController

/**
 * Swift-friendly wrapper: `IosEntryPointsKt.configureSmartScanSdk(config: ...)`.
 * Saves Swift callers from having to pass the empty iOS context by hand.
 */
fun configureSmartScanSdk(config: SdkConfig = SdkConfig()) {
    SmartScanSdk.configure(IosContext, config)
}

/**
 * Swift-friendly entry point for the scan&go screen with the SDK's default (Romanian) strings.
 *
 * Kotlin/Native does not expose default-argument constructors as a zero-arg `init()` in Swift,
 * so Swift callers cannot write `ScanGoStrings()`. This wrapper builds it on the Kotlin side, so
 * the common case needs no `ScanGoStrings` at all:
 *
 * ```swift
 * let vc = IosEntryPointsKt.makeScanGoViewController { barcodes in ... }
 * ```
 *
 * To ship other languages, pass your own instance to the overload below.
 */
fun makeScanGoViewController(
    onFinish: (List<ScannedBarcode>) -> Unit,
): UIViewController = ScanGoViewController(strings = ScanGoStrings(), onFinish = onFinish)

/** Same, but with caller-provided strings for localisation. */
fun makeScanGoViewController(
    strings: ScanGoStrings,
    onFinish: (List<ScannedBarcode>) -> Unit,
): UIViewController = ScanGoViewController(strings = strings, onFinish = onFinish)
