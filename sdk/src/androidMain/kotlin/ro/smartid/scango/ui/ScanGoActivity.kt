package ro.smartid.scango.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import ro.smartid.scango.model.ScannedBarcode

/**
 * Drop-in entry point for hosts that are not written in Compose (XML views, React Native,
 * Flutter, whatever): launch it with [ScanGoContract] and get the list back.
 *
 * Compose-based hosts should call [ScanGoScreen] directly instead — no Activity hop.
 */
class ScanGoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScanGoScreen(onFinish = ::finishWithResult)
        }
    }

    private fun finishWithResult(barcodes: List<ScannedBarcode>) {
        val data = Intent().putExtra(EXTRA_RESULT, encodeBarcodes(barcodes))
        setResult(Activity.RESULT_OK, data)
        finish()
    }

    internal companion object {
        const val EXTRA_RESULT = "ro.smartid.scango.EXTRA_RESULT"
    }
}

/**
 * ```
 * private val scanLauncher = registerForActivityResult(ScanGoContract()) { barcodes ->
 *     // barcodes: List<ScannedBarcode>
 * }
 * scanLauncher.launch(Unit)
 * ```
 */
class ScanGoContract : ActivityResultContract<Unit, List<ScannedBarcode>>() {

    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(context, ScanGoActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): List<ScannedBarcode> {
        if (resultCode != Activity.RESULT_OK) return emptyList()
        val raw = intent?.getStringExtra(ScanGoActivity.EXTRA_RESULT) ?: return emptyList()
        return decodeBarcodes(raw)
    }
}

private val json = Json { ignoreUnknownKeys = true }
private val barcodeListSerializer = ListSerializer(ScannedBarcode.serializer())

internal fun encodeBarcodes(barcodes: List<ScannedBarcode>): String =
    json.encodeToString(barcodeListSerializer, barcodes)

internal fun decodeBarcodes(raw: String): List<ScannedBarcode> =
    runCatching { json.decodeFromString(barcodeListSerializer, raw) }.getOrDefault(emptyList())
