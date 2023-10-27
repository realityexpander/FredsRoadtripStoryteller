package presentation.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import maps.MapMarker
import maps.RecentMapMarker

@Composable
fun RecentlySeenMarkers(
    recentlySeenMarkersForUiList: SnapshotStateList<RecentMapMarker>,
    onClickRecentlySeenMarkerItem: ((MapMarker) -> Unit) = {}
) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            userScrollEnabled = true,
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
        ) {
            // Header
            item {
                Text(
                    text = "RECENTLY SEEN MARKERS",
                    color = MaterialTheme.colors.onSurface,
                    fontStyle = FontStyle.Normal,
                    fontSize = MaterialTheme.typography.subtitle2.fontSize,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, bottom = 8.dp)
                )
            }

            // Show "empty" placeholder if no markers
            if (recentlySeenMarkersForUiList.isEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = "No recently seen markers, drive around to see some!",
                        color = MaterialTheme.colors.onBackground,
                        fontStyle = FontStyle.Normal,
                        fontSize = MaterialTheme.typography.h6.fontSize,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp)
                    )
                }
            }

            items(recentlySeenMarkersForUiList.size) {
                val recentMarker = recentlySeenMarkersForUiList.elementAt(it)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 0.dp, 8.dp, 8.dp)
                        .background(
                            color = MaterialTheme.colors.primary.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .heightIn(min = 48.dp)
                        .padding(8.dp, 0.dp, 8.dp, 4.dp)
                        .clickable {
                            onClickRecentlySeenMarkerItem(recentMarker.marker)
                        }
                ) {
                    Text(
                        text = recentMarker.marker.title,
                        color = MaterialTheme.colors.onPrimary,
                        fontStyle = FontStyle.Normal,
                        fontSize = MaterialTheme.typography.h6.fontSize,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = "◉ " + recentMarker.key(),
                        color = MaterialTheme.colors.onPrimary.copy(alpha = 0.50f),
                        fontStyle = FontStyle.Normal,
                        fontSize = MaterialTheme.typography.body1.fontSize,
                        fontWeight = FontWeight.Medium,
                    )

                }
            }
        }
    }
}
