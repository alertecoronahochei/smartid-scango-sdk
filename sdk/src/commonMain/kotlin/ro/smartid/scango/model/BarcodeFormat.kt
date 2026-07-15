package ro.smartid.scango.model

import kotlinx.serialization.Serializable

/**
 * Barcode symbologies the SDK can recognise.
 *
 * The set is intentionally platform-neutral: each platform maps these to its own
 * scanning engine (ML Kit on Android, AVFoundation on iOS).
 */
@Serializable
enum class BarcodeFormat(val displayName: String, val isTwoDimensional: Boolean) {
    QR_CODE("QR Code", true),
    DATA_MATRIX("Data Matrix", true),
    PDF_417("PDF417", true),
    AZTEC("Aztec", true),
    EAN_13("EAN-13", false),
    EAN_8("EAN-8", false),
    UPC_A("UPC-A", false),
    UPC_E("UPC-E", false),
    CODE_128("Code 128", false),
    CODE_39("Code 39", false),
    CODE_93("Code 93", false),
    ITF("ITF", false),
    CODABAR("Codabar", false);

    companion object {
        /** Formats enabled on a fresh install: retail 1D codes + QR. */
        val DEFAULTS: Set<BarcodeFormat> = setOf(QR_CODE, EAN_13, EAN_8, CODE_128)

        val ALL: Set<BarcodeFormat> = entries.toSet()

        fun fromNameOrNull(name: String): BarcodeFormat? = entries.firstOrNull { it.name == name }
    }
}
