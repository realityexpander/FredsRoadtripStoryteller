package presentation.app

import BottomSheetScreen
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.loadMarkers.MarkersResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppDrawerContent(
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    fetchedMarkersResult: MarkersResult,
    onSetBottomSheetActiveScreen: (BottomSheetScreen) -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    // Header
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Mystery Marker Madness",
            fontSize = MaterialTheme.typography.h5.fontSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .weight(3f)
        )
        IconButton(
            modifier = Modifier
                .offset(16.dp, (-16).dp),
            onClick = {
                coroutineScope.launch {
                    bottomSheetScaffoldState.drawerState.close()
                }
            }) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close"
            )
        }

    }
    Spacer(modifier = Modifier.height(16.dp))

    // Show all loaded markers
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
    ) {
        Text(
            "Loaded Markers",
            modifier = Modifier.weight(3f),
            fontStyle = FontStyle.Italic,
            fontSize = MaterialTheme.typography.body2.fontSize,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "ID",
            modifier = Modifier
                .padding(start=16.dp, end = 8.dp)
                .weight(1.3f),
            fontStyle = FontStyle.Italic,
            fontSize = MaterialTheme.typography.body2.fontSize,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Seen",
            modifier = Modifier.weight(.8f)
                .padding(end = 8.dp),
            fontStyle = FontStyle.Italic,
            fontSize = MaterialTheme.typography.body2.fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }

    // List all loaded markers
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .verticalScroll(
                state = rememberScrollState(),
                enabled = true,
            ),

    ) {
        fetchedMarkersResult.markerIdToMapMarkerMap
            .entries
            .reversed()
            .forEach { marker ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            coroutineScope.launch {
                                println("Clicked on marker: ${marker.value.id}")
                                onSetBottomSheetActiveScreen(
                                    BottomSheetScreen.MarkerDetailsScreen(marker.value)
                                )
                                bottomSheetScaffoldState.drawerState.close()
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = marker.value.title,
                        modifier = Modifier.weight(3f),
                        softWrap = false,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontStyle = FontStyle.Normal,
                        fontSize = MaterialTheme.typography.body1.fontSize,
                    )
                    Text(
                        text = marker.key,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .weight(1.2f),
                        fontStyle = FontStyle.Normal,
                        fontSize = MaterialTheme.typography.body1.fontSize,
                    )

                    if(marker.value.isSeen) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Seen",
                            modifier = Modifier
                                .weight(.4f)
                        )
                    }
                    else {
                        // "leave blank"
                        Spacer(
                            modifier = Modifier
                                .padding(end = 2.dp)
                                .weight(.4f)
                        )
                    }
                }
            }
    }
}
