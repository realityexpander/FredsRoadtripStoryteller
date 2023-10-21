package com.realityexpander

import getPlatformName

class AppKtTest {
    @org.junit.Test
    fun testExample() {
        val result = getPlatformName()
        kotlin.test.assertTrue(result.contains("Android") || result.contains("iOS"), "Android or iOS was found")
    }
}
