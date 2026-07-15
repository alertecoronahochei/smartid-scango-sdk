package ro.smartid.scango.scanner

import com.google.mlkit.vision.barcode.common.Barcode
import ro.smartid.scango.model.BarcodeFormat

internal fun BarcodeFormat.toMlKitFormat(): Int = when (this) {
    BarcodeFormat.QR_CODE -> Barcode.FORMAT_QR_CODE
    BarcodeFormat.DATA_MATRIX -> Barcode.FORMAT_DATA_MATRIX
    BarcodeFormat.PDF_417 -> Barcode.FORMAT_PDF417
    BarcodeFormat.AZTEC -> Barcode.FORMAT_AZTEC
    BarcodeFormat.EAN_13 -> Barcode.FORMAT_EAN_13
    BarcodeFormat.EAN_8 -> Barcode.FORMAT_EAN_8
    BarcodeFormat.UPC_A -> Barcode.FORMAT_UPC_A
    BarcodeFormat.UPC_E -> Barcode.FORMAT_UPC_E
    BarcodeFormat.CODE_128 -> Barcode.FORMAT_CODE_128
    BarcodeFormat.CODE_39 -> Barcode.FORMAT_CODE_39
    BarcodeFormat.CODE_93 -> Barcode.FORMAT_CODE_93
    BarcodeFormat.ITF -> Barcode.FORMAT_ITF
    BarcodeFormat.CODABAR -> Barcode.FORMAT_CODABAR
}

internal fun mlKitFormatToBarcodeFormat(format: Int): BarcodeFormat? = when (format) {
    Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
    Barcode.FORMAT_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
    Barcode.FORMAT_PDF417 -> BarcodeFormat.PDF_417
    Barcode.FORMAT_AZTEC -> BarcodeFormat.AZTEC
    Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
    Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
    Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
    Barcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
    Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
    Barcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
    Barcode.FORMAT_CODE_93 -> BarcodeFormat.CODE_93
    Barcode.FORMAT_ITF -> BarcodeFormat.ITF
    Barcode.FORMAT_CODABAR -> BarcodeFormat.CODABAR
    else -> null
}
