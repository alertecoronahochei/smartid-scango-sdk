package ro.smartid.scango.demo

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ro.smartid.scango.model.ScannedBarcode
import ro.smartid.scango.ui.ScanGoContract

/**
 * A deliberately boring, XML-based host — no Compose anywhere in this module.
 * It proves the SDK's screens drop into any Android app, not just Compose ones.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var resultsView: TextView

    private val scanLauncher = registerForActivityResult(ScanGoContract()) { barcodes ->
        render(barcodes)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultsView = findViewById(R.id.results)
        findViewById<Button>(R.id.scan_button).setOnClickListener {
            scanLauncher.launch(Unit)
        }

        render(emptyList())
    }

    private fun render(barcodes: List<ScannedBarcode>) {
        resultsView.text = if (barcodes.isEmpty()) {
            getString(R.string.no_results)
        } else {
            barcodes.joinToString(separator = "\n") { barcode ->
                val suffix = if (barcode.count > 1) "  (×${barcode.count})" else ""
                "• ${barcode.value}  [${barcode.format.displayName}]$suffix"
            }
        }
    }
}
