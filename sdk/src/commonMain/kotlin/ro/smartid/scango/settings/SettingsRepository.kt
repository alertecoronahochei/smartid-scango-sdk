package ro.smartid.scango.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import ro.smartid.scango.model.ScanSettings
import ro.smartid.scango.platform.PlatformContext

/** Minimal key/value persistence contract; backed by SharedPreferences / NSUserDefaults. */
interface KeyValueStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
    fun remove(key: String)
}

internal expect fun createKeyValueStore(context: PlatformContext): KeyValueStore

/**
 * Single source of truth for [ScanSettings]. Reads are cheap (in-memory [StateFlow]),
 * writes go straight to disk so the host can be killed at any time.
 */
class SettingsRepository internal constructor(
    private val store: KeyValueStore,
    private val defaults: ScanSettings,
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private val _settings = MutableStateFlow(readFromDisk())

    val settings: StateFlow<ScanSettings> = _settings.asStateFlow()

    /** Snapshot, for hosts that do not consume flows (e.g. Swift call sites). */
    val current: ScanSettings get() = _settings.value

    fun save(settings: ScanSettings) {
        _settings.value = settings
        store.putString(KEY_SETTINGS, json.encodeToString(ScanSettings.serializer(), settings))
    }

    fun update(transform: (ScanSettings) -> ScanSettings) {
        save(transform(_settings.value))
    }

    fun resetToDefaults() {
        store.remove(KEY_SETTINGS)
        _settings.value = defaults
    }

    private fun readFromDisk(): ScanSettings {
        val raw = store.getString(KEY_SETTINGS) ?: return defaults
        return runCatching { json.decodeFromString(ScanSettings.serializer(), raw) }.getOrDefault(defaults)
    }

    private companion object {
        const val KEY_SETTINGS = "scango.settings.v1"
    }
}
