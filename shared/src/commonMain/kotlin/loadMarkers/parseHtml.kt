package loadMarkers

import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import co.touchlab.kermit.Logger as Log

suspend fun parseHtml(htmlResponse: String): MarkersResult {
    if (htmlResponse.isBlank()) {
        Log.w { "htmlResponse is Blank" }
        return MarkersResult()
    }

    var isListItselfFound = false

    var isCapturingMarkerText = false
    var curCaptureMarkerId = ""
    var capturePhase = 0  // 1 == marker title, 2 == text

    val markerIdToRawMarkerInfoStringsMap = mutableMapOf<String, String>()
    var rawMarkerCountFromHtmlMultiPageDocument = 0
    var foundMarkerCount = 0
    var totalMarkersAtLocation = 0

    val markerInfos = mutableMapOf<String, MarkerInfo>()

    // Create the scraper callbacks for the parser
    val handler = KsoupHtmlHandler
        .Builder()
        .onText { text ->
            if (isCapturingMarkerText) {
                val strippedBlankLines = text.trim()

                if (strippedBlankLines.isNotEmpty())
                    markerIdToRawMarkerInfoStringsMap[curCaptureMarkerId] =
                        (markerIdToRawMarkerInfoStringsMap[curCaptureMarkerId] ?: "") + KsoupEntities.decodeHtml(
                            strippedBlankLines
                        ) + "\n"
            }

            // First page only
            // Check for the entry count (239 entries matched your criteria. The first 100 are listed above.)
            if (text.contains("entries matched your criteria.", ignoreCase = true)) {
                rawMarkerCountFromHtmlMultiPageDocument = text.substringBefore("entries").trim().toIntOrNull() ?: 0
                // Log.d { "Entry count: $entryCount" }
            }
        }
        .onOpenTag { tagName, attributes, isSelfClosing ->

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
                        Log.d { "Failed to parse lat value for latlong link, marker id: $curCaptureMarkerId" }
                        return@onOpenTag
                    }
                    val long = attributes["href"]
                        ?.substringAfter(",")
                        ?.substringBefore(" ")
                        ?.toDoubleOrNull() ?: run {
                        Log.d { "Failed to parse long value for latlong link, marker id: $curCaptureMarkerId" }
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

    // Create parser & pass the HTML to it
    val ksoupHtmlParser = KsoupHtmlParser(handler = handler)
    ksoupHtmlParser.write(htmlResponse)
    ksoupHtmlParser.end()

    // Process the raw extracted strings into `MarkerInfo` objects
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
        val description = if(indexOfSecondDashLine > indexOfFirstDashLine) {
            lines.subList(indexOfFirstDashLine+1, indexOfSecondDashLine).joinToString("\n")
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

    // How many markers are there at this location?
    if(foundMarkerCount > 0 && rawMarkerCountFromHtmlMultiPageDocument == 0)
        totalMarkersAtLocation = foundMarkerCount  // only one page of markers.
    else
        totalMarkersAtLocation = rawMarkerCountFromHtmlMultiPageDocument // more than one page of markers.

    return MarkersResult(
        markerIdToRawMarkerInfoStringsMap,
        markerInfos,
        totalMarkersAtLocation
    )
}
