package com.realityexpander.data.loadMarkers

import data.loadMarkers.parseMarkersPageHtml
import data.loadMarkers.sampleData.tepoztlanMarkersPage1Html
import kotlin.test.Test
import kotlin.test.assertFalse

class ParseMarkersPageHtmlTest {

    @Test
    fun `Parse markers page html should be Successful`() {
        val markersPageHtml = tepoztlanMarkersPage1Html()

        val result = parseMarkersPageHtml(markersPageHtml)

        assert(result.markerIdToMarkerMap.isNotEmpty())
        assert(result.markerIdToMarkerMap.contains("M207019"))
    }

    @Test
    fun `Subtitle should have no Em-dashes`() {
        val subtitle = "This is a subtitle with no em-dashes"
        assertFalse(subtitle.contains("—"))

        val markersPageHtml = tepoztlanMarkersPage1Html()

        val result = parseMarkersPageHtml(markersPageHtml)
        assertFalse(
            result.markerIdToMarkerMap["M207019"]?.subtitle?.contains("—") ?: false,
            "Subtitle should have no em-dashes, but it does: ${result.markerIdToMarkerMap["M207019"]?.subtitle}"
        )
    }
}
