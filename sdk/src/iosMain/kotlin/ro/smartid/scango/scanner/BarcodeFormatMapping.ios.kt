package ro.smartid.scango.scanner

import platform.AVFoundation.AVMetadataObjectTypeAztecCode
import platform.AVFoundation.AVMetadataObjectTypeCodabarCode
import platform.AVFoundation.AVMetadataObjectTypeCode128Code
import platform.AVFoundation.AVMetadataObjectTypeCode39Code
import platform.AVFoundation.AVMetadataObjectTypeCode93Code
import platform.AVFoundation.AVMetadataObjectTypeDataMatrixCode
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeEAN8Code
import platform.AVFoundation.AVMetadataObjectTypeITF14Code
import platform.AVFoundation.AVMetadataObjectTypePDF417Code
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.AVMetadataObjectTypeUPCECode
import ro.smartid.scango.model.BarcodeFormat

/**
 * AVFoundation has no dedicated UPC-A type: it reports UPC-A as EAN-13 with a leading zero.
 * Asking for UPC_A therefore enables the EAN-13 detector, and such reads surface as [BarcodeFormat.EAN_13].
 */
internal fun BarcodeFormat.toAvMetadataType(): String = when (this) {
    BarcodeFormat.QR_CODE -> AVMetadataObjectTypeQRCode
    BarcodeFormat.DATA_MATRIX -> AVMetadataObjectTypeDataMatrixCode
    BarcodeFormat.PDF_417 -> AVMetadataObjectTypePDF417Code
    BarcodeFormat.AZTEC -> AVMetadataObjectTypeAztecCode
    BarcodeFormat.EAN_13 -> AVMetadataObjectTypeEAN13Code
    BarcodeFormat.EAN_8 -> AVMetadataObjectTypeEAN8Code
    BarcodeFormat.UPC_A -> AVMetadataObjectTypeEAN13Code
    BarcodeFormat.UPC_E -> AVMetadataObjectTypeUPCECode
    BarcodeFormat.CODE_128 -> AVMetadataObjectTypeCode128Code
    BarcodeFormat.CODE_39 -> AVMetadataObjectTypeCode39Code
    BarcodeFormat.CODE_93 -> AVMetadataObjectTypeCode93Code
    BarcodeFormat.ITF -> AVMetadataObjectTypeITF14Code
    BarcodeFormat.CODABAR -> AVMetadataObjectTypeCodabarCode
}

internal fun avMetadataTypeToBarcodeFormat(type: String): BarcodeFormat? = when (type) {
    AVMetadataObjectTypeQRCode -> BarcodeFormat.QR_CODE
    AVMetadataObjectTypeDataMatrixCode -> BarcodeFormat.DATA_MATRIX
    AVMetadataObjectTypePDF417Code -> BarcodeFormat.PDF_417
    AVMetadataObjectTypeAztecCode -> BarcodeFormat.AZTEC
    AVMetadataObjectTypeEAN13Code -> BarcodeFormat.EAN_13
    AVMetadataObjectTypeEAN8Code -> BarcodeFormat.EAN_8
    AVMetadataObjectTypeUPCECode -> BarcodeFormat.UPC_E
    AVMetadataObjectTypeCode128Code -> BarcodeFormat.CODE_128
    AVMetadataObjectTypeCode39Code -> BarcodeFormat.CODE_39
    AVMetadataObjectTypeCode93Code -> BarcodeFormat.CODE_93
    AVMetadataObjectTypeITF14Code -> BarcodeFormat.ITF
    AVMetadataObjectTypeCodabarCode -> BarcodeFormat.CODABAR
    else -> null
}
