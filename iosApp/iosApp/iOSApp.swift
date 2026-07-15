import SwiftUI
import SmartScanSDK   // the XCFramework produced by ./gradlew assembleSmartScanSDKXCFramework

@main
struct iOSApp: App {

    init() {
        // The iOS equivalent of DemoApplication.onCreate() — one call, at startup.
        IosEntryPointsKt.configureSmartScanSdk(
            config: SdkConfig(
                apiKey: "demo-public-key",
                environment: .staging,
                defaultSettings: ScanSettings(
                    enabledFormats: [.ean13, .ean8, .code128, .qrCode],
                    continuousScan: true,
                    allowDuplicates: false,
                    beepOnScan: true,
                    vibrateOnScan: true,
                    torchOnStart: false,
                    rescanCooldownMillis: 1500,
                    maxItems: 0
                ),
                settingsScreenEnabled: true
            )
        )
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
