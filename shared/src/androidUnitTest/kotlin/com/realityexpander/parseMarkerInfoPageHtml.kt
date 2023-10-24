package com.realityexpander

import data.loadMarkerInfo.parseMarkerInfoPageHtml
import data.loadMarkerInfo.sampleData.almadenVineyardsM2580
import data.loadMarkerInfo.sampleData.deAnzaExpeditionM38342
import json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndroidParseMarkerInfoPageHtml {

    @Test
    fun `Parse MarkerInfo Page Html for almadenVineyardsM2580 is Successful`() {
        val markerInfoPageHtml = almadenVineyardsM2580()

        val result = parseMarkerInfoPageHtml(markerInfoPageHtml)
        val markerInfo = result.second!!

        // println(json.encodeToString(MapMarker.serializer(), markerInfo))

        // Title
        assertTrue(
            markerInfo.title.contains("Almadén Vineyards"),
            "Title was not found"
        )

        // Inscription
        assertTrue(
            markerInfo.inscription.contains("1852 Charles LeFranc"),
            "Inscription was not found"
        )

        // Main Photo URL
        assertTrue(
            markerInfo.mainPhotoUrl.contains("https://www.hmdb.org/Photos/7/Photo7252.jpg?11252005"),
            "Marker Photo Url was not found"
        )

        // Marker Photos
        assertTrue(
            markerInfo.markerPhotos.size == 3,
            "Some Marker Photos are missing"
        )
        assertTrue(
            markerInfo.markerPhotos[1] == "https://www.hmdb.org/Photos/7/Photo7253.jpg?11252005",
            "Marker Photo at index 1 was not found"
        )

        // Photo Attributions
        assertTrue(
            markerInfo.photoAttributions.size == 3,
            "Some Photo Attributions are missing"
        )
        assertTrue(
            markerInfo.photoAttributions[0] == "Photographed By Leticia A. Kohnen, June 7, 2007",
            "Additional Photo Attribution at index 0 was not found"
        )

        // Photo Captions
        assertTrue(
            markerInfo.photoCaptions.size == 3,
            "Some Photo Captions are missing"
        )
        assertTrue(
            markerInfo.photoCaptions[0] == "1. State Historic Landmark 505",
            "Additional Photo Caption at index 0 was not found"
        )

        // Location
        assertTrue(
            markerInfo.location.contains("Marker is in San Jose, California, in Santa Clara County."),
            "Location was not found"
        )
        assertFalse(
            markerInfo.location.contains("Touch for map.") ||
                    markerInfo.location.contains("Touch for directions."),
            "Location contains Touch for map or Touch for directions"
        )

        // Erected
        assertTrue(
            markerInfo.erected.contains("1953 by California State Parks Commission"),
            "Erected was not found"
        )
    }

    @Test
    fun `Parse MarkerInfo Page Html for deAnzaExpeditionM38342 is Successful`() {
        val markerInfoPageHtml = deAnzaExpeditionM38342()

        val result = parseMarkerInfoPageHtml(markerInfoPageHtml)
        val markerInfo = result.second!!

        // println(json.encodeToString(MapMarker.serializer(), markerInfo))

        // Title
        assertTrue(
            markerInfo.title.contains("De Anza Expedition 1775 - 1776"),
            "Title was not found"
        )

        // Inscription
        assertTrue(
            markerInfo.inscription.contains("Lt. Juan Bautista de Anza and party crossed"),
            "Inscription was not found"
        )

        // Main Photo URL
        assertTrue(
            markerInfo.mainPhotoUrl.contains("https://www.hmdb.org/Photos1/137/Photo137793.jpg?11252005"),
            "Marker Photo Url was not found"
        )

        // Marker Photos
        assertTrue(
            markerInfo.markerPhotos.size == 2,
            "Some Marker Photos are missing"
        )
        assertTrue(
            markerInfo.markerPhotos[1] == "https://www.hmdb.org/Photos1/137/Photo137758.jpg?11252005",
            "Marker Photo at index 1 was not found"
        )

        // Photo Attributions
        assertTrue(
            markerInfo.photoAttributions.size == 2,
            "Some Photo Attributions are missing"
        )
        assertTrue(
            markerInfo.photoAttributions[0] == "Photographed By Sunny L. Wagstaff",
            "Additional Photo Attribution at index 0 was not found"
        )

        // Photo Captions
        assertTrue(
            markerInfo.photoCaptions.size == 2,
            "Some Photo Captions are missing"
        )
        assertTrue(
            markerInfo.photoCaptions[0] == "1. Juan Bautista de Anza Marker",
            "Additional Photo Caption at index 0 was not found"
        )

        // Location
        assertTrue(
            markerInfo.location.contains("It is in Dartmouth. Marker is at the intersection of Meridian Avenue "),
            "Location was not found"
        )
        assertFalse(
            markerInfo.location.contains("Touch for map.") ||
                    markerInfo.location.contains("Touch for directions."),
            "Location contains Touch for map or Touch for directions"
        )

        // Erected
        assertTrue(
            markerInfo.erected.isBlank(),
            "Erected was found but should be blank"
        )
    }
}
