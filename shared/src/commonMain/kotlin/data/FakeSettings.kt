package data

import com.russhwolf.settings.Settings

class FakeSettings: Settings {
    // Use non-persistent memory amp for testing
    private val settingsMap: MutableMap<String, Any?> = mutableMapOf()
    override val keys: Set<String>
        get() = settingsMap.keys
    override val size: Int
        get() = settingsMap.size


    override fun clear() {
        keys.forEach { key ->
            settingsMap.remove(key)
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if(keys.contains(key)) {
            settingsMap[key] as Boolean
        } else {
            defaultValue
        }
    }

    override fun getBooleanOrNull(key: String): Boolean? {
        return if(keys.contains(key)) {
            settingsMap[key] as Boolean
        } else {
            null
        }
    }

    override fun getDouble(key: String, defaultValue: Double): Double {
        return if(keys.contains(key)) {
            settingsMap[key] as Double
        } else {
            defaultValue
        }
    }

    override fun getDoubleOrNull(key: String): Double? {
        return if(keys.contains(key)) {
            settingsMap[key] as Double
        } else {
            null
        }
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return if(keys.contains(key)) {
            settingsMap[key] as Float
        } else {
            defaultValue
        }
    }

    override fun getFloatOrNull(key: String): Float? {
        return if(keys.contains(key)) {
            settingsMap[key] as Float
        } else {
            null
        }
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return if(keys.contains(key)) {
            settingsMap[key] as Int
        } else {
            defaultValue
        }
    }

    override fun getIntOrNull(key: String): Int? {
        return if(keys.contains(key)) {
            settingsMap[key] as Int
        } else {
            null
        }
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return if(keys.contains(key)) {
            settingsMap[key] as Long
        } else {
            defaultValue
        }
    }

    override fun getLongOrNull(key: String): Long? {
        return if(keys.contains(key)) {
            settingsMap[key] as Long
        } else {
            null
        }
    }

    override fun getString(key: String, defaultValue: String): String {
        return if(keys.contains(key)) {
            settingsMap[key] as String
        } else {
            defaultValue
        }
    }

    override fun getStringOrNull(key: String): String? {
        return if(keys.contains(key)) {
            settingsMap[key] as String
        } else {
            null
        }
    }

    override fun hasKey(key: String): Boolean {
        return keys.contains(key)
    }

    override fun putBoolean(key: String, value: Boolean) {
        settingsMap[key] = value
    }

    override fun putDouble(key: String, value: Double) {
        settingsMap[key] = value
    }

    override fun putFloat(key: String, value: Float) {
        settingsMap[key] = value
    }

    override fun putInt(key: String, value: Int) {
        settingsMap[key] = value
    }

    override fun putLong(key: String, value: Long) {
        settingsMap[key] = value
    }

    override fun putString(key: String, value: String) {
        settingsMap[key] = value
    }

    override fun remove(key: String) {
        settingsMap.remove(key)
    }
}
