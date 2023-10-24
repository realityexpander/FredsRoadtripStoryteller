package data.loadMarkerInfo

import MapMarker
import co.touchlab.kermit.Logger
import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import data.LoadingState
import data.loadMarkers.kBaseHmdbDotOrgUrl


fun parseMarkerInfoPageHtml(rawPageHtml: String): Pair<String?, MapMarker?> {

    var mapMarkerResult = MapMarker(location = "")
    val rawMarkerInfoStrings = mutableListOf<String>()

    if (rawPageHtml.isBlank()) {
        Logger.w { "htmlResponse is Blank" }
        return Pair("htmlResponse is Blank", null)
    }

    fun markerInfoPageHandler(): KsoupHtmlHandler {
        var isCapturingText = false
        var isCapturingTitleText = false
        var isCapturingInscriptionText = false
        var isCapturingPhotoCaption = false
        var isCapturingPhotoAttribution = false

        var isCapturingErectedTextPhase1 = false
        var isCapturingErectedTextPhase2 = false

        var isCapturingLocationTextPhase1 = false
        var isCapturingLocationTextPhase2 = false

        return KsoupHtmlHandler.Builder()
            .onOpenTag { tagName, attributes, _ ->
                // title - Start
                // <h1>Almadén Vineyards</h1>
                if(tagName == "h1") {
                    isCapturingText = true
                    isCapturingTitleText = true
                }

                // inscription - Start
                // <div id="inscription1" style="display:none;">Almadén Vineyards. . On this site in 1852 Charles LeFranc made the first commercial planting of fine European wine grapes in Santa Clara County and founded Almadén Vineyards. LeFranc imported cuttings from vines in the celebrated wine districts of his native France, shipping them around the Horn by sail. . This historical marker  was erected  in 1953 by California State Parks Commission. It  is in South San Jose  in Santa Clara County California</div>
                if(tagName == "div" && attributes["id"] == "inscription1") {
                    isCapturingText = true
                    isCapturingInscriptionText = true
                }

                // markerPhotoUrl & additionalPhotos - (self closing tag)
                // <img loading="lazy" class="photoimage" src="Photos/7/Photo7252.jpg?11252005" style="max-height:300px;" alt="State Historic Landmark 505 image. Click for full size." title="State Historic Landmark 505. Click for full size.">
                if(tagName == "img" && attributes["class"] == "photoimage") {
                    val markerPhotoUrl = kBaseHmdbDotOrgUrl + (attributes["src"] ?: "")

                    // Set first photo as the main photo
                    if(mapMarkerResult.mainPhotoUrl.isBlank()) {
                        mapMarkerResult = mapMarkerResult.copy(
                            mainPhotoUrl = markerPhotoUrl
                        )
                    }

                    // Add all photos to markerPhotos list
                    mapMarkerResult =
                        mapMarkerResult.copy(
                            markerPhotos = mapMarkerResult.markerPhotos.plus(markerPhotoUrl)
                        )
                }

                // markerPhotoCaption
                // <div class="imagecaption">1. State Historic Landmark 505</div>
                if(tagName == "div" && attributes["class"] == "imagecaption") {
                    isCapturingText = true
                    isCapturingPhotoCaption = true
                }

                // markerPhotoAttribution
                // <div class="imagecredit">Photographed By Leticia A. Kohnen,  June 7, 2007</div>
                if(tagName == "div" && attributes["class"] == "imagecredit") {
                    isCapturingText = true
                    isCapturingPhotoAttribution = true
                }

                // erected - Start - collects until the next `sectionhead`
                // <span class="sectionhead">Erected </span>
                if(tagName == "span" && attributes["class"] == "sectionhead") {
                    isCapturingText = true
                    isCapturingErectedTextPhase1 = true
                }

                // location - Start - collects until the next `sectionhead`
                // <span class="sectionhead">Location. </span>
                if(tagName == "span" && attributes["class"] == "sectionhead") {
                    isCapturingText = true
                    isCapturingLocationTextPhase1 = true
                }

            }
            .onText { text ->
                if(isCapturingText) {
                    val decodedString =
                        KsoupEntities
                            .decodeHtml(text)
                            .stripDoubleSpaces()
                            .stripDoublePeriodAndSpaces()
                    if (decodedString.isNotBlank())
                        rawMarkerInfoStrings += decodedString

                    // Title Text
                    if (isCapturingTitleText) {
                        mapMarkerResult = mapMarkerResult.copy(
                            title = decodedString
                        )
                        isCapturingText = false
                        isCapturingTitleText = false
                    }

                    // Inscription
                    if(isCapturingInscriptionText) {
                        mapMarkerResult = mapMarkerResult.copy(
                            inscription = decodedString
                        )
                        isCapturingText = false
                        isCapturingInscriptionText = false
                    }

                    // Photo Caption(s)
                    if(isCapturingPhotoCaption) {
                        mapMarkerResult = mapMarkerResult.copy(
                            photoCaptions = mapMarkerResult
                                .photoCaptions.plus(decodedString)
                        )
                        isCapturingText = false
                        isCapturingPhotoCaption = false
                    }

                    // Photo Attribution(s)
                    if(isCapturingPhotoAttribution) {
                        mapMarkerResult = mapMarkerResult.copy(
                            photoAttributions = mapMarkerResult
                                .photoAttributions.plus(decodedString)
                        )
                        isCapturingText = false
                        isCapturingPhotoAttribution = false
                    }

                    // Erected
                    if(isCapturingErectedTextPhase1) {
                        // keep collecting until the next `sectionhead` text is found (Topics and series.)
                        if(decodedString.contains("Topics and series.", ignoreCase = true)) {
                            isCapturingErectedTextPhase1 = false
                            isCapturingErectedTextPhase2 = false
                            isCapturingText = false
                            return@onText
                        }

                        // Start collecting the `erected` text after the "Erected" text is found : <span class="sectionhead">Erected </span>
                        if(decodedString.contains("Erected", ignoreCase = true)) {
                            isCapturingErectedTextPhase2 = true
                            return@onText
                        }

                        if(isCapturingErectedTextPhase2) {
                            mapMarkerResult = mapMarkerResult.copy(
                                erected = mapMarkerResult.erected + decodedString
                            )
                        }
                    }

                    // Location
                    if(isCapturingLocationTextPhase1) {
                        // keep collecting until the next `sectionhead` text is found (Other nearby markers.)
                        if(decodedString.contains("Other nearby markers.", ignoreCase = true)) {
                            isCapturingLocationTextPhase1 = false
                            isCapturingLocationTextPhase2 = false
                            isCapturingText = false
                            return@onText
                        }

                        // Start collecting the `location` text after the "Location" text is found : <span class="sectionhead">Location </span>
                        if(decodedString.contains("Location", ignoreCase = true)) {
                            isCapturingLocationTextPhase2 = true
                            return@onText
                        }

                        if(isCapturingLocationTextPhase2) {
                            mapMarkerResult = mapMarkerResult.copy(
                                location = mapMarkerResult.location + decodedString
                            )

                            // Strip the final string of "Touch for map." or "Touch for directions."
                            mapMarkerResult = mapMarkerResult.copy(
                                location = mapMarkerResult.location
                                    .stripString("Touch for map.")
                                    .stripString("Touch for directions.")
                            )
                        }
                    }
                }
            }
            .build()
    }

    val ksoupHtmlParser = KsoupHtmlParser(handler = markerInfoPageHandler())

    // Parse the html
    ksoupHtmlParser.write(rawPageHtml)
    ksoupHtmlParser.end()

//    // Parse the rawMarkerInfoStrings into a MapMarker
//    if (rawMarkerInfoStrings.size < 2) {
//        Logger.w { "rawMarkerInfoStrings.size < 2" }
//        return Pair("rawMarkerInfoStrings.size < 2", null)
//    }

    return Pair(null, mapMarkerResult)
}

