import SwiftUI
import UIKit
import SmartScanSDK

/// A plain SwiftUI host. It owns no scanning UI of its own — it presents the SDK's
/// view controller and receives the collected barcodes back.
struct ContentView: View {

    @State private var results: [ScannedBarcode] = []
    @State private var isScannerPresented = false

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Aplicație terță (host)")
                .font(.title2).bold()
            Text("Nu conține UI de scanare — tot ce vezi la apăsarea butonului vine din SDK.")
                .font(.subheadline)
                .foregroundStyle(.secondary)

            Button("Deschide Scan & Go") {
                isScannerPresented = true
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)

            if results.isEmpty {
                Text("Niciun rezultat încă.").foregroundStyle(.secondary)
            } else {
                List(results, id: \.value) { barcode in
                    VStack(alignment: .leading) {
                        Text(barcode.value).font(.body.monospaced())
                        Text("\(barcode.format.displayName)\(barcode.count > 1 ? "  ×\(barcode.count)" : "")")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }
                .listStyle(.plain)
            }

            Spacer()
        }
        .padding(24)
        .fullScreenCover(isPresented: $isScannerPresented) {
            ScanGoView { barcodes in
                results = barcodes
                isScannerPresented = false
            }
            .ignoresSafeArea()
        }
    }
}

/// Bridges the Kotlin-provided UIViewController into SwiftUI.
private struct ScanGoView: UIViewControllerRepresentable {

    let onFinish: ([ScannedBarcode]) -> Void

    func makeUIViewController(context: Context) -> UIViewController {
        // Default (Romanian) strings; for another language use the
        // makeScanGoViewController(strings:onFinish:) overload with your own ScanGoStrings.
        IosEntryPointsKt.makeScanGoViewController { barcodes in
            onFinish(barcodes as? [ScannedBarcode] ?? [])
        }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
