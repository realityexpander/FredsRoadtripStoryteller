import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import sampleData.fullHtmlSamplePage1
import sampleData.fullHtmlSamplePage2
import sampleData.fullHtmlSamplePage3

//val request = Request.Builder()
//    .url("https://www.hmdb.org/results.asp?Search=Coord&Latitude=37.422160&Longitude=-122.084270&Miles=10&MilesType=1&HistMark=Y&WarMem=Y&FilterNOT=&FilterTown=&FilterCounty=&FilterState=&FilterCountry=&FilterCategory=0&Page=1")
//    .build()


@Composable
fun loadMarkersFromHtml(): MutableState<ParseDataResult> {
    var pageNum by remember { mutableStateOf(1)}
//    val loadingState by load<String>("https://www.hmdb.org/results.asp?Search=Coord&Latitude=37.422160&Longitude=-122.084270&Miles=10&MilesType=1&HistMark=Y&WarMem=Y&FilterNOT=&FilterTown=&FilterCounty=&FilterState=&FilterCountry=&FilterCategory=0&Page=$pageNum")
    var loadingState by remember {
        mutableStateOf(generateFakeData(pageNum))
    }
    val parsedState = remember { mutableStateOf(ParseDataResult()) }
    val coroutineScope = rememberCoroutineScope()

    when (val state = loadingState) {
        is LoadingState.Loading -> {
            Text("Loading...")
        }
        is LoadingState.Loaded<String> -> {
            Text(
                fontSize = 11.sp,
                text = "Loaded: ${parsedState.value.markerToInfoStrings.size} / ${parsedState.value.totalEntriesCount} entries\n" +
                        "Data: ${state.data.length} chars"
            )

//            SideEffect {  // test
//                parsedState.value = parseHtml(state.data)
//            }
        }
        is LoadingState.Error -> {
            Text("Error: ${state.message}")
        }
    }

    LaunchedEffect(loadingState) {
        if (loadingState !is LoadingState.Loaded<String>)
            return@LaunchedEffect


        delay(50) // prevent race condition with the loadingState being set to Loading again before the parseHtml() call below?
        withContext(Dispatchers.Default) {
            val data = (loadingState as LoadingState.Loaded<String>).data
            if (data.isBlank()) {
                // println("Blank data")
                return@withContext
            }
            val parseResult = parseHtml(data)

            if (parseResult.totalEntriesCount == 0) {
                // println("No entries found")
                return@withContext
            }

            // Process the Strings in markerToInfoStrings into MarkerInfo objects
            if (pageNum == 1) {
                parsedState.value = parseResult
            } else {
                parsedState.value = parsedState.value.copy(
                    markerToInfoStrings = (parsedState.value.markerToInfoStrings + parseResult.markerToInfoStrings).toMutableMap(),
                    markerInfos = (parsedState.value.markerInfos + parseResult.markerInfos).toMap(),
                )
            }
            // println("Parsed Entry count with Drivable Map Location: ${parsedState.value.markerInfos.size}")
            // println("Parsed markerToInfoStrings count: ${parsedState.value.markerToInfoStrings.size}")

            // Check if we need to load more pages
            if (parsedState.value.markerToInfoStrings.size < parsedState.value.totalEntriesCount) { // base size the check on the number of markerToInfoStrings, not the markerInfos because some of the markers may have been rejected.
                loadingState = LoadingState.Loading()
                yield()
                pageNum++  // trigger the next page load
                yield()
            } else {
                parsedState.value = parsedState.value.copy(isFinished = true)
            }
        }
    }

    LaunchedEffect(pageNum) {
        withContext(Dispatchers.Default) {
            if (parsedState.value.isFinished) {
                return@withContext
            }

            // Check if we need to load more pages
            if (parsedState.value.markerToInfoStrings.size < parsedState.value.totalEntriesCount) {
                // println("Loading page $pageNum")
                loadingState = LoadingState.Loading()
                loadingState = generateFakeData(pageNum)
            }
        }
    }

    return parsedState
}

fun generateFakeData(pageNum: Int) : LoadingState<String> {
    return LoadingState.Loaded(
        when(pageNum) {
            1 -> fullHtmlSamplePage1()
            2 -> fullHtmlSamplePage2()
            3 -> fullHtmlSamplePage3()
            else -> ""
        }
    )
}

data class MarkerInfo(
    val id: String,
    val title: String = "",
    val description: String = "",
    val shortDescription: String = "",
    val lat: Double = 0.0,
    val long: Double = 0.0,
)

data class ParseDataResult(
    val markerToInfoStrings: MutableMap<String, String> = mutableMapOf(),
    val totalEntriesCount: Int = 0,
    val markerInfos: Map<String, MarkerInfo> = mutableMapOf(),
    val isFinished: Boolean = false
)

