package data.loadMarkers.sampleData

import almadenSingleItemPageHtmlPage1

const val kUseRealNetwork = 0
const val kSunnyvaleFakeDataset = 1
const val kTepoztlanFakeDataset = 2
const val kSingleItemPageFakeDataset = 3 // near Sunnyvale, CA

fun generateTestPageHtml(pageNum: Int, useTestData: Int): String {
    return when (useTestData) {
            kSunnyvaleFakeDataset -> { // 3 pages based near Sunnyvale, CA
                when (pageNum) {
                    1 -> sunnyvaleMarkersPage1Html()
                    2 -> sunnyvaleMarkersPage2Html()
                    3 -> sunnyvaleMarkersPage3Html()
                    else -> throw Exception("Invalid page number: $pageNum")
                }
            }
            kTepoztlanFakeDataset -> { // 1 page only near Tepoztlan, Mexico
                when (pageNum) {
                    1 -> tepoztlanMarkersPage1Html()
                    else -> throw Exception("Invalid page number: $pageNum")
                }
            }
            kSingleItemPageFakeDataset -> { // 1 page only with 1 item near in Sunnyvale, CA
                when (pageNum) {
                    1 -> almadenSingleItemPageHtmlPage1()
                    else -> throw Exception("Invalid page number: $pageNum")
                }
            }
            else -> throw Exception("Invalid dataset id: $useTestData")
        }
}
