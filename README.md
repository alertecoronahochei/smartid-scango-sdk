# SmartID Scan&Go — Core SDK (Kotlin Multiplatform)

Un SDK cu **logică + ecrane** de scanare coduri de bare, livrat ca artefact nativ pentru
Android (`.aar`) și iOS (`XCFramework`). Aplicațiile terțe îl adaugă ca dependință și primesc
înapoi o listă de coduri scanate — fără să scrie o linie de cod de cameră.

Implementează varianta **5.1 Kotlin Multiplatform** din `core-sdk-android-ios(claude).pdf`.

## Structura

| Modul | Ce e |
|---|---|
| `sdk` | SDK-ul: modele, scanner (CameraX+ML Kit / AVFoundation), sesiune, setări persistate **și** ecranele Compose Multiplatform |
| `demo-android` | Aplicație host XML (fără Compose), consumă SDK-ul |
| `iosApp` | Codul Swift de integrare |

Un singur modul, un singur artefact per platformă: `sdk-release.aar` și `SmartScanSDK.xcframework`.

Pachetele dinăuntru păstrează separarea logică — `model` / `scanner` / `session` / `settings` nu
importă nimic din `ui`. Dacă apare vreodată un consumator care vrea doar logica (de ex. un native
module React Native, secțiunea 6 din document), UI-ul se poate desprinde înapoi într-un modul
`sdk-ui` fără să atingi o linie de cod — doar `build.gradle.kts`.

## Fluxul livrat

```
HomeScreen (lista de coduri)
   ├── [Pornește scanarea] ──> ScannerScreen (cameră + lanternă + reticul)
   │                                └── codul citit intră în listă și revine în Home
   ├── [Setări] ─────────────> SettingsScreen
   └── [Finalizează] ────────> host-ul primește List<ScannedBarcode>
```

## Setările expuse

- **Tipuri de coduri** — 13 simbologii (EAN-13/8, UPC-A/E, Code 128/39/93, ITF, Codabar, QR, DataMatrix, PDF417, Aztec). Mai puține active = scanare mai rapidă.
- **Scanare continuă** — camera rămâne deschisă după fiecare cod, sau se închide la primul.
- **Permite duplicate** — același cod de mai multe ori în listă, sau o singură intrare cu contor `×n`.
- **Lanternă la pornire**.
- **Limită de coduri** — ∞ / 10 / 25 / 50.
- **Sunet** și **vibrație** la scanare.

Setările sunt persistate (SharedPreferences pe Android, NSUserDefaults pe iOS) și supraviețuiesc
repornirii aplicației.

## Integrare Android

```kotlin
// Application.onCreate()
SmartScanSdk.configure(this, SdkConfig(apiKey = "…", environment = SdkEnvironment.PRODUCTION))

// oriunde în app (host non-Compose)
private val scanLauncher = registerForActivityResult(ScanGoContract()) { barcodes ->
    // barcodes: List<ScannedBarcode>
}
scanLauncher.launch(Unit)

// host Compose: fără Activity intermediară
ScanGoScreen(onFinish = { barcodes -> … })
```

## Integrare iOS

```swift
IosEntryPointsKt.configureSmartScanSdk(config: SdkConfig(apiKey: "…", environment: .production))

let vc = ScanGoViewControllerKt.ScanGoViewController(strings: ScanGoStrings()) { barcodes in
    // barcodes: [ScannedBarcode]
}
present(vc, animated: true)
```

Detalii în [iosApp/README.md](iosApp/README.md). `NSCameraUsageDescription` e obligatoriu.

## Build

```bash
./gradlew :demo-android:assembleDebug         # APK demo
./gradlew :sdk:assembleRelease                # sdk/build/outputs/aar/sdk-release.aar
./gradlew :sdk:testDebugUnitTest              # teste logică (7)
./gradlew :sdk:assembleSmartScanSDKXCFramework   # XCFramework — doar pe macOS
```

Necesar: **JDK 17+** (proiectul e verificat pe Temurin 21) și Android SDK. Pentru iOS: macOS + Xcode.

## Stare

| | Status |
|---|---|
| Logică + teste | ✅ compilat, 7/7 teste trec |
| Android scanner + ecrane | ✅ compilat, `sdk-release.aar` (248 KB) |
| Demo Android | ✅ APK generat (nerulat pe device) |
| iOS (`iosMain`, XCFramework) | ⚠️ **scris, necompilat** — Kotlin/Native iOS cere macOS |

## Localizare

Toate textele sunt în `ScanGoStrings` (implicit română). Un host care livrează în altă limbă
pasează propria instanță: `ScanGoScreen(onFinish = …, strings = ScanGoStrings(startScan = "Start scan", …))`.

## Ce urmează (nefăcut intenționat)

- Publicare Maven / SPM + versionare SemVer (secțiunile 9–10 din document).
- Native module React Native peste SDK (secțiunea 6).
- Rulare pe device fizic + tuning acuratețe.
