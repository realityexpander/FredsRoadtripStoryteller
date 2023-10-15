package loadMarkers.sampleData

import singleItemPageFakeMarkerHtmlPage1
import loadMarkers.LoadingState

const val kUseRealNetwork = 0
const val kSunnyvaleFakeDataset = 1
const val kTepoztlanFakeDataset = 2
const val kSingleItemPageFakeDataset = 3 // near Sunnyvale, CA

//fun generateFakeMarkerPageHtml(pageNum: Int, useTestData: Int): LoadingState<String> {
fun generateFakeMarkerPageHtml(pageNum: Int, useTestData: Int): String {
//    return LoadingState.Loaded(
    return when (useTestData) {
            kSunnyvaleFakeDataset -> { // 3 pages based near Sunnyvale, CA
                when (pageNum) {
                    1 -> sunnyvaleFakeMarkerHtmlPage1()
                    2 -> sunnyvaleFakeMarkerHtmlPage2()
                    3 -> sunnyvaleFakeMarkerHtmlPage3()
                    else -> throw Exception("Invalid page number: $pageNum")
                }
            }
            kTepoztlanFakeDataset -> { // 1 page only near Tepoztlan, Mexico
                when (pageNum) {
                    1 -> tepoztlanFakeDatasetPage1()
                    else -> throw Exception("Invalid page number: $pageNum")
                }
            }
            kSingleItemPageFakeDataset -> { // 1 page only with 1 item near in Sunnyvale, CA
                when (pageNum) {
                    1 -> singleItemPageFakeMarkerHtmlPage1()
                    else -> throw Exception("Invalid page number: $pageNum")
                }
            }
            else -> throw Exception("Invalid dataset id: $useTestData")
        }
//    )
}
