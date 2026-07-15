package ro.smartid.scango.ui

import androidx.compose.runtime.Immutable

/**
 * Every user-visible string in the SDK, in one place.
 *
 * Defaults are Romanian; a host that ships in another language passes its own instance to
 * [ScanGoScreen] instead of having to fork the SDK. No resource files, no locale plumbing —
 * the host already knows its language.
 */
@Immutable
data class ScanGoStrings(
    val appTitle: String = "Scan & Go",
    val startScan: String = "Pornește scanarea",
    val settings: String = "Setări",
    val finish: String = "Finalizează",
    val clearList: String = "Golește lista",
    val back: String = "Înapoi",
    val close: String = "Închide",
    val torch: String = "Lanternă",

    val emptyTitle: String = "Niciun cod scanat",
    val emptySubtitle: String = "Apasă „Pornește scanarea” și îndreaptă camera spre un cod de bare.",
    val scannedItems: (Int) -> String = { count -> if (count == 1) "1 cod scanat" else "$count coduri scanate" },
    val seenTimes: (Int) -> String = { times -> "×$times" },
    val remove: String = "Șterge",

    val permissionTitle: String = "Acces la cameră",
    val permissionMessage: String = "Avem nevoie de cameră pentru a citi codurile de bare. Datele imaginii rămân pe telefon.",
    val permissionGrant: String = "Permite accesul",
    val permissionDeniedHint: String = "Accesul a fost refuzat. Îl poți activa din setările sistemului.",

    val cameraError: String = "Camera nu a putut porni",

    val scannerHint: String = "Încadrează codul în chenar",
    val duplicateIgnored: String = "Cod deja în listă",
    val limitReached: String = "Ai atins limita de coduri",
    val formatDisabled: String = "Tip de cod dezactivat din setări",

    val settingsTitle: String = "Setări scanare",
    val sectionFormats: String = "Tipuri de coduri recunoscute",
    val sectionFormatsHint: String = "Cu cât sunt mai puține tipuri active, cu atât scanarea e mai rapidă și mai precisă.",
    val sectionBehavior: String = "Comportament",
    val sectionFeedback: String = "Feedback",

    val continuousScan: String = "Scanare continuă",
    val continuousScanHint: String = "Camera rămâne deschisă după fiecare cod citit.",
    val allowDuplicates: String = "Permite duplicate",
    val allowDuplicatesHint: String = "Același cod poate apărea de mai multe ori în listă.",
    val torchOnStart: String = "Lanternă la pornire",
    val torchOnStartHint: String = "Aprinde lanterna imediat ce se deschide scanerul.",
    val beepOnScan: String = "Sunet la scanare",
    val vibrateOnScan: String = "Vibrație la scanare",
    val maxItems: String = "Limită coduri",
    val maxItemsHint: (Int) -> String = { limit -> if (limit <= 0) "Fără limită" else "Maxim $limit coduri" },
    val resetDefaults: String = "Resetează la valorile implicite",
    val noFormatSelected: String = "Selectează cel puțin un tip de cod.",
)
