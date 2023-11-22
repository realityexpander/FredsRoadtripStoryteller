package com.realityexpander.data

import data.FakeSettings
import kotlin.test.BeforeTest
import kotlin.test.assertEquals

class FakeSettingsTest {

    private lateinit var fakeSettings: FakeSettings

    @BeforeTest
    fun setUp() {
        fakeSettings = FakeSettings()
    }

    @org.junit.Test
    fun `FakeSettings should be able to set and get a string`() {
        val key = "key"
        val value = "value"
        fakeSettings.putString(key, value)
        assertEquals(fakeSettings.getString(key, ""), value)
    }

    @org.junit.Test
    fun `FakeSettings should be able to set and get a boolean`() {
        val key = "key"
        val value = true
        fakeSettings.putBoolean(key, value)
        assertEquals(fakeSettings.getBoolean(key, false), value)
    }

    @org.junit.Test
    fun `FakeSettings should be able to set and get a double`() {
        val key = "key"
        val value = 1.0
        fakeSettings.putDouble(key, value)
        assertEquals(fakeSettings.getDouble(key, 0.0), value)
    }

    @org.junit.Test
    fun `FakeSettings should be able to set and get a float`() {
        val key = "key"
        val value = 1.0f
        fakeSettings.putFloat(key, value)
        assertEquals(fakeSettings.getFloat(key, 0.0f), value)
    }

    @org.junit.Test
    fun `FakeSettings should be able to set and get an int`() {
        val key = "key"
        val value = 1
        fakeSettings.putInt(key, value)
        assertEquals(fakeSettings.getInt(key, 0), value)
    }

    @org.junit.Test
    fun `FakeSettings should be able to set and get a long`() {
        val key = "key"
        val value = 1L
        fakeSettings.putLong(key, value)
        assertEquals(fakeSettings.getLong(key, 0L), value)
    }

    @org.junit.Test
    fun `FakeSettings should be able to clear all settings`() {
        val key = "key"
        val value = 1L
        fakeSettings.putLong(key, value)
        fakeSettings.clear()
        assertEquals(fakeSettings.getLongOrNull(key), null)
    }

    @org.junit.Test
    fun `FakeSettings should be able to remove a setting`() {
        val key = "key"
        val value = 1L
        fakeSettings.putLong(key, value)
        fakeSettings.remove(key)
        assertEquals(fakeSettings.getLongOrNull(key), null)
    }

}
