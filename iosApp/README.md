# iOS host — cum se leagă

Fișierele Swift de aici sunt *codul de integrare*, nu un proiect Xcode complet (nu poate fi
generat corect de pe Windows). Pe Mac, pașii sunt:

## 1. Construiește framework-ul

```bash
./gradlew :sdk-ui:assembleSmartScanSDKXCFramework
# rezultat: sdk-ui/build/XCFrameworks/release/SmartScanSDK.xcframework
```

XCFramework-ul conține **și** API-ul din `sdk-core` (e re-exportat cu `export(project(":sdk-core"))`),
deci Swift vede `ScannedBarcode`, `ScanSettings`, `SmartScanSdk` etc. dintr-un singur import.

## 2. Creează proiectul Xcode

1. Xcode → New Project → App (SwiftUI), bundle id `ro.smartid.scango.demo`.
2. Deployment target: **iOS 15.4+** (tipul Codabar din AVFoundation e disponibil de la 15.4).
3. Copiază `iOSApp.swift`, `ContentView.swift`, `Info.plist` din `iosApp/iosApp/` în proiect.
4. General → Frameworks, Libraries, and Embedded Content → **+** → adaugă
   `SmartScanSDK.xcframework` → *Embed & Sign*.

## 3. Info.plist

`NSCameraUsageDescription` este **obligatoriu** — fără el iOS termină procesul la pornirea camerei.

## Alternativ: build automat la fiecare compilare Xcode

În Build Phases → New Run Script Phase:

```bash
cd "$SRCROOT/.."
./gradlew :sdk-ui:embedAndSignAppleFrameworkForXcode
```

Așa nu mai trebuie să reconstruiești manual framework-ul după fiecare modificare în Kotlin.

## Ce nu e verificat încă

Codul Kotlin din `iosMain` (AVFoundation + `UIKitView`) **nu a fost compilat** — Kotlin/Native
pentru iOS se poate construi doar pe macOS. Prima compilare pe Mac poate cere ajustări minore
de import/semnătură. Codul Android e compilat, testat și rulat.
