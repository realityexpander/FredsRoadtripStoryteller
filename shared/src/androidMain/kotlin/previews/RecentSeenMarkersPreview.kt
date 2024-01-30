package previews

import android.content.res.Configuration
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.tooling.preview.Preview
import data.AppSettings
import data.FakeSettings
import data.MarkersRepo
import presentation.app.RecentlySeenMarkers
import presentation.maps.RecentlySeenMarker
import presentation.uiComponents.AppTheme

@Preview(
    name = "Recently Seen Markers (night)",
    group = "top element",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:width=1080px,height=1340px,dpi=440"
)
@Composable
fun RecentlySeenMarkersPreview() {
    AppTheme {
        Surface {
            RecentlySeenMarkers(
                recentlySeenMarkersForUiList = SnapshotStateList<RecentlySeenMarker>().also {
                    mutableListOf(
                        RecentlySeenMarker(
                            id = "M1234569",
                            title = "The story of the Falls Church Episcopal",
                            insertedAtEpochMilliseconds = 0,
                        ),
                        RecentlySeenMarker(
                            id = "M2020202",
                            title = "Sears Home Kit",
                            insertedAtEpochMilliseconds = 0,
                        ),
                        RecentlySeenMarker(
                            title = "Here is a very long title of a marker that will be truncated at this very long length due to the sordid details of this insanely long title",
                            id = "M123987",
                            insertedAtEpochMilliseconds = 0,
                        ),
                    ).forEach { marker ->
                        it.add(marker)
                    }
                },
                activeSpeakingMarker = RecentlySeenMarker(
                    id = "M69420",
                    // title = "Plans to fight the Ordinance of Nullification",
                    title = "Plans to fight the Ordinance",
                    insertedAtEpochMilliseconds = 0,
                ),
                isTextToSpeechCurrentlySpeaking = true,
                isSpeakWhenUnseenMarkerFoundEnabled = false,
                markersRepo = MarkersRepo(
                    appSettings = AppSettings.use(settings = FakeSettings()),
                ),
                onClickRecentlySeenMarkerItem = {},
                onClickStartSpeakingMarker = { _, _ -> Unit },
                onClickStopSpeakingMarker = {},
                onClickPauseSpeakingMarker = {},
                onClickPauseSpeakingAllMarkers = {},
            ) {}
        }
    }
}
@Preview(
    name = "Recently Seen Markers",
    group = "top element", device = "spec:width=1080px,height=1340px,dpi=440"
)
@Composable
fun RecentlySeenMarkersLightPreview() {
    RecentlySeenMarkersPreview()
}
