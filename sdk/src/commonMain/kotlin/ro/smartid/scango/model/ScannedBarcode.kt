package ro.smartid.scango.model

import kotlinx.serialization.Serializable

/** A single successful detection, as handed back to the host application. */
@Serializable
data class ScannedBarcode(
    val value: String,
    val format: BarcodeFormat,
    val scannedAtMillis: Long,
    /** How many times this exact value was seen in the session (>= 1). */
    val count: Int = 1,
)
