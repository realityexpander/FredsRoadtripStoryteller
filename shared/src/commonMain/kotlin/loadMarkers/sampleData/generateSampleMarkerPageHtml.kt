package loadMarkers.sampleData

import loadMarkers.LoadingState

fun generateSampleMarkerPageHtml(pageNum: Int, useTestData: Int): LoadingState<String> {
    return LoadingState.Loaded(
        if(useTestData == 1) {
            when (pageNum) {
                1 -> fullHtmlSamplePage1()  // 3 pages based in Sunnyvale, CA
                2 -> fullHtmlSamplePage2()
                3 -> fullHtmlSamplePage3()
                else -> throw Exception("Invalid page number: $pageNum")
            }
        } else {
            when (pageNum) {
                1 -> fullHtmlSamplePage1a()  // 1 page only based in Tepoztlan, Mexico
                else -> throw Exception("Invalid page number: $pageNum")
            }
        }
    )
}
