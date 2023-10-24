package com.realityexpander

import data.loadMarkerInfo.parseMarkerInfoPageHtml
import data.loadMarkerInfo.sampleData.almadenVineyardsM2580
import data.loadMarkerInfo.sampleData.deAnzaExpeditionM38342
import data.loadMarkerInfo.sampleData.elTepoztecoNationalParkM207314
import data.loadMarkerInfo.sampleData.firstCityCouncilOfTepoztlanM207310
import json
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParseMarkerInfoPageHtmlTest {

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

    @Test
    fun `Parse MarkerInfo Page Html for elTepoztecoNationalParkM207314 is Successful`() {
        val markerInfoPageHtml = elTepoztecoNationalParkM207314()

        val result = parseMarkerInfoPageHtml(markerInfoPageHtml)
        val markerInfo = result.second!!

         // println(json.encodeToString(MapMarker.serializer(), markerInfo))

        // Title
        assertTrue(
            markerInfo.title.contains("El Tepozteco National Park"),
            "Title was not found"
        )

        // Inscription
        assertTrue(
            markerInfo.inscription.contains(""), // multi-language inscription
            "Inscription was not found"
        )
        assertTrue(
            markerInfo.inscription.contains("English translation"), // multi-language inscription
            "Inscription was incorrect for a multi-language inscription"
        )
        assertTrue(
            markerInfo.englishInscription.contains("This park was decreed as a Protected Area on January 22, 1937"),
            "English Inscription was not found"
        )
        assertTrue(
            markerInfo.spanishInscription.contains("Este parque fue decretado como Área Protegida el 22 de enero de 1937"),
            "Spanish Inscription was not found"
        )

        // Main Photo URL
        assertTrue(
            markerInfo.mainPhotoUrl.contains("https://www.hmdb.org/Photos6/681/Photo681931.jpg?1052022105400PM"),
            "Marker Photo Url was not found"
        )

        // Marker Photos
        assertTrue(
            markerInfo.markerPhotos.size == 3,
            "Some Marker Photos are missing"
        )
        assertTrue(
            markerInfo.markerPhotos[1] == "https://www.hmdb.org/Photos6/681/Photo681932.jpg?1052022105600PM",
            "Marker Photo at index 1 was not found"
        )

        // Photo Attributions
        assertTrue(
            markerInfo.photoAttributions.size == 3,
            "Some Photo Attributions are missing"
        )
        assertTrue(
            markerInfo.photoAttributions[0] == "Photographed By J. Makali Bruton, August 6, 2022",
            "Additional Photo Attribution at index 0 was not found"
        )

        // Photo Captions
        assertTrue(
            markerInfo.photoCaptions.size == 3,
            "Some Photo Captions are missing"
        )
        assertTrue(
            markerInfo.photoCaptions[0] == "1. El Tepozteco National Park Marker",
            "Additional Photo Caption at index 0 was not found"
        )

        // Location
        assertTrue(
            markerInfo.location.contains("Marker is in Tepoztlán, Morelos. Marker is on 5 de Mayo just south of Avenida"),
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

    @Test
    fun `Parse MarkerInfo Page Html for firstCityCouncilOfTepoztlanM207310 is Successful`() {
        val markerInfoPageHtml = firstCityCouncilOfTepoztlanM207310()

        val result = parseMarkerInfoPageHtml(markerInfoPageHtml)
        val markerInfo = result.second!!

         //println(json.encodeToString(MapMarker.serializer(), markerInfo))

        // Title
        assertTrue(
            markerInfo.title.contains("First City Council of Tepoztlán"),
            "Title was not found"
        )

        // Inscription
        assertTrue(
            markerInfo.inscription.contains(""), // multi-language inscription
            "Inscription was not found"
        )
        assertTrue(
            markerInfo.inscription.contains("English translation"), // multi-language inscription
            "Inscription was incorrect for a multi-language inscription"
        )
        assertTrue(
            markerInfo.englishInscription.contains("In 1820, on September 8, the day on which Tepuztecatl was celebrated, the town of Tepoztlan"),
            "English Inscription was not found"
        )
        assertTrue(
            markerInfo.spanishInscription.contains("En 1820, el 8 de septiembre, día en que se festejaba a Tepuztecatl"),
            "Spanish Inscription was not found"
        )

        // Main Photo URL
        assertTrue(
            markerInfo.mainPhotoUrl.contains("https://www.hmdb.org/Photos6/681/Photo681920.jpg?1052022102800PM"),
            "Marker Photo Url was not found"
        )

        // Marker Photos
        assertTrue(
            markerInfo.markerPhotos.size == 4,
            "Some Marker Photos are missing"
        )
        assertTrue(
            markerInfo.markerPhotos[1] == "https://www.hmdb.org/Photos6/681/Photo681923.jpg?1052022103400PM",
            "Marker Photo at index 1 was not found"
        )

        // Photo Attributions
        assertTrue(
            markerInfo.photoAttributions.size == 4,
            "Some Photo Attributions are missing"
        )
        assertTrue(
            markerInfo.photoAttributions[0] == "Photographed By J. Makali Bruton, August 6, 2022",
            "Additional Photo Attribution at index 0 was not found"
        )

        // Photo Captions
        assertTrue(
            markerInfo.photoCaptions.size == 4,
            "Some Photo Captions are missing"
        )
        assertTrue(
            markerInfo.photoCaptions[0] == "1. First City Council of Tepotzlán Marker",
            "Additional Photo Caption at index 0 was not found"
        )

        // Location
        assertTrue(
            markerInfo.location.contains("Marker is in Tepoztlán, Morelos. Marker can be reached from 5 de Mayo just south of Avenida Ignacio"),
            "Location was not found"
        )
        assertFalse(
            markerInfo.location.contains("Touch for map.") ||
                    markerInfo.location.contains("Touch for directions."),
            "Location contains Touch for map or Touch for directions"
        )

        // Erected
        assertTrue(
            markerInfo.erected.contains("2020."),
            "Erected was not found"
        )
    }
}
