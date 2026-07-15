package ro.smartid.scango.settings

import android.content.Context
import ro.smartid.scango.platform.PlatformContext

internal actual fun createKeyValueStore(context: PlatformContext): KeyValueStore =
    SharedPreferencesStore(context.applicationContext)

private class SharedPreferencesStore(context: Context) : KeyValueStore {

    private val prefs = context.getSharedPreferences("ro.smartid.scango.prefs", Context.MODE_PRIVATE)

    override fun getString(key: String): String? = prefs.getString(key, null)

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    override fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }
}