suspend fun parseHtml(htmlResponse: String): ParseDataResult {
    if(htmlResponse.isBlank()) {
        // println("Blank data")
        return ParseDataResult()
    }

    var isListItselfFound = false

    var isCapturingMarkerText = false
    var curCaptureMarkerId = ""
    var capturePhase = 0  // 1 == marker title, 2 == text

    val markerToInfoStrings = mutableMapOf<String, String>()
    var entryCount = 0

    val markerInfos = mutableMapOf<String, MarkerInfo>()

    // Create the scraper callbacks for the parser
    val handler = KsoupHtmlHandler
        .Builder()
        .onText { text ->
            if(isCapturingMarkerText) {
                val strippedBlankLines = text.trim()

                if(strippedBlankLines.isNotEmpty())
                    markerToInfoStrings[curCaptureMarkerId] =
                        (markerToInfoStrings[curCaptureMarkerId] ?: "") + KsoupEntities.decodeHtml(strippedBlankLines) +"\n"
            }

            // First page only
            // Check for the entry count (239 entries matched your criteria. The first 100 are listed above.)
            if(text.contains("entries matched your criteria.", ignoreCase = true)) {
                entryCount = text.substringBefore("entries").trim().toIntOrNull() ?: 0
                // println("Entry count: $entryCount")
            }
        }
        .onOpenTag { tagName, attributes, isSelfClosing ->

            // Find the "TheListItself" div, which contains the list of markers
            if(tagName == "div" && attributes["id"] == "TheListItself") {
                isListItselfFound = true
            }
            if(!isListItselfFound) return@onOpenTag

            // Found a marker
            if(tagName == "table") {
                if(attributes["id"]?.startsWith("M") == true) {
                    // println("Found a marker ${attributes["id"]}")
                    curCaptureMarkerId = attributes["id"]!!
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
                            println("Failed to parse lat value for latlong link, marker id: $curCaptureMarkerId")
                            return@onOpenTag
                        }
                    val long = attributes["href"]
                        ?.substringAfter(",")
                        ?.substringBefore(" ")
                        ?.toDoubleOrNull() ?: run {
                            println("Failed to parse long value for latlong link, marker id: $curCaptureMarkerId")
                            return@onOpenTag
                        }

                    // println("Found an a lat long link, Lat long: $lat, $long")
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
            if(tagName == "td" && isListItselfFound) {
                capturePhase++
                if(capturePhase == 2) {
                    capturePhase = 0
                    isCapturingMarkerText = false
//                    println("Captured marker: $curCaptureMarkerId")
                    //println("Captured text: ${markerToInfoStrings[curCaptureMarkerId]}")
                }
            }
        }
        .onAttribute { tagName, attributeName, attributeValue ->
            if(tagName == "id" && attributeName == "TheListItself") {
                isListItselfFound = true
            }

            // Found a marker
            if(tagName == "id" && isListItselfFound) {
                if(attributeName.startsWith("M")) {
                    attributeName.substring(1).toIntOrNull()?.let { markerId ->
                        // println("Marker id: $markerId")
                    }
                }
            }
        }
        .build()

    // Create a parser
    val ksoupHtmlParser = KsoupHtmlParser(
        handler = handler,
    )

    // Pass the HTML to the parser (It is going to parse the HTML and call the callbacks)
    ksoupHtmlParser.write(htmlResponse)
    ksoupHtmlParser.end()

    // Process the Strings in markerToInfoStrings into MarkerInfo objects
    markerToInfoStrings.forEach { (markerId, infoString) ->
        // break info string into lines
        val lines = infoString.split("\n")

        val shortLocation = lines[2]
        val title = lines[3]
        val description = lines[5]

        val lat = markerInfos[markerId]?.lat ?: run {
            println("Failed to find lat value for marker id: $markerId")
            return@forEach
        }
        val long = markerInfos[markerId]?.long ?: run {
            println("Failed to find long value for marker id: $markerId")
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

    return ParseDataResult(markerToInfoStrings, entryCount, markerInfos)
}


// Leave for reference
val smallHtmlSample = """
    <html>
        <head>
            <title>My Title</title>
        </head>
        <body>
            <h1>My Heading</h1>
            <p>My paragraph.</p>
        </body>
        <div id="TheListItself">
        <table id=M113017>
            <td colspan=4 class=bodysansserif style='padding:8 0 0 0;'>
            
                <i><small><font color=gray>1</small></i> </font>&#9658; <a Name=113017>California, San Mateo County, Menlo Park &#8212;  
                <a href='../m.asp?m=113017'>Early People of the Creek</a> &#8212; 
                <a href=" https://www.google.com/maps/dir/?api=1&destination= 37.44765,-122.170317 " target=_blank rel='noopener'>
             
                <img src=directions.png width=20 title='Driving directions to this location'></a></i>
                
            </td></tr>
        </table>
                
        <table>
            <tr>
                <td width=12></td><td width=13></td>
                <td colspan=2 style='padding-bottom:6pt;' class=bodysansserifsmaller>
                    As the largest local watercourse, San Francisquito Creek played a major role in the lives of native Americans, Spanish explorers and early Anglo settlers of the area.  
    
                    For perhaps 7,000 years, the native peoples -- called <i>Costanos</i> by  . . . 
                    <!-- --> &#8212; 
                    <span class=bodysansserifsmaller> &#8212; 
                        <a href=" ../map.asp?markers=113017 " target=_blank>Map</a><small> (db&nbsp;m113017)</small> HM
                    </span>
                </td>
            </tr>    
        </table>
        </div>
    </html>
""".trimIndent()
