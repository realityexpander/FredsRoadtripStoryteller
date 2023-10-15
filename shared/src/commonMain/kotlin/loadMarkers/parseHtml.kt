package loadMarkers

import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler.Default.onOpenTag
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import co.touchlab.kermit.Logger as Log

suspend fun parseMarkerPageHtml(htmlResponse: String): MarkersResult {
    if (htmlResponse.isBlank()) {
        Log.w { "htmlResponse is Blank" }
        return MarkersResult()
    }

    var isSingleMarkerPage = false

    // Processing Results
    var curCaptureMarkerId = ""
    val markerIdToRawMarkerInfoStringsMap = mutableMapOf<String, String>()
    var rawMarkerCountFromFirstPageHtmlOfMultiPageResult = 0
    var foundMarkerCount = 0
    val markerInfos = mutableMapOf<String, MarkerInfo>()

    // Simple scraper that checks if a page is only a single-marker page
    // - If it has a "span" with class "sectionhead", then it's a single-marker page.
    fun singleItemForPageChecker():  KsoupHtmlHandler {
        return KsoupHtmlHandler.Builder()
            .onOpenTag { tagName, attributes, _ ->
                if (tagName == "span" && attributes["class"] == "sectionhead") {
                    isSingleMarkerPage = true
                }
            }
            .build()
    }
    val singleItemForPageCheckerKsoupHtmlParser = KsoupHtmlParser(handler = singleItemForPageChecker())
    singleItemForPageCheckerKsoupHtmlParser.write(htmlResponse)
    singleItemForPageCheckerKsoupHtmlParser.end()

    fun singleMarkerPageHandler(): KsoupHtmlHandler {
        return KsoupHtmlHandler.Builder()
            .onOpenTag { tagName, attributes, isSelfClosing ->

                // Get the marker id
                if (tagName == "meta" && attributes["property"] == "og:url") {
                    val url = attributes["content"] ?: ""
                    val id = url.substringAfter("m=").toIntOrNull() ?: 0
                    curCaptureMarkerId = "M$id"
                    foundMarkerCount++ // should be max 1

                    if(foundMarkerCount > 1)
                        Log.e { "Found more than one marker on a single marker page. Found marker: $curCaptureMarkerId, count= $foundMarkerCount" }

                    // initialize the marker info for this marker
                    markerInfos[curCaptureMarkerId] = markerInfos[curCaptureMarkerId]?.copy(
                        id = curCaptureMarkerId,
                        infoPageUrl = url,
                    ) ?: MarkerInfo(
                        id = curCaptureMarkerId,
                        infoPageUrl = url,
                    )
                }

                // Get the title
                if (tagName == "meta" && attributes["property"] == "og:title") {
                    val title = attributes["content"] ?: ""

                    markerInfos[curCaptureMarkerId] = markerInfos[curCaptureMarkerId]?.copy(
                        title = title,
                        shortDescription = title
                    ) ?: MarkerInfo(
                        id = curCaptureMarkerId,
                        title = title,
                        shortDescription = title,
                    )
                }

                // Get the description
                if (tagName == "meta" && attributes["name"] == "description") {
                    val description = attributes["content"] ?: ""

                    markerInfos[curCaptureMarkerId] = markerInfos[curCaptureMarkerId]?.copy(
                        description = description,
                    ) ?: MarkerInfo(
                        id = curCaptureMarkerId,
                        description = description,
                    )
                }

                // Get the image url
                if (tagName == "meta" && attributes["name"] == "twitter:image") {
                    val imageUrl = attributes["content"] ?: ""

                    markerInfos[curCaptureMarkerId] = markerInfos[curCaptureMarkerId]?.copy(
                        imageUrl = imageUrl
                    ) ?: MarkerInfo(
                        id = curCaptureMarkerId,
                        imageUrl = imageUrl,
                    )
                }

                // Get the lat/long
                if (tagName == "a" && attributes["href"]?.contains(
                        "https://www.google.com/maps/dir/?api=1&destination="
                    ) == true
                ) {
                    val lat = attributes["href"]
                        ?.substringAfter("destination=")
                        ?.substringBefore(",")
                        ?.toDoubleOrNull() ?: run {
                        Log.w { "Failed to parse lat value for latlong link, marker id: $curCaptureMarkerId" }
                        return@onOpenTag
                    }
                    val long = attributes["href"]
                        ?.substringAfter(",")
                        ?.substringBefore(" ")
                        ?.toDoubleOrNull() ?: run {
                        Log.w { "Failed to parse long value for latlong link, marker id: $curCaptureMarkerId" }
                        return@onOpenTag
                    }

                    // Log.d { "Found an a lat long link, Lat long: $lat, $long")  }
                    markerInfos[curCaptureMarkerId] = markerInfos[curCaptureMarkerId]?.copy(
                        lat = lat,
                        long = long
                    ) ?: MarkerInfo(
                        id = curCaptureMarkerId,
                        lat = lat,
                        long = long,
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
                if (isCapturingMarkerText) {
                    val strippedBlankLines = text.trim()

                    if (strippedBlankLines.isNotEmpty())
                        markerIdToRawMarkerInfoStringsMap[curCaptureMarkerId] =
                            (markerIdToRawMarkerInfoStringsMap[curCaptureMarkerId]
                                ?: "") + KsoupEntities.decodeHtml(
                                strippedBlankLines
                            ) + "\n"
                }

                // First page only
                // Check for the entry count (239 entries matched your criteria. The first 100 are listed above.)
                if (text.contains("entries matched your criteria.", ignoreCase = true)) {
                    rawMarkerCountFromFirstPageHtmlOfMultiPageResult =
                        text.substringBefore("entries").trim().toIntOrNull() ?: 0
                    // Log.d { "Entry count: $entryCount" }
                }
            }
            .onOpenTag { tagName, attributes, _ ->

                // Find the "TheListItself" div, which contains the list of markers
                if (tagName == "div" && attributes["id"] == "TheListItself") {
                    isListItselfFound = true
                }
                if (!isListItselfFound) return@onOpenTag

                // Found a marker
                if (tagName == "table") {
                    if (attributes["id"]?.startsWith("M") == true) {
                        // Log.d { "Found a marker ${attributes["id"]}" }
                        curCaptureMarkerId = attributes["id"]!!
                        foundMarkerCount++
                    }
                }

                if (tagName == "td") {
                    isCapturingMarkerText = true
                }

                if (tagName == "a" && isCapturingMarkerText) {
                    if (attributes["href"]?.contains("https://www.google.com/maps/dir/?api=1&destination=") == true
                    ) {
                        val lat = attributes["href"]
                            ?.substringAfter("destination=")
                            ?.substringBefore(",")
                            ?.toDoubleOrNull() ?: run {
                            Log.w { "Failed to parse lat value for latlong link, marker id: $curCaptureMarkerId" }
                            return@onOpenTag
                        }
                        val long = attributes["href"]
                            ?.substringAfter(",")
                            ?.substringBefore(" ")
                            ?.toDoubleOrNull() ?: run {
                            Log.w { "Failed to parse long value for latlong link, marker id: $curCaptureMarkerId" }
                            return@onOpenTag
                        }

                        // Log.d { "Found an a lat long link, Lat long: $lat, $long")  }
                        markerInfos[curCaptureMarkerId] = markerInfos[curCaptureMarkerId]?.copy(
                            lat = lat,
                            long = long
                        ) ?: MarkerInfo(
                            id = curCaptureMarkerId,
                            lat = lat,
                            long = long,
                        )
                    }
                }
            }
            .onCloseTag { tagName, isSelfClosing ->
                if (tagName == "td" && isListItselfFound) {
                    capturePhase++
                    if (capturePhase == 2) {
                        capturePhase = 0
                        isCapturingMarkerText = false
                        // Log.d { "Captured marker: $curCaptureMarkerId")  }
                        // Log.d { "Captured text: ${markerToInfoStrings[curCaptureMarkerId]}")  }
                    }
                }
            }
            .onAttribute { tagName, attributeName, attributeValue ->
                if (tagName == "id" && attributeName == "TheListItself") {
                    isListItselfFound = true
                }

                // Found a marker
                if (tagName == "id" && isListItselfFound) {
                    if (attributeName.startsWith("M")) {
                        attributeName.substring(1).toIntOrNull()?.let { markerId ->
                            // Log.d { "Found Marker id: $markerId")  }
                        }
                    }
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
    ksoupHtmlParser.write(htmlResponse)
    ksoupHtmlParser.end()

    if (isSingleMarkerPage) {
        // For a page with a single marker, just add a dummy entry to the markerIdToRawMarkerInfoStrings map to make it process like a multi-marker page.
        markerIdToRawMarkerInfoStringsMap[curCaptureMarkerId] = "A single marker page."
    } else {
        // For a page with multiple markers, process the raw extracted strings into each `MarkerInfo` object
        markerIdToRawMarkerInfoStringsMap.forEach { (markerId, infoString) ->
            // break info string into lines
            val lines = infoString.split("\n")

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
                lines.subList(indexOfFirstDashLine + 1, indexOfSecondDashLine).joinToString("\n")
            } else {
                lines[5]  // default to one Line Description
            }

            val lat = markerInfos[markerId]?.lat ?: run {
                Log.d { "Failed to find lat value for marker id: $markerId" }
                return@forEach
            }
            val long = markerInfos[markerId]?.long ?: run {
                Log.d { "Failed to find long value for marker id: $markerId" }
                return@forEach
            }

            markerInfos[markerId] = MarkerInfo(
                id = markerId,
                title = title,
                description = description,
                shortDescription = shortLocation,
                lat = lat,
                long = long,
            )
        }
    }

    // How many markers are there at this location?
    val totalMarkersAtLocation =
        if(foundMarkerCount > 0 && rawMarkerCountFromFirstPageHtmlOfMultiPageResult == 0)
            foundMarkerCount  // only one page of markers.
        else
            rawMarkerCountFromFirstPageHtmlOfMultiPageResult // more than one page of markers.

    return MarkersResult(
        markerIdToRawMarkerInfoStringsMap, // used for multi-page processing.
        markerInfos,
        totalMarkersAtLocation
    )
}
