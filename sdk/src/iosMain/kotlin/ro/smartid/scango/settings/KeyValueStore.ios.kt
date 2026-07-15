package ro.smartid.scango.settings

import platform.Foundation.NSUserDefaults
import ro.smartid.scango.platform.PlatformContext

internal actual fun createKeyValueStore(context: PlatformContext): KeyValueStore = UserDefaultsStore()

private class UserDefaultsStore : KeyValueStore {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String): String? = defaults.stringForKey(key)

    override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }

    override fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }
}
