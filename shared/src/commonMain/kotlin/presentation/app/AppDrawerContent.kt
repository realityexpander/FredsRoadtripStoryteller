package presentation.app

import BottomSheetScreen
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kAppNameStr
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import presentation.maps.Marker

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun AppDrawerContent(
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    finalMarkers: List<Marker>,
    onSetBottomSheetActiveScreen: (BottomSheetScreen) -> Unit = {},
    onShowOnboarding: () -> Unit = {},
    onShowAboutBox: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()

    val entries: SnapshotStateList<Marker> = remember { mutableStateListOf() }
    LaunchedEffect(finalMarkers) {
        // Log.d("📌📌📌AppDrawerContent: LaunchedEffect(finalMarkers) calculating entries...")
        entries.clear()
        entries.addAll(finalMarkers.reversed())
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            kAppNameStr,
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

    // Show onboarding button
    Button(
        onClick = {
            coroutineScope.launch {
                onShowOnboarding()
                yield()
                bottomSheetScaffoldState.drawerState.close()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp),
    ) {
        Text(
            "Show Onboarding",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),
            fontStyle = FontStyle.Normal,
            fontSize = MaterialTheme.typography.body1.fontSize,
            textAlign = TextAlign.Center,
        )
    }
    Spacer(modifier = Modifier.height(16.dp))


    // Show about box
    Button(
        onClick = {
            coroutineScope.launch {
                onShowAboutBox()
                yield()
                bottomSheetScaffoldState.drawerState.close()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp),
    ) {
        Text(
            "About this app",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),
            fontStyle = FontStyle.Normal,
            fontSize = MaterialTheme.typography.body1.fontSize,
            textAlign = TextAlign.Center,
        )
    }
    Spacer(modifier = Modifier.height(16.dp))

    // Header for list of loaded markers
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
    ) {
        Text(
            "Loaded Markers",
            modifier = Modifier.weight(2.5f),
            fontStyle = FontStyle.Italic,
            fontSize = MaterialTheme.typography.body2.fontSize,
            fontWeight = FontWeight.Bold,
        )
        Icon(
            imageVector = Icons.Default.Visibility,
            contentDescription = "Seen",
            modifier = Modifier
                .weight(.3f)
                .height(18.dp)
                .offset((-6).dp, 2.dp)
        )
        Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Spoken",
            modifier = Modifier
                .weight(.3f)
                .height(18.dp)
                .offset((-4).dp, 2.dp)
        )
        Text(
            "ID",
            modifier = Modifier
                .padding(start = 0.dp, end = 8.dp)
                .weight(1.2f)
                .offset((-4).dp, 0.dp),
            fontStyle = FontStyle.Italic,
            fontSize = MaterialTheme.typography.body2.fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Start,
        )
    }

    if (finalMarkers.isEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "No markers loaded yet, drive around to load some!",
            modifier = Modifier.padding(start = 8.dp),
            fontSize = MaterialTheme.typography.h6.fontSize,
            fontWeight = FontWeight.Normal,
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight(),
        state = rememberLazyListState(),
        userScrollEnabled = true,
    ) {
        // Header

        items(entries.size) { markerIdx ->
            val marker = entries[markerIdx]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .animateItemPlacement(animationSpec = tween(250))
                    .clickable {
                        coroutineScope.launch {
                            println("Clicked on marker: ${marker.id}")
                            bottomSheetScaffoldState.bottomSheetState.expand()
                            onSetBottomSheetActiveScreen(
                                BottomSheetScreen.MarkerDetailsScreen(id=marker.id)
                            )
                            bottomSheetScaffoldState.drawerState.close()
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = marker.title,
                    modifier = Modifier.weight(2.5f),
                    softWrap = false,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontStyle = FontStyle.Normal,
                    fontSize = MaterialTheme.typography.body1.fontSize,
                )

                // isSeen
                if (marker.isSeen) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Seen",
                        modifier = Modifier
                            .weight(.3f)
                            .height(16.dp)
                    )
                } else {
                    // "leave blank"
                    Spacer(
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .weight(.3f)
                            .height(16.dp)
                    )
                }

                // isSpoken
                if (marker.isSpoken) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Spoken",
                        modifier = Modifier
                            .weight(.3f)
                            .height(16.dp)
                    )
                } else {
                    // "leave blank"
                    Spacer(
                        modifier = Modifier
                            .padding(end = 2.dp)
                            .weight(.3f)
                            .height(16.dp)
                    )
                }

                Text(
                    text = marker.id,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .weight(1.2f),
                    fontStyle = FontStyle.Normal,
                    fontSize = MaterialTheme.typography.body1.fontSize,
                )
            }
        }
    }
}
