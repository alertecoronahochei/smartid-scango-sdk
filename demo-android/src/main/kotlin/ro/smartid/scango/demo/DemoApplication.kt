package ro.smartid.scango.demo

import android.app.Application
import ro.smartid.scango.SmartScanSdk
import ro.smartid.scango.model.BarcodeFormat
import ro.smartid.scango.model.ScanSettings
import ro.smartid.scango.model.SdkConfig
import ro.smartid.scango.model.SdkEnvironment

/**
 * Everything a host app has to do to wire the SDK up: one call, at startup.
 */
class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        SmartScanSdk.configure(
            context = this,
            config = SdkConfig(
                apiKey = "demo-public-key",
                environment = SdkEnvironment.STAGING,
                defaultSettings = ScanSettings(
                    enabledFormats = setOf(
                        BarcodeFormat.EAN_13,
                        BarcodeFormat.EAN_8,
                        BarcodeFormat.CODE_128,
                        BarcodeFormat.QR_CODE,
                    ),
                    continuousScan = true,
                    beepOnScan = true,
                ),
            ),
        )
    }
}