fun String.stripDoubleSpaces(): String {
    return this.replace("  ", " ")
}

fun String.stripDoublePeriodAndSpaces(): String {
    return this.replace(". . ", ". ")
}

fun String.stripString(stringToStrip: String): String {
    return this.replace(stringToStrip, "")
}


fun fakeLoadingStateForParseMarkerInfoPageHtml(marker: MapMarker): LoadingState<MapMarker> {
    return LoadingState.Loaded(
        MapMarker(
            key = marker.key,
            position = marker.position,
            title = marker.title,
            alpha = marker.alpha,
            subtitle = marker.subtitle,

            markerInfoPageUrl = marker.markerInfoPageUrl,
            isDescriptionLoaded = true,
            mainPhotoUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1e/Stonehenge.jpg/640px-Stonehenge.jpg",
            markerPhotos = listOf(
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1e/Stonehenge.jpg/640px-Stonehenge.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1e/Stonehenge.jpg/640px-Stonehenge.jpg",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1e/Stonehenge.jpg/640px-Stonehenge.jpg"
            ),
            photoCaptions = listOf("Stonehenge"),
            photoAttributions = listOf("Attribution: XXX (CC BY-SA 4.0)"),
            inscription = "Inscription about Stonehenge",
            erected = "Erected 1913 from rubbled stones that were once erected around 2000 BC",
            credits = "Credits",
            location = ""
        ))
}
