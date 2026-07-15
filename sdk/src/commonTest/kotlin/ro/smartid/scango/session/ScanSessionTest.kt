package ro.smartid.scango.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import ro.smartid.scango.model.BarcodeFormat
import ro.smartid.scango.model.ScanSettings

class ScanSessionTest {

    private var now = 0L
    private val clock: () -> Long = { now }

    private fun session(settings: ScanSettings) = ScanSession(settings, clock)

    @Test
    fun acceptsEnabledFormat() {
        val session = session(ScanSettings.DEFAULT)

        val outcome = session.submit("5941234567890", BarcodeFormat.EAN_13)

        assertIs<ScanOutcome.Accepted>(outcome)
        assertEquals(1, session.itemCount)
        assertEquals("5941234567890", session.result().single().value)
    }

    @Test
    fun rejectsDisabledFormat() {
        val session = session(ScanSettings.DEFAULT.copy(enabledFormats = setOf(BarcodeFormat.QR_CODE)))

        val outcome = session.submit("5941234567890", BarcodeFormat.EAN_13)

        assertEquals(ScanOutcome.IgnoredFormatDisabled, outcome)
        assertEquals(0, session.itemCount)
    }

    @Test
    fun ignoresSameCodeInsideCooldownWindow() {
        val session = session(ScanSettings.DEFAULT.copy(rescanCooldownMillis = 1_000))

        session.submit("ABC", BarcodeFormat.QR_CODE)
        now += 300
        val outcome = session.submit("ABC", BarcodeFormat.QR_CODE)

        assertEquals(ScanOutcome.IgnoredCooldown, outcome)
        assertEquals(1, session.itemCount)
    }

    @Test
    fun mergesDuplicateAfterCooldownWhenDuplicatesDisallowed() {
        val session = session(ScanSettings.DEFAULT.copy(rescanCooldownMillis = 1_000, allowDuplicates = false))

        session.submit("ABC", BarcodeFormat.QR_CODE)
        now += 2_000
        val outcome = session.submit("ABC", BarcodeFormat.QR_CODE)

        val merged = assertIs<ScanOutcome.DuplicateMerged>(outcome)
        assertEquals(2, merged.barcode.count)
        assertEquals(1, session.itemCount)
    }

    @Test
    fun addsSecondRowWhenDuplicatesAllowed() {
        val session = session(ScanSettings.DEFAULT.copy(rescanCooldownMillis = 1_000, allowDuplicates = true))

        session.submit("ABC", BarcodeFormat.QR_CODE)
        now += 2_000
        val outcome = session.submit("ABC", BarcodeFormat.QR_CODE)

        assertIs<ScanOutcome.Accepted>(outcome)
        assertEquals(2, session.itemCount)
    }

    @Test
    fun stopsAtMaxItems() {
        val session = session(ScanSettings.DEFAULT.copy(maxItems = 1, rescanCooldownMillis = 0))

        session.submit("A", BarcodeFormat.QR_CODE)
        val outcome = session.submit("B", BarcodeFormat.QR_CODE)

        assertEquals(ScanOutcome.IgnoredLimitReached, outcome)
        assertEquals(1, session.itemCount)
    }

    @Test
    fun clearResetsCooldownToo() {
        val session = session(ScanSettings.DEFAULT.copy(rescanCooldownMillis = 10_000))

        session.submit("ABC", BarcodeFormat.QR_CODE)
        session.clear()
        val outcome = session.submit("ABC", BarcodeFormat.QR_CODE)

        assertIs<ScanOutcome.Accepted>(outcome)
        assertTrue(session.itemCount == 1)
    }
}
