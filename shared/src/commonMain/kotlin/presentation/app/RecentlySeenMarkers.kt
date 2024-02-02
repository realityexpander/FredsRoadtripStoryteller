package presentation.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import data.MarkersRepo
import data.appSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import presentation.maps.MarkerIdStr
import presentation.maps.RecentlySeenMarker
import presentation.uiComponents.darkenBy

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecentlySeenMarkers(
    recentlySeenMarkersForUiList: List<RecentlySeenMarker>,
    activeSpeakingMarker: RecentlySeenMarker? = null,
    isTextToSpeechCurrentlySpeaking: Boolean = false,
    isSpeakWhenUnseenMarkerFoundEnabled: Boolean = appSettings.isSpeakWhenUnseenMarkerFoundEnabled,
    markersRepo: MarkersRepo,
    onClickRecentlySeenMarkerItem: (MarkerIdStr) -> Unit = {},
    onClickStartSpeakingMarker: (RecentlySeenMarker, shouldSpeakDetails: Boolean) -> Unit =
        { _, _ -> Unit },
    onClickPauseSpeakingMarker: () -> Unit = {},
    onClickStopSpeakingMarker: () -> Unit = {},
    onClickPauseSpeakingAllMarkers: () -> Unit = {},
    onClickResumeSpeakingAllMarkers: () -> Unit = {},
    onClickSkipSpeakingToNextMarker: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    var curTopItemKey by remember { mutableStateOf("") }

    // Scroll to top when first item key changes
    LaunchedEffect(
        recentlySeenMarkersForUiList.isNotEmpty()
        && recentlySeenMarkersForUiList.first().id != curTopItemKey
    ) {
        if(recentlySeenMarkersForUiList.isEmpty()) return@LaunchedEffect

        curTopItemKey = recentlySeenMarkersForUiList.first().id
        coroutineScope.launch {
            scrollState.animateScrollToItem(0)
        }
    }

    val speakingMarker by remember(activeSpeakingMarker) {
        mutableStateOf(activeSpeakingMarker)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Active Speaking marker
        AnimatedVisibility(
            speakingMarker != null,
            enter = expandVertically(tween(1500)),
            exit = fadeOut(tween(500))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp, 4.dp, 8.dp, 8.dp)
                    .animateContentSize(
                        animationSpec = tween(500)
                    ) { initialValue, targetValue ->
                        if (initialValue.height == 0) {
                            expandVertically()
                        } else {
                            shrinkVertically()
                        }
                    }
                ,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // Active Speaking Marker Title & ID, Speak Marker Button, Pause Speaking All Markers Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .wrapContentSize()
                ) {

                    // Marker Title & ID
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colors.primary.darkenBy(.2f)
                                    .copy(alpha = 0.75f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(8.dp, 0.dp, 8.dp, 4.dp)
                            .clickable {
                                onClickRecentlySeenMarkerItem(speakingMarker?.id ?: return@clickable)
                            }
                            .weight(3f)
                    ) {
                        Text(
                            text = speakingMarker?.title ?: "",
                            color = MaterialTheme.colors.onPrimary,
                            fontStyle = FontStyle.Normal,
                            fontSize = MaterialTheme.typography.h6.fontSize,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = (speakingMarker?.id ?: "") + " "
                                    + if (isTextToSpeechCurrentlySpeaking) "speaking" else "spoken last",
                            color = MaterialTheme.colors.onPrimary.copy(alpha = 0.50f),
                            fontStyle = FontStyle.Normal,
                            fontSize = MaterialTheme.typography.body1.fontSize,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))

                    // Speak/Pause Marker Button
                    Column(
                        modifier = Modifier
                            .weight(.5f)
                            .background(
                                color = MaterialTheme.colors.primary.darkenBy(.2f)
                                    .copy(alpha = 0.75f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (isTextToSpeechCurrentlySpeaking) {
                            IconButton(
                                onClick = {
                                    onClickPauseSpeakingMarker()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Pause,
                                    contentDescription = "Stop Speaking Marker",
                                    tint = MaterialTheme.colors.onBackground
                                )
                            }
                        } else {
                            IconButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp),
                                onClick = {
                                    speakingMarker ?: return@IconButton

                                    markersRepo.updateMarkerIsSpoken(
                                        id = speakingMarker?.id ?: return@IconButton,
                                        isSpoken = true
                                    )
                                    onClickStartSpeakingMarker(
                                        speakingMarker ?: return@IconButton,
                                        appSettings.isSpeakDetailsWhenUnseenMarkerFoundEnabled
                                    )
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VolumeUp,
                                    contentDescription = "Speak Marker",
                                    tint = MaterialTheme.colors.onBackground
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))

                    // Skip to Next Marker Button
                    Column(
                        modifier = Modifier
                            .weight(.5f)
                            .background(
                                color = MaterialTheme.colors.primary.darkenBy(.2f)
                                    .copy(alpha = 0.75f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        IconButton(
                            onClick = {
                                onClickSkipSpeakingToNextMarker()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Skip to Next Marker",
                                tint = MaterialTheme.colors.onBackground
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))

                    //NavigationButton( // LEAVE FOR FUTURE USE
                    //    modifier = Modifier
                    //        .fillMaxWidth()
                    //        .weight(.5f),
                    //    markersRepo,
                    //    speakingMarker
                    //)

                    // Pause/Resume Speaking ALL Markers Button
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(.5f)
                            .background(
                                color = MaterialTheme.colors.primary.darkenBy(.2f)
                                    .copy(alpha = 0.75f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                    ) {
                        if (isSpeakWhenUnseenMarkerFoundEnabled) {
                            // Pause Speaking All Markers Button
                            IconButton(
                                onClick = {
                                    onClickPauseSpeakingAllMarkers()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                            ) {
                                Column {
                                    Icon(
                                        imageVector = Icons.Outlined.Pause,
                                        contentDescription = "Pause Speaking All Markers",
                                        tint = MaterialTheme.colors.onBackground
                                    )
                                    Text(
                                        "ALL",
                                        fontSize = MaterialTheme.typography.body2.fontSize.times(
                                            .75f
                                        ),
                                        color = MaterialTheme.colors.onBackground.copy(alpha =.75f)
                                    )
                                }
                            }
                        } else {
                            // Resume Speaking All Markers Button
                            IconButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp),
                                onClick = {
                                    onClickResumeSpeakingAllMarkers()
                                }
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.VolumeUp,
                                        contentDescription = "Start Speaking All Markers",
                                        tint = MaterialTheme.colors.onBackground
                                    )
                                    Text(
                                        "ALL",
                                        fontSize = MaterialTheme.typography.body2.fontSize.times(
                                            .75f
                                        ),
                                        color = MaterialTheme.colors.onBackground.copy(alpha =.75f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        LazyColumn(
            userScrollEnabled = true,
            state = scrollState,
            modifier = Modifier.background(MaterialTheme.colors.surface)
        ) {
            // Header & "empty" placeholder
            if (recentlySeenMarkersForUiList.isEmpty()) {
                // Show "empty" placeholder if no markers
                item {
                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = "No recently seen markers, drive around to see some!",
                        color = MaterialTheme.colors.onSurface,
                        fontStyle = FontStyle.Normal,
                        fontSize = MaterialTheme.typography.h6.fontSize,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp)
                    )
                }
            } else {
                // Header
                item(
                    "header"
                ) {
                    Text(
                        text = "TOP 5 RECENTLY SEEN MARKERS",
                        color = MaterialTheme.colors.onSurface,
                        fontStyle = FontStyle.Normal,
                        fontSize = MaterialTheme.typography.subtitle2.fontSize,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 0.dp, bottom = 0.dp)
                    )
                }
            }

            items(
                recentlySeenMarkersForUiList.size,
                key = { index -> recentlySeenMarkersForUiList[index].id }
            ) { idx ->
                val recentMarker = recentlySeenMarkersForUiList.elementAt(idx)

                // Marker item row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 4.dp, 8.dp, 4.dp)
                        .background(
                            color = MaterialTheme.colors.primary,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .animateItemPlacement(animationSpec = tween(500))
                    ,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    // Marker Title & ID
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .padding(8.dp, 0.dp, 8.dp, 4.dp)
                            .clickable {
                                onClickRecentlySeenMarkerItem(recentMarker.id)
                            }
                            .weight(3f)
                    ) {
                        Text(
                            text = recentMarker.title,
                            color = MaterialTheme.colors.onPrimary,
                            fontStyle = FontStyle.Normal,
                            fontSize = MaterialTheme.typography.h6.fontSize,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = recentMarker.id,
                            color = MaterialTheme.colors.onPrimary.copy(alpha = 0.50f),
                            fontStyle = FontStyle.Normal,
                            fontSize = MaterialTheme.typography.body1.fontSize,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    //Column( // LEAVE FOR POSSIBLE FUTURE USE - NAVIGATE TO MARKER
                    //    modifier = Modifier
                    //        .fillMaxWidth()
                    //        .weight(.5f)
                    //) {
                    //    NavigationButton(markersRepo = markersRepo, recentMarker = recentMarker)
                    //}

                    // Speak Marker Button
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(.5f)
                    ) {
                        if (markersRepo.marker(recentMarker.id)?.isSpoken == true) {
                            // Show the stop icon if this marker is the currently speaking marker
                            if (speakingMarker?.id == recentMarker.id && isTextToSpeechCurrentlySpeaking) {
                                // Stop speaking marker
                                IconButton(
                                    onClick = {
                                        onClickStopSpeakingMarker()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop Speaking Marker",
                                    )
                                }
                            } else {
                                // Speak "Already spoken" marker again
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            delay(150)
                                            onClickStartSpeakingMarker(recentMarker, true)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeMute,
                                        contentDescription = "Speak Marker Again",
                                        tint = MaterialTheme.colors.onBackground.copy(alpha =.75f)
                                    )
                                }
                            }
                        } else {
                            // Speak  "Never spoken" marker
                            IconButton(
                                onClick = {
                                    onClickStartSpeakingMarker(recentMarker, true)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.VolumeUp,
                                    contentDescription = "Speak Marker",
                                    tint = MaterialTheme.colors.onBackground // note: no alpha
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
