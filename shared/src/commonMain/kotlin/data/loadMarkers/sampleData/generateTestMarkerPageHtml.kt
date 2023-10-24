package data.loadMarkers.sampleData

import almadenVineyardsSingleItemPageHtmlPage1

const val kUseRealNetwork = 0
const val kSunnyvaleFakeDataset = 1
const val kTepoztlanFakeDataset = 2
const val kSingleItemPageFakeDataset = 3 // near Sunnyvale, CA

fun simpleMarkersPageHtml(pageNum: Int, useTestData: Int): String {
    return when (useTestData) {
            kSunnyvaleFakeDataset -> { // 3 pages based near Sunnyvale, CA
                when (pageNum) {
                    1 -> sunnyvaleMarkersHtmlPage1()
                    2 -> sunnyvaleMarkersHtmlPage2()
                    3 -> sunnyvaleMarkersHtmlPage3()
                    else -> throw Exception("Invalid page number: $pageNum")
                }
            }
            kTepoztlanFakeDataset -> { // 1 page only near Tepoztlan, Mexico
                when (pageNum) {
                    1 -> tepoztlanMarkersHtmlPage1()
                    else -> throw Exception("Invalid page number: $pageNum")
                }
            }
            kSingleItemPageFakeDataset -> { // 1 page only with 1 item near in Sunnyvale, CA
                when (pageNum) {
                    1 -> almadenVineyardsSingleItemPageHtmlPage1()
                    else -> throw Exception("Invalid page number: $pageNum")
                }
            }
            else -> throw Exception("Invalid dataset id: $useTestData")
        }
}
