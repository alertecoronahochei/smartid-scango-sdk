package ro.smartid.scango.ui

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import ro.smartid.scango.model.ScannedBarcode

/**
 * The iOS entry point. From Swift:
 *
 * ```swift
 * let vc = ScanGoViewControllerKt.ScanGoViewController { barcodes in
 *     self.dismiss(animated: true)
 *     print(barcodes.map { $0.value })
 * }
 * present(vc, animated: true)
 * ```
 *
 * Requires `NSCameraUsageDescription` in the host's Info.plist.
 */
fun ScanGoViewController(
    strings: ScanGoStrings = ScanGoStrings(),
    onFinish: (List<ScannedBarcode>) -> Unit,
): UIViewController = ComposeUIViewController {
    ScanGoScreen(
        onFinish = onFinish,
        strings = strings,
    )
}
