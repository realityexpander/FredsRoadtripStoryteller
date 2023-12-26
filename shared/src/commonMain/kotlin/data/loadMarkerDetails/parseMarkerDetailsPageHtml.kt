package data.loadMarkerDetails

import co.touchlab.kermit.Logger
import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import data.loadMarkers.kBaseHmdbDotOrgUrl
import data.util.LoadingState
import presentation.maps.Marker

fun parseMarkerDetailsPageHtml(rawPageHtml: String): Pair<String?, Marker?> {

    var markerResult = Marker()
    val rawMarkerInfoStrings = mutableListOf<String>()
    val spanishInscriptionLines = mutableListOf<String>()
    val englishInscriptionLines = mutableListOf<String>()

    if (rawPageHtml.isBlank()) {
        Logger.w { "htmlResponse is Blank" }
        return Pair("htmlResponse is Blank", null)
    }

    fun markerDetailsPageHandler(): KsoupHtmlHandler {
        var isCapturingText = false
        var isCapturingTitleText = false
        var isCapturingSubtitleText = false

        var isCapturingDefaultInscriptionText = false
        var didCaptureDefaultInscriptionText = false

        var isCapturingPhotoCaption = false
        var isCapturingPhotoAttribution = false

        var isCapturingErectedTextPhase1 = false
        var isCapturingErectedTextPhase2 = false
        var isCapturingErectedTextComplete = false

        var isCapturingLocationTextPhase1 = false
        var isCapturingLocationTextPhase2 = false
        var isCapturingLocationTextComplete = false

        var isCapturingSpanishInscription = false

        var isCapturingEnglishInscription = false
        var isCapturingEnglishInscriptionPaused = false  // skip ad copy/links/scripts in the middle of the english inscription

        return KsoupHtmlHandler.Builder()
            .onOpenTag { tagName, attributes, _ ->
                // title - Start
                // <h1>Almadén Vineyards</h1>
                if(tagName == "h1") {
                    isCapturingText = true
                    isCapturingTitleText = true
                }

                // Subtitle - Start
                // <h2>State Historic Landmark #505</h2>
                if(tagName == "h2") {
                    isCapturingText = true
                    isCapturingSubtitleText = true
                }

                // inscription - Start (for english-only pages)
                // <div id="inscription1" style="display:none;">Almadén Vineyards. . On this site in 1852 Charles LeFranc made the first commercial planting of fine European wine grapes in Santa Clara County and founded Almadén Vineyards. LeFranc imported cuttings from vines in the celebrated wine districts of his native France, shipping them around the Horn by sail. . This historical marker  was erected  in 1953 by California State Parks Commission. It  is in South San Jose  in Santa Clara County California</div>
                if(tagName == "div"
                    && attributes["id"] == "inscription1"
                    && !didCaptureDefaultInscriptionText
                ) {
                    isCapturingText = true
                    isCapturingDefaultInscriptionText = true
                }
                if(isCapturingDefaultInscriptionText
                    && !didCaptureDefaultInscriptionText
                ) {
                    // Stop collecting inscription upon encountering "script" tag.
                    // - (because inscription can have "bold" and "italic" html tags)
                    if (tagName =="script") {
                        isCapturingDefaultInscriptionText = false
                        didCaptureDefaultInscriptionText = true
                        isCapturingText = false
                    }
                }

                // Multi-language Inscription - START
                // <lang=es>
                if(tagName == "lang=es") {
                    isCapturingText = true
                    isCapturingSpanishInscription = true // spanish starts first, will be set to false when "English translation:" is found
                    isCapturingEnglishInscription = false
                }

                // Multi-language Inscription - PAUSE text collection for ad copy
                // <fieldset id="incopyAd" class="adleft"><legend class="adtitle">Paid Advertisement</legend>
                if(tagName == "fieldset" && attributes["id"] == "incopyAd") {
                    isCapturingEnglishInscriptionPaused = true
                }

                // Multi-language Inscription - END
                // <span class="sectionhead">Topics.</span>
                if(isCapturingText && tagName == "span" && attributes["class"] == "sectionhead") {
                    isCapturingText = false
                    isCapturingSpanishInscription = false
                    isCapturingEnglishInscription = false
                }

                // markerPhotoUrl & additionalPhotos - (self closing tag)
                // <img loading="lazy" class="photoimage" src="Photos/7/Photo7252.jpg?11252005" style="max-height:300px;" alt="State Historic Landmark 505 image. Click for full size." title="State Historic Landmark 505. Click for full size.">
                if(tagName == "img" && attributes["class"] == "photoimage") {
                    val markerPhotoUrl = kBaseHmdbDotOrgUrl + (attributes["src"] ?: "")

                    // Set first photo as the main photo
                    if(markerResult.mainPhotoUrl.isBlank()) {
                        markerResult = markerResult.copy(
                            mainPhotoUrl = markerPhotoUrl
                        )
                    }

                    // Add all photos to markerPhotos list
                    markerResult =
                        markerResult.copy(
                            markerPhotos = markerResult.markerPhotos.plus(markerPhotoUrl)
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

                // erected - Start - collects until the next `sectionhead` - checked in onText
                // <span class="sectionhead">Erected </span>
                if(!isCapturingErectedTextComplete) {
                    if (tagName == "span" && attributes["class"] == "sectionhead") {
                        if (!isCapturingErectedTextPhase2) {
                            isCapturingText = true
                            isCapturingErectedTextPhase1 = true // start collecting
                        } else {
                            isCapturingErectedTextPhase2 = false // end collecting
                            isCapturingErectedTextComplete = true
                        }
                    }
                }

                // location - Start - collects until the next `sectionhead` - checked in onText
                // <span class="sectionhead">Location. </span>
                if(!isCapturingLocationTextComplete) {
                    if (tagName == "span" && attributes["class"] == "sectionhead") {
                        if (!isCapturingLocationTextPhase2) {
                            isCapturingText = true
                            isCapturingLocationTextPhase1 = true // start collecting
                        } else {
                            isCapturingLocationTextPhase2 = false // end collecting
                            isCapturingLocationTextComplete = true
                        }
                    }
                }

            }
            .onCloseTag { tagName, isSelfClosing ->
                // Multi-language Inscription - Unpause for ads
                // </div>  // from id="QRCode"
                if(isCapturingEnglishInscriptionPaused) {
                    if (tagName == "center") {
                        isCapturingEnglishInscriptionPaused = false
                    }
                }
            }
            .onText { text ->
                if(isCapturingText) {
                    val decodedString =
                        KsoupEntities
                            .decodeHtml(text)
                            .stripDoubleSpaceCommaDoubleSpace()
                            .stripDoubleSpace() + " " // require space at end to conform to text-formatting from site
                    if (decodedString.isBlank()) return@onText

                    rawMarkerInfoStrings += decodedString

                    // Title Text
                    if (isCapturingTitleText) {
                        markerResult = markerResult.copy(
                            title = decodedString
                        )
                        isCapturingText = false
                        isCapturingTitleText = false
                    }

                    // Subtitle Text
                    if (isCapturingSubtitleText) {
                        markerResult = markerResult.copy(
                            subtitle = decodedString
                        )
                        isCapturingText = false
                        isCapturingSubtitleText = false
                    }

                    // Inscription (english-only text) (if multi-language, will be literal string: "English translation:" to indicate use the multi-language text)
                    if(isCapturingDefaultInscriptionText) {
                        markerResult = markerResult.copy(
                            inscription = markerResult.inscription + decodedString
                        )
                    }

                    // Spanish Inscription (multi-language text)
                    if(isCapturingSpanishInscription) {
                        if(decodedString.contains("English translation:", ignoreCase = true)) {
                            isCapturingSpanishInscription = false
                            isCapturingEnglishInscription = true
                            return@onText
                        }

                        spanishInscriptionLines += decodedString
                    }

                    // English Inscription (multi-language text)
                    if(isCapturingEnglishInscription && !isCapturingEnglishInscriptionPaused) {
                        englishInscriptionLines += decodedString
                    }

                    // Photo Caption(s)
                    if(isCapturingPhotoCaption) {
                        markerResult = markerResult.copy(
                            photoCaptions = markerResult
                                .photoCaptions.plus(decodedString.trim())
                        )
                        isCapturingText = false
                        isCapturingPhotoCaption = false
                    }

                    // Photo Attribution(s)
                    if(isCapturingPhotoAttribution) {
                        markerResult = markerResult.copy(
                            photoAttributions = markerResult
                                .photoAttributions.plus(decodedString.trim())
                        )
                        isCapturingText = false
                        isCapturingPhotoAttribution = false
                    }

                    // Erected - Collect & End collecting
                    if(isCapturingErectedTextPhase1) {
                        // keep collecting until the next `sectionhead` text is found (Topics and series.)
                        if(decodedString.contains("Topics and series.", ignoreCase = true)) {
                            isCapturingErectedTextPhase1 = false
                            isCapturingErectedTextPhase2 = false
                            isCapturingErectedTextComplete = true
                            return@onText
                        }

                        // Start collecting the `erected` text after the "Erected" html text is found
                        // <span class="sectionhead">Erected </span>
                        if(decodedString.startsWith("Erected", ignoreCase = true)) {
                            isCapturingErectedTextPhase1 = false
                            isCapturingErectedTextPhase2 = true
                            return@onText
                        }

                    }
                    if(isCapturingErectedTextPhase2) {
                        markerResult = markerResult.copy(
                            erected = markerResult.erected + decodedString.trim()
                        )
                        return@onText
                    }

                    // Location - Collect & End collecting
                    if(isCapturingLocationTextPhase1) {
                        // keep collecting until the next `sectionhead` text is found (Other nearby markers.)
                        if(decodedString.contains("Other nearby markers.", ignoreCase = true)) { isCapturingLocationTextPhase1 = false
                            isCapturingLocationTextPhase2 = false
                            isCapturingLocationTextComplete = true
                            return@onText
                        }

                        // Start collecting the `location` text after the "Location" text is found
                        // <span class="sectionhead">Location </span>
                        if(decodedString.startsWith("Location", ignoreCase = true)) {
                            isCapturingLocationTextPhase1 = false
                            isCapturingLocationTextPhase2 = true
                            return@onText
                        }

                    }
                    if(isCapturingLocationTextPhase2) {
                        markerResult = markerResult.copy(
                            location = markerResult.location + decodedString
                        )

                        // Strip the final string of "Touch for map." or "Touch for directions."
                        markerResult = markerResult.copy(
                            location = markerResult.location
                                .stripString("Touch for map")
                                .stripString("Touch for directions")
                                .stripString(" . ")
                        )
                        return@onText
                    }

                }
            }
            .build()
    }

    val ksoupHtmlParser = KsoupHtmlParser(handler = markerDetailsPageHandler())

    // Parse the html
    ksoupHtmlParser.write(rawPageHtml)
    ksoupHtmlParser.end()

    // • Text cleanup

    // Multi-language Inscription - Process the collected lines, if any
    val finalEnglishInscription =
        if(englishInscriptionLines.size > 0
           || markerResult.inscription.contains("English translation", ignoreCase=true)
        ) {
            englishInscriptionLines
                .subList(1, englishInscriptionLines.size) // skip the first line of the inscription (it's the title)
                .joinToString("")
        } else {
            ""
        }
    val finalSpanishInscription =
        if(spanishInscriptionLines.size > 0
            || markerResult.inscription.contains("English translation", ignoreCase=true)
        ) {
            spanishInscriptionLines
                .subList(1, spanishInscriptionLines.size) // skip the first line of the inscription (it's the title)
                .joinToString("")
        } else {
            ""
        }

    // Prepare final result
    markerResult = markerResult.copy(
        id = rawPageHtml.findFirstMarkerID(),
        inscription = markerResult.inscription.processInscriptionString(),
        englishInscription = finalEnglishInscription.processInscriptionString(),
        spanishInscription = finalSpanishInscription.processInscriptionString(),
        location = markerResult.location.stripString("Touch for map"),
        isDetailsLoaded = true
    )

    return Pair(null, markerResult)
}

fun String.stripDoubleSpace(): String {
    return this.replace("  ", " ")
}
fun String.stripDoublePeriodAndSpace(): String {
    return this.replace(". . ", ". ")
}
fun String.stripString(stringToStrip: String): String {
    return this.replace(stringToStrip, "")
}
fun String.ensureSpaceAfterPeriod(): String {
    return this.replace(". ", ".")
        .replace(".", ". ")
}
fun String.stripNewlines(): String {
    return this.replace("\n", "")
}
fun String.stripTripleSpace(): String {
    return this.replace("   ", " ")
}
fun String.stripDoubleSpaceCommaDoubleSpace(): String {
    return this.replace("  ,  ", "")
}
fun String.stripCommaDoubleSpace(): String {
    return this.replace(" ,  ", "")
}
fun String.processInscriptionString(): String {
    return this
        .stripNewlines()
        .stripString("Touch for map")
        .stripString("Touch for directions")
        .stripDoubleSpace()
        .stripDoublePeriodAndSpace()
        .ensureSpaceAfterPeriod()
        .stripTripleSpace()
        .stripDoubleSpace()
        .stripCommaDoubleSpace()
        .stripString(" . ")
        .stripString(",  ")
        .stripString(" ,")
        .trim()
}
// Finds the first instance of "MarkerID=1234" and returns "M1234"
fun String.findFirstMarkerID(): String {
    val regex = Regex("""(?<=MarkerID=)\d+""")
    val matchResult = regex.find(this)
    return "M" + matchResult?.value
}

fun fakeLoadingStateForMarkerDetailsPageHtml(marker: Marker): LoadingState<Marker> {
    return LoadingState.Loaded(
        Marker(
            id = marker.id,
            position = marker.position,
            title = marker.title,
            alpha = marker.alpha,
            subtitle = marker.subtitle,

            markerDetailsPageUrl = marker.markerDetailsPageUrl,
            isDetailsLoaded = true,
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
        )
    )
}
