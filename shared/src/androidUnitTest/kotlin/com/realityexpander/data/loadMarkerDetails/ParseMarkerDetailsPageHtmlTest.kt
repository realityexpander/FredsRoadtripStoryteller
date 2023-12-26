package com.realityexpander.data.loadMarkerDetails

import data.loadMarkerDetails.parseMarkerDetailsPageHtml
import data.loadMarkerDetails.sampleData.almadenVineyardsM2580MarkerDetailsHtml
import data.loadMarkerDetails.sampleData.deAnzaExpeditionM38342MarkerDetailsHtml
import data.loadMarkerDetails.sampleData.elTepoztecoNationalParkM207314MarkerDetailsHtml
import data.loadMarkerDetails.sampleData.firstCityCouncilOfTepoztlanM207310MarkerDetailsHtml
import data.loadMarkerDetails.sampleData.mintBuildingM233374MarkerDetailsHtml
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParseMarkerDetailsPageHtmlTest {

    @Test
    fun `Parse MarkerDetails Page Html for almadenVineyardsM2580 is Successful`() {
        val markerInfoPageHtml = almadenVineyardsM2580MarkerDetailsHtml()

        val result = parseMarkerDetailsPageHtml(markerInfoPageHtml)
        val markerDetails = result.second!!

        // For debugging
        //println(json.encodeToString(maps.MapMarker.serializer(), markerDetails))

        // ID
        assertTrue(
            markerDetails.id == "M2580",
            "ID was not found"
        )

        // Title
        assertTrue(
            markerDetails.title.contains("Almadén Vineyards"),
            "Title was not found"
        )

        // Inscription
        assertTrue(
            markerDetails.inscription.contains("1852 Charles LeFranc"),
            "Inscription was not found"
        )

        // Main Photo URL
        assertTrue(
            markerDetails.mainPhotoUrl.contains("https://www.hmdb.org/Photos/7/Photo7252.jpg?11252005"),
            "Marker Photo Url was not found"
        )

        // Marker Photos
        assertTrue(
            markerDetails.markerPhotos.size == 3,
            "Some Marker Photos are missing"
        )
        assertTrue(
            markerDetails.markerPhotos[1] == "https://www.hmdb.org/Photos/7/Photo7253.jpg?11252005",
            "Marker Photo at index 1 was not found"
        )

        // Photo Attributions
        assertTrue(
            markerDetails.photoAttributions.size == 3,
            "Some Photo Attributions are missing"
        )
        assertTrue(
            markerDetails.photoAttributions[0] == "Photographed By Leticia A. Kohnen, June 7, 2007",
            "Additional Photo Attribution at index 0 was not found"
        )

        // Photo Captions
        assertTrue(
            markerDetails.photoCaptions.size == 3,
            "Some Photo Captions are missing"
        )
        assertTrue(
            markerDetails.photoCaptions[0] == "1. State Historic Landmark 505",
            "Additional Photo Caption at index 0 was not found"
        )

        // Location
        assertTrue(
            markerDetails.location.contains("Marker is in San Jose, California, in Santa Clara County."),
            "Location was not found"
        )
        assertFalse(
            markerDetails.location.contains("Touch for map.") ||
                    markerDetails.location.contains("Touch for directions."),
            "Location contains Touch for map or Touch for directions"
        )

        // Erected
        assertTrue(
            markerDetails.erected.contains("1953 by California State Parks Commission"),
            "Erected was not found"
        )
    }

    @Test
    fun `Parse MarkerDetails Page Html for deAnzaExpeditionM38342 is Successful`() {
        val markerInfoPageHtml = deAnzaExpeditionM38342MarkerDetailsHtml()

        val result = parseMarkerDetailsPageHtml(markerInfoPageHtml)
        val markerDetails = result.second!!

        // For debugging
        // println(json.encodeToString(maps.MapMarker.serializer(), markerDetails))

        // ID
        assertTrue(
            markerDetails.id == "M38342",
            "ID was not found"
        )

        // Title
        assertTrue(
            markerDetails.title.contains("De Anza Expedition 1775 - 1776"),
            "Title was not found"
        )

        // Inscription
        assertTrue(
            markerDetails.inscription.contains("Lt. Juan Bautista de Anza and party crossed"),
            "Inscription was not found"
        )

        // Main Photo URL
        assertTrue(
            markerDetails.mainPhotoUrl.contains("https://www.hmdb.org/Photos1/137/Photo137793.jpg?11252005"),
            "Marker Photo Url was not found"
        )

        // Marker Photos
        assertTrue(
            markerDetails.markerPhotos.size == 2,
            "Some Marker Photos are missing"
        )
        assertTrue(
            markerDetails.markerPhotos[1] == "https://www.hmdb.org/Photos1/137/Photo137758.jpg?11252005",
            "Marker Photo at index 1 was not found"
        )

        // Photo Attributions
        assertTrue(
            markerDetails.photoAttributions.size == 2,
            "Some Photo Attributions are missing"
        )
        assertTrue(
            markerDetails.photoAttributions[0] == "Photographed By Sunny L. Wagstaff",
            "Additional Photo Attribution at index 0 was not found"
        )

        // Photo Captions
        assertTrue(
            markerDetails.photoCaptions.size == 2,
            "Some Photo Captions are missing"
        )
        assertTrue(
            markerDetails.photoCaptions[0] == "1. Juan Bautista de Anza Marker",
            "Additional Photo Caption at index 0 was not found"
        )

        // Location
        assertTrue(
            markerDetails.location.contains("It is in Dartmouth. Marker is at the intersection of Meridian Avenue "),
            "Location was not found"
        )
        assertFalse(
            markerDetails.location.contains("Touch for map.") ||
                    markerDetails.location.contains("Touch for directions."),
            "Location contains Touch for map or Touch for directions"
        )

        // Erected
        assertTrue(
            markerDetails.erected.isBlank(),
            "Erected was found but should be blank"
        )
    }

    @Test
    fun `Parse MarkerDetails Page Html for elTepoztecoNationalParkM207314 is Successful`() {
        val markerInfoPageHtml = elTepoztecoNationalParkM207314MarkerDetailsHtml()

        val result = parseMarkerDetailsPageHtml(markerInfoPageHtml)
        val markerDetails = result.second!!

        // For debugging
        //println(json.encodeToString(Marker.serializer(), markerDetails))

        // ID
        assertTrue(
            markerDetails.id == "M207314",
            "ID was not found"
        )

        // Title
        assertTrue(
            markerDetails.title.contains("El Tepozteco National Park"),
            "Title was not found"
        )

        // Subtitle
        assertTrue(
            markerDetails.subtitle.contains(""),
            "Subtitle was found and there shouldn't be one"
        )

        // Inscription
        assertTrue(
            markerDetails.inscription.contains(""), // multi-language inscription
            "Inscription was not found"
        )
        assertTrue(
            markerDetails.inscription.contains("English translation"), // multi-language inscription
            "Inscription was incorrect for a multi-language inscription"
        )
        assertTrue(
            markerDetails.englishInscription.contains("This park was decreed as a Protected Area on January 22, 1937"),
            "English Inscription was not found"
        )
        assertTrue(
            markerDetails.spanishInscription.contains("Este parque fue decretado como Área Protegida el 22 de enero de 1937"),
            "Spanish Inscription was not found"
        )

        // Main Photo URL
        assertTrue(
            markerDetails.mainPhotoUrl.contains("https://www.hmdb.org/Photos6/681/Photo681931.jpg?1052022105400PM"),
            "Marker Photo Url was not found"
        )

        // Marker Photos
        assertTrue(
            markerDetails.markerPhotos.size == 3,
            "Some Marker Photos are missing"
        )
        assertTrue(
            markerDetails.markerPhotos[1] == "https://www.hmdb.org/Photos6/681/Photo681932.jpg?1052022105600PM",
            "Marker Photo at index 1 was not found"
        )

        // Photo Attributions
        assertTrue(
            markerDetails.photoAttributions.size == 3,
            "Some Photo Attributions are missing"
        )
        assertTrue(
            markerDetails.photoAttributions[0] == "Photographed By J. Makali Bruton, August 6, 2022",
            "Additional Photo Attribution at index 0 was not found"
        )

        // Photo Captions
        assertTrue(
            markerDetails.photoCaptions.size == 3,
            "Some Photo Captions are missing"
        )
        assertTrue(
            markerDetails.photoCaptions[0] == "1. El Tepozteco National Park Marker",
            "Additional Photo Caption at index 0 was not found"
        )

        // Location
        assertTrue(
            markerDetails.location.contains("Marker is in Tepoztlán, Morelos. Marker is on 5 de Mayo just south of Avenida"),
            "Location was not found"
        )
        assertFalse(
            markerDetails.location.contains("Touch for map.") ||
                    markerDetails.location.contains("Touch for directions."),
            "Location contains Touch for map or Touch for directions"
        )

        // Erected
        assertTrue(
            markerDetails.erected.isBlank(),
            "Erected was found but should be blank"
        )

        // Check that inscription has proper spaces between words
        assertTrue(
            markerDetails.englishInscription == "This park was decreed as a Protected Area on January 22, 1937, has an area of 23,286 hectares and is located in the Municipality of Tepoztlán, Morelos. This Park is made up of six types of vegetation, from the Oyamel forest to the Low Deciduous Forest. As for fauna, there are 237 species of birds, 60 of mammals and 45 of reptiles. During your visit you can appreciate the natural beauty of this Park, which is incorporated into the \"Chichinautzin Biological Corridor\" and ranges from the Lagunas de Zempoala National Park to the northern area of the municipality of Atlatlahucan. The mountainous characteristics of the area have stimulated the isolation of animal and plant species, however, in this area there are 64 at risk. With your help we keep this site.",
            "Inscription has improper spaces between words"
        )
    }

    @Test
    fun `Parse MarkerDetails Page Html for firstCityCouncilOfTepoztlanM207310 is Successful`() {
        val markerInfoPageHtml = firstCityCouncilOfTepoztlanM207310MarkerDetailsHtml()

        val result = parseMarkerDetailsPageHtml(markerInfoPageHtml)
        val markerDetails = result.second!!

        // For debugging
        //println(json.encodeToString(Marker.serializer(), markerDetails))

        // ID
        assertTrue(
            markerDetails.id == "M207310",
            "ID was not found"
        )

        // Title
        assertTrue(
            markerDetails.title.contains("First City Council of Tepoztlán"),
            "Title was not found"
        )

        // Subtitle
        assertTrue(
            markerDetails.subtitle.contains(""), // no subtitle
            "Subtitle was not found"
        )

        // Inscription
        assertTrue(
            markerDetails.inscription.contains(""), // multi-language inscription
            "Inscription was not found"
        )
        assertTrue(
            markerDetails.inscription.contains("English translation"), // multi-language inscription
            "Inscription was incorrect for a multi-language inscription"
        )
        assertTrue(
            markerDetails.englishInscription.contains("In 1820, on September 8, the day on which Tepuztecatl was celebrated, the town of Tepoztlan"),
            "English Inscription was not found"
        )
        assertTrue(
            markerDetails.spanishInscription.contains("En 1820, el 8 de septiembre, día en que se festejaba a Tepuztecatl"),
            "Spanish Inscription was not found"
        )

        // Main Photo URL
        assertTrue(
            markerDetails.mainPhotoUrl.contains("https://www.hmdb.org/Photos6/681/Photo681920.jpg?1052022102800PM"),
            "Marker Photo Url was not found"
        )

        // Marker Photos
        assertTrue(
            markerDetails.markerPhotos.size == 4,
            "Some Marker Photos are missing"
        )
        assertTrue(
            markerDetails.markerPhotos[1] == "https://www.hmdb.org/Photos6/681/Photo681923.jpg?1052022103400PM",
            "Marker Photo at index 1 was not found"
        )

        // Photo Attributions
        assertTrue(
            markerDetails.photoAttributions.size == 4,
            "Some Photo Attributions are missing"
        )
        assertTrue(
            markerDetails.photoAttributions[0] == "Photographed By J. Makali Bruton, August 6, 2022",
            "Additional Photo Attribution at index 0 was not found"
        )

        // Photo Captions
        assertTrue(
            markerDetails.photoCaptions.size == 4,
            "Some Photo Captions are missing"
        )
        assertTrue(
            markerDetails.photoCaptions[0] == "1. First City Council of Tepotzlán Marker",
            "Additional Photo Caption at index 0 was not found"
        )

        // Location
        assertTrue(
            markerDetails.location.contains("Marker is in Tepoztlán, Morelos. Marker can be reached from 5 de Mayo just south of Avenida Ignacio"),
            "Location was not found"
        )
        assertFalse(
            markerDetails.location.contains("Touch for map.") ||
                    markerDetails.location.contains("Touch for directions."),
            "Location contains Touch for map or Touch for directions"
        )

        // Erected
        assertTrue(
            markerDetails.erected.contains("2020."),
            "Erected was not found"
        )

        // Credits
        assertTrue(
            markerDetails.credits.contains(""), // no credits
            "Credits were found and shouldn't have been"
        )

        // Check that inscription has proper spaces between words
        assertTrue(
            markerDetails.englishInscription == "In 1820, on September 8, the day on which Tepuztecatl was celebrated, the town of Tepoztlan installed its first town hall. In 1826 the municipality of Tepoztlán, as part of the State of Mexico, was ratified. Honorable City Council of Tepoztlan Rogelio Torres Ortega, Municipal President of Tepoztlan 2019-2021 Government of the State of Morelos Cuauhtemoc Blanco Bravo, Governor of the State of Photographed By J. Makali Bruton, August 6, 2022 2. Additional tablets near the First City Council of Tepotzlán Marker Erected 2020.",
            "Inscription has improper spaces between words"
        )
    }

    @Test
    fun `Parse MarkerDetails Page Html for mintBuildingM233374 is Successful`() {
        val markerInfoPageHtml = mintBuildingM233374MarkerDetailsHtml()

        val result = parseMarkerDetailsPageHtml(markerInfoPageHtml)
        val markerDetails = result.second!!

        // For debugging
        //println(json.encodeToString(Marker.serializer(), markerDetails))

        // ID
        assertTrue(
            markerDetails.id == "M233374",
            "ID was not found"
        )

        // Title
        assertTrue(
            markerDetails.title.contains("Mint Building"),
            "Title was not found"
        )

        // Subtitle
        assertTrue(
            markerDetails.subtitle.contains("Charles R. Jonas Federal Building"),
            "Subtitle was not found"
        )

        // Inscription
        assertTrue(
            markerDetails.inscription.contains("Here stood the first branch Mint of the United States."),
            "Inscription was not found"
        )

        // Main Photo URL
        assertTrue(
            markerDetails.mainPhotoUrl.contains("https://www.hmdb.org/Photos7/751/Photo751617.jpg?9242023125500AM"),
            "Marker Photo Url was not found"
        )

        // Marker Photos
        assertTrue(
            markerDetails.markerPhotos.size == 5,
            "Some Marker Photos are missing"
        )
        assertTrue(
            markerDetails.markerPhotos[1] == "https://www.hmdb.org/Photos7/751/Photo751618.jpg?924202310700AM",
            "Marker Photo at index 1 was not found"
        )

        // Photo Attributions
        assertTrue(
            markerDetails.photoAttributions.size == 5,
            "Some Photo Attributions are missing"
        )
        assertTrue(
            markerDetails.photoAttributions[0] == "Photographed By J.T. Lambrou, September 1, 2023",
            "Photo Attribution at index 0 was not found"
        )

        // Photo Captions
        assertTrue(
            markerDetails.photoCaptions.size == 5,
            "Some Photo Captions are missing"
        )
        assertTrue(
            markerDetails.photoCaptions[0] == "1. Mint Building Marker",
            "Photo Caption at index 0 was not found"
        )

        // Erected
        assertTrue(
            markerDetails.erected.contains(""), // no erected date
            "Erected was found and should not have been"
        )

        // Credits
        assertTrue(
            markerDetails.credits.contains(""),  // no credits
            "Credits was not found and should not have been"
        )

        // Location
        assertTrue(
            markerDetails.location.contains("35 ° 13.781′ N, 80 ° 50.785′ W. Marker is in Charlotte, North Carolina, in Mecklenburg County."),
            "Location was not found"
        )
    }
}
