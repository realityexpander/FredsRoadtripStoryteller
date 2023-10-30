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

        assert(result.markerIdToMarker.isNotEmpty())
        assert(result.markerIdToMarker.contains("M207019"))
    }

    @Test
    fun `Subtitle should have no Em-dashes`() {
        val subtitle = "This is a subtitle with no em-dashes"
        assertFalse(subtitle.contains("—"))

        val markersPageHtml = tepoztlanMarkersHtmlPage1()

        val result = parseMarkersPageHtml(markersPageHtml)
        assertFalse(
            result.markerIdToMarker["M207019"]?.subtitle?.contains("—") ?: false,
            "Subtitle should have no em-dashes, but it does: ${result.markerIdToMarker["M207019"]?.subtitle}"
        )
    }
}
