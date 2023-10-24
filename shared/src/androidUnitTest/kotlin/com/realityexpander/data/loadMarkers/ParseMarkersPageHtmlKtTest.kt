package com.realityexpander.data.loadMarkers

import data.loadMarkers.parseMarkersPageHtml
import data.loadMarkers.sampleData.tepoztlanMarkersHtmlPage1
import kotlin.test.Test
import kotlin.test.assertFalse

class ParseMarkersPageHtmlTest {

    @Test
    fun `Parse markers page html should be Successful`() {
        val markersPageHtml = tepoztlanMarkersHtmlPage1()

        val result = parseMarkersPageHtml(markersPageHtml)

        assert(result.markerInfos.isNotEmpty())
        assert(result.markerInfos.contains("M207019"))
    }

    @Test
    fun `Subtitle should have no Em-dashes`() {
        val subtitle = "This is a subtitle with no em-dashes"
        assertFalse(subtitle.contains("—"))

        val markersPageHtml = tepoztlanMarkersHtmlPage1()

        val result = parseMarkersPageHtml(markersPageHtml)
        assertFalse(
            result.markerInfos["M207019"]?.shortDescription?.contains("—") ?: false,
            "Subtitle should have no em-dashes, but it does: ${result.markerInfos["M207019"]?.shortDescription}"
        )
    }
}
