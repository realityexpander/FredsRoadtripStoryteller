package com.realityexpander.data

import com.russhwolf.settings.Settings
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

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

class FakeSettingsTest {

    private lateinit var fakeSettings: FakeSettings

    @BeforeTest
    fun setUp() {
        fakeSettings = FakeSettings()
    }

    @Test
    fun `FakeSettings should be able to set and get a string`() {
        val key = "key"
        val value = "value"
        fakeSettings.putString(key, value)
        assertEquals(fakeSettings.getString(key, ""), value)
    }

    @Test
    fun `FakeSettings should be able to set and get a boolean`() {
        val key = "key"
        val value = true
        fakeSettings.putBoolean(key, value)
        assertEquals(fakeSettings.getBoolean(key, false), value)
    }

    @Test
    fun `FakeSettings should be able to set and get a double`() {
        val key = "key"
        val value = 1.0
        fakeSettings.putDouble(key, value)
        assertEquals(fakeSettings.getDouble(key, 0.0), value)
    }

    @Test
    fun `FakeSettings should be able to set and get a float`() {
        val key = "key"
        val value = 1.0f
        fakeSettings.putFloat(key, value)
        assertEquals(fakeSettings.getFloat(key, 0.0f), value)
    }

    @Test
    fun `FakeSettings should be able to set and get an int`() {
        val key = "key"
        val value = 1
        fakeSettings.putInt(key, value)
        assertEquals(fakeSettings.getInt(key, 0), value)
    }

    @Test
    fun `FakeSettings should be able to set and get a long`() {
        val key = "key"
        val value = 1L
        fakeSettings.putLong(key, value)
        assertEquals(fakeSettings.getLong(key, 0L), value)
    }

    @Test
    fun `FakeSettings should be able to clear all settings`() {
        val key = "key"
        val value = 1L
        fakeSettings.putLong(key, value)
        fakeSettings.clear()
        assertEquals(fakeSettings.getLongOrNull(key), null)
    }

    @Test
    fun `FakeSettings should be able to remove a setting`() {
        val key = "key"
        val value = 1L
        fakeSettings.putLong(key, value)
        fakeSettings.remove(key)
        assertEquals(fakeSettings.getLongOrNull(key), null)
    }

}
