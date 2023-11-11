package data.loadMarkers

import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import presentation.maps.LatLong
import presentation.maps.Marker
import presentation.maps.MarkerIdStr
import co.touchlab.kermit.Logger as Log

const val kBaseHmdbDotOrgUrl = "https://www.hmdb.org/"

fun parseMarkersPageHtml(rawPageHtml: String): LoadMarkersResult {
    if (rawPageHtml.isBlank()) {
        Log.w { "htmlResponse is Blank" }
        return LoadMarkersResult()
    }

    var isSingleMarkerPage = false

    // Processing Results
    var rawMarkerCountFromFirstPageHtmlOfMultiPageResult = 0
    var curCapturingMarkerId = ""
    var foundMarkerCount = 0
    val markerIdToRawMarkerDetailStringMap = mutableMapOf<MarkerIdStr, String>()  // Strings are the text strings, separated by newlines (maybe should use a list next time for clarity)
    val markerIdToMarker = mutableMapOf<MarkerIdStr, Marker>()

    // Simple scraper that checks if a page is only a single-marker page
    // - If it has a "span" with class "sectionhead", then it's a single-marker page.
    fun checkSingleItemOnPageHandler():  KsoupHtmlHandler {
        return KsoupHtmlHandler.Builder()
            .onOpenTag { tagName, attributes, _ ->
                if (tagName == "span" && attributes["class"] == "sectionhead") {
                    isSingleMarkerPage = true
                }
            }
            .build()
    }
    val checkSingleItemOnPageKsoupHtmlParser = KsoupHtmlParser(handler = checkSingleItemOnPageHandler())
    checkSingleItemOnPageKsoupHtmlParser.write(rawPageHtml)
    checkSingleItemOnPageKsoupHtmlParser.end()

    fun singleMarkerPageHandler(): KsoupHtmlHandler {
        return KsoupHtmlHandler.Builder()
            .onOpenTag { tagName, attributes, _ ->

                // Get the marker id
                if(tagName == "meta" && attributes["property"] == "og:url") {
                    val url = attributes["content"] ?: ""
                    val id = url.substringAfter("m=").toIntOrNull() ?: 0
                    curCapturingMarkerId = "M$id"
                    foundMarkerCount++ // should be max 1

                    if(foundMarkerCount > 1)
                        Log.e { "Found more than one marker on a single marker page. Found marker: $curCapturingMarkerId, count= $foundMarkerCount" }

                    // initialize the marker info for this marker
                    markerIdToMarker[curCapturingMarkerId] = markerIdToMarker[curCapturingMarkerId]?.copy(
                        id = curCapturingMarkerId,
                        markerDetailsPageUrl = url,
                    ) ?: Marker(
                        id = curCapturingMarkerId,
                        markerDetailsPageUrl = url,
                    )
                }

                // Get the title
                if(tagName == "meta" && attributes["property"] == "og:title") {
                    val title = attributes["content"] ?: ""

                    markerIdToMarker[curCapturingMarkerId] = markerIdToMarker[curCapturingMarkerId]?.copy(
                        title = title,
                    ) ?: Marker(
                        id = curCapturingMarkerId,
                        title = title,
                    )
                }

                // Get the description
                if(tagName == "meta" && attributes["name"] == "description") {
                    val description = attributes["content"] ?: ""

                    markerIdToMarker[curCapturingMarkerId] = markerIdToMarker[curCapturingMarkerId]?.copy(
                        subtitle = description,
                    ) ?: Marker(
                        id = curCapturingMarkerId,
                        subtitle = description,
                    )
                }

                // Get the main image url
                if(tagName == "meta" && attributes["name"] == "twitter:image") {
                    val imageUrl = attributes["content"] ?: ""

                    markerIdToMarker[curCapturingMarkerId] = markerIdToMarker[curCapturingMarkerId]?.copy(
                        mainPhotoUrl = imageUrl
                    ) ?: Marker(
                        id = curCapturingMarkerId,
                        mainPhotoUrl = imageUrl,
                    )
                }

                // Get the lat/long
                if(tagName == "a" && attributes["href"]?.contains(
                        "https://www.google.com/maps/dir/?api=1&destination="
                    ) == true
                ) {
                    val lat = attributes["href"]
                        ?.substringAfter("destination=")
                        ?.substringBefore(",")
                        ?.toDoubleOrNull() ?: run {
                        Log.w { "Failed to parse lat value for latlong link, marker id: $curCapturingMarkerId" }
                        return@onOpenTag
                    }
                    val long = attributes["href"]
                        ?.substringAfter(",")
                        ?.substringBefore(" ")
                        ?.toDoubleOrNull() ?: run {
                        Log.w { "Failed to parse long value for latlong link, marker id: $curCapturingMarkerId" }
                        return@onOpenTag
                    }

                    // Log.d { "Found an a lat long link, Lat long: $lat, $long")  }
                    markerIdToMarker[curCapturingMarkerId] = markerIdToMarker[curCapturingMarkerId]?.copy(
                        position = LatLong(lat, long)
                    ) ?: Marker(
                        id = curCapturingMarkerId,
                        position = LatLong(lat, long)
                    )
                }

                // Get the Marker Info page url
                // <a href="../m.asp?m=218883">Skinner's Seedling</a> -> https://www.hmdb.org/m.asp?m=218883
                if(tagName == "a" && attributes["href"] == "../m.asp?m=") {
                    val url = attributes["href"] ?: ""

                    // replace the ".." in the url with the full url
                    val fullUrl =
                        kBaseHmdbDotOrgUrl +
                        url.substringAfterLast("/")  // m.asp?m=218883

                    markerIdToMarker[curCapturingMarkerId] = markerIdToMarker[curCapturingMarkerId]?.copy(
                        markerDetailsPageUrl = fullUrl,
                    ) ?: Marker(
                        id = curCapturingMarkerId,
                        markerDetailsPageUrl = fullUrl,
                    )
                }
            }
            .build()
    }

    fun moreThanOneMarkerPageHandler(): KsoupHtmlHandler {
        // Multi-page state machine flags
        var isListItselfFound = false
        var isCapturingMarkerText = false
        var capturePhase = 0  // 1 == marker title, 2 == text

        return KsoupHtmlHandler.Builder()
            .onText { text ->
                if(isCapturingMarkerText) {
                    val strippedBlankLines = text.trim()

                    if (strippedBlankLines.isNotEmpty())
                        markerIdToRawMarkerDetailStringMap[curCapturingMarkerId] =
                            (markerIdToRawMarkerDetailStringMap[curCapturingMarkerId]
                                ?: "") + KsoupEntities.decodeHtml(
                                strippedBlankLines
                            ) + "\n"
                }

                // Check for the entry count (239 entries matched your criteria. The first 100 are listed above.)
                // - This is for First page only
                if(text.contains("entries matched your criteria.", ignoreCase = true)) {
                    rawMarkerCountFromFirstPageHtmlOfMultiPageResult =
                        text.substringBefore("entries").trim().toIntOrNull() ?: 0
                    // Log.d { "Entry count: $entryCount" }
                }
            }
            .onOpenTag { tagName, attributes, _ ->

                // Find the "TheListItself" div, which contains the list of markers
                if(tagName == "div" && attributes["id"] == "TheListItself") {
                    isListItselfFound = true
                }
                if(!isListItselfFound) return@onOpenTag

                // Found a marker
                if(tagName == "table") {
                    if (attributes["id"]?.startsWith("M") == true) {
                        // Log.d { "Found a marker ${attributes["id"]}" }
                        curCapturingMarkerId = attributes["id"]!!
                        foundMarkerCount++
                    }
                }

                if(tagName == "td") {
                    isCapturingMarkerText = true
                }

                if(tagName == "a" && isCapturingMarkerText) {
                    if (attributes["href"]?.contains("https://www.google.com/maps/dir/?api=1&destination=") == true
                    ) {
                        val lat = attributes["href"]
                            ?.substringAfter("destination=")
                            ?.substringBefore(",")
                            ?.toDoubleOrNull() ?: run {
                            Log.w { "Failed to parse lat value for latlong link, marker id: $curCapturingMarkerId" }
                            return@onOpenTag
                        }
                        val long = attributes["href"]
                            ?.substringAfter(",")
                            ?.substringBefore(" ")
                            ?.toDoubleOrNull() ?: run {
                            Log.w { "Failed to parse long value for latlong link, marker id: $curCapturingMarkerId" }
                            return@onOpenTag
                        }

                        // Log.d { "Found an a lat long link, Lat long: $lat, $long")  }
                        markerIdToMarker[curCapturingMarkerId] = markerIdToMarker[curCapturingMarkerId]?.copy(
                            position = LatLong(lat, long)
                        ) ?: Marker(
                            id = curCapturingMarkerId,
                            position = LatLong(lat, long)
                        )
                    }
                }
            }
            .onCloseTag { tagName, _ ->
                if(tagName == "td" && isListItselfFound) {
                    capturePhase++
                    if (capturePhase == 2) {
                        capturePhase = 0
                        isCapturingMarkerText = false
                        // Log.d { "Captured marker: $curCaptureMarkerId")  }
                        // Log.d { "Captured text: ${markerToInfoStrings[curCaptureMarkerId]}")  }
                    }
                }
            }
            .onAttribute { tagName, attributeName, _ ->
                if(tagName == "id" && attributeName == "TheListItself") {
                    isListItselfFound = true
                }

                // Found a marker
                if(tagName == "id" && isListItselfFound) {
                    //if (attributeName.startsWith("M")) {
                    //    attributeName.substring(1).toIntOrNull()?.let { markerId ->
                    //        // Log.d { "Found Marker id: $markerId")  }
                    //    }
                    //}
                }
            }
            .build()
    }

    // Pick the correct handler
    val ksoupHtmlParser =
        if(isSingleMarkerPage)
            KsoupHtmlParser(handler = singleMarkerPageHandler())
        else
            KsoupHtmlParser(handler = moreThanOneMarkerPageHandler())

    // Parse the html
    ksoupHtmlParser.write(rawPageHtml)
    ksoupHtmlParser.end()

    if (isSingleMarkerPage) {
        // For a page with a single marker, just add a dummy entry to the markerIdToRawMarkerInfoStrings map
        // to make it process same as a multi-marker page.
        markerIdToRawMarkerDetailStringMap[curCapturingMarkerId] = "A single marker page."
    } else {
        // For a page with multiple markers, process the raw extracted strings into each `MarkerInfo` object
        markerIdToRawMarkerDetailStringMap.forEach { (markerId, detailString) ->
            // break info string into lines
            val lines = detailString.split("\n")

            val shortLocation = lines[2]
            val title = lines[3]

            // val oneLineDescription = lines[5]

            // Collect lines between first and second dashes
            var indexOfFirstDashLine = 0
            var indexOfSecondDashLine = 0
            lines.forEachIndexed { i, line ->
                if (line == "—") {
                    if (indexOfFirstDashLine == 0)  // first dash
                        indexOfFirstDashLine = i
                    else if (indexOfSecondDashLine == 0)  // second dash
                        indexOfSecondDashLine = i
                }
            }
            val description = if (indexOfSecondDashLine > indexOfFirstDashLine) {
                lines
                    .subList(indexOfFirstDashLine + 1, indexOfSecondDashLine)
                    .joinToString("\n")
            } else {
                lines[5]  // default to one Line Description
            }

            val lat = markerIdToMarker[markerId]?.position?.latitude ?: run {
                Log.d { "Failed to find lat value for marker id: $markerId" }
                return@forEach
            }
            val long = markerIdToMarker[markerId]?.position?.longitude ?: run {
                Log.d { "Failed to find long value for marker id: $markerId" }
                return@forEach
            }

            markerIdToMarker[markerId] = Marker(
                id = markerId,
                title = title,
                subtitle = shortLocation.stripEmDash(),
                position = LatLong(lat, long),
                markerDetailsPageUrl = markerIdToMarker[markerId]?.markerDetailsPageUrl ?: "",
            )
        }
    }

    // How many markers are there at this location?
    val totalMarkersAtLocation =
        if(foundMarkerCount > 0 && rawMarkerCountFromFirstPageHtmlOfMultiPageResult == 0)
            foundMarkerCount  // only one page of markers.
        else
            rawMarkerCountFromFirstPageHtmlOfMultiPageResult // more than one page of markers.

    return LoadMarkersResult(
        markerIdToRawMarkerDetailStringMap, // used for multi-page processing.
        markerIdToMarker,
        totalMarkersAtLocation
    )
}

fun String.stripEmDash(): String {
    return this.replace("—", "")
}
