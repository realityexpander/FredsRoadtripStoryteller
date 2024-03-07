@file:Suppress("FunctionName") // for Composable names

package presentation.app

import BottomSheetScreen
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import appNameStr
import data.AppSettings
import data.billing.CommonBilling
import data.billing.CommonBilling.BillingState
import data.billing.isProVersionEnabled
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import openNavigationAction
import presentation.maps.Marker
import presentation.maps.RecentlySeenMarker
import presentation.uiComponents.PurchaseProVersionButton

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerContent(
    finalMarkers: List<Marker>,
    onSetBottomSheetActiveScreen: (BottomSheetScreen) -> Unit = {},
    onShowOnboarding: () -> Unit = {},
    onShowAboutBox: () -> Unit = {},
    onExpandBottomSheet: () -> Unit = {},
    onCloseDrawer: () -> Unit = {},
    activeSpeakingMarker: RecentlySeenMarker? = null,
    isMarkerCurrentlySpeaking: Boolean = false,
    onClickStartSpeakingMarker: (Marker, isSpeakDetailsEnabled: Boolean) -> Unit = { _, _ -> },
    onClickStopSpeakingMarker: () -> Unit = {},
    onLocateMarker: (Marker) -> Unit = {},
    commonBilling: CommonBilling,
    billingState: BillingState,
    calcTrialTimeRemainingStringFunc: () -> String,
    appSettings: AppSettings,
    onDisplayPurchaseProMessage: () -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()

    val entries: SnapshotStateList<Marker> = remember { mutableStateListOf() }
    val searchMarkers = remember { mutableStateListOf<Marker>() }
    LaunchedEffect(finalMarkers) {
        // Log.d("📌📌📌AppDrawerContent: LaunchedEffect(finalMarkers) calculating entries...")
        entries.clear()
        entries.addAll(finalMarkers.reversed())

        searchMarkers.clear()
        searchMarkers.addAll(finalMarkers.reversed())
    }

    // App title & close button
    Row(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            appNameStr,
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
                    onCloseDrawer()
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
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
    ) {
        Text(
            "How to use this App",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),
            fontStyle = FontStyle.Normal,
            fontSize = MaterialTheme.typography.body1.fontSize,
            textAlign = TextAlign.Center,
        )
    }
    Spacer(modifier = Modifier.height(16.dp))

    // Purchase Pro Version
    PurchaseProVersionButton(
        billingState,
        commonBilling,
        coroutineScope,
        onCloseDrawer,
        calcTrialTimeRemainingStringFunc()
    )

    // Show about box
    Button(
        onClick = {
            coroutineScope.launch {
                onShowAboutBox()
                yield()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
    ) {
        Text(
            "About this App",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp),
            fontStyle = FontStyle.Normal,
            fontSize = MaterialTheme.typography.body1.fontSize,
            textAlign = TextAlign.Center,
        )
    }
    Spacer(modifier = Modifier.height(16.dp))

    // Search box
    var searchQuery by remember { mutableStateOf("") }
    var isSearchDialogVisible by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colors.primary.copy(alpha = 0.5f))
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        isSearchDialogVisible = true
                    }
                }
                .focusProperties {
                    if(isSearchDialogVisible) {
                        canFocus = false
                    }
                }
            ,
            placeholder = {
                Text(
                    text = "Search Marker Title...",
                    fontWeight = FontWeight.Bold,
                )
            },
            value = searchQuery,
            onValueChange = { },
            readOnly = true,
            singleLine = true,
            maxLines = 1,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.colors.primary,
                placeholderColor = MaterialTheme.colors.onBackground,
                leadingIconColor = MaterialTheme.colors.onBackground,
                textColor = MaterialTheme.colors.onBackground,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colors.onBackground,
            ),
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchQuery = ""
                            searchMarkers.clear()
                            searchMarkers.addAll(finalMarkers.reversed())
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Clear search",
                            modifier = Modifier
                                .height(18.dp)
                        )
                    }
                }
            },
        )
    }
    Spacer(modifier = Modifier.height(8.dp))

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

    if(isSearchDialogVisible) {
        SearchMarkerDialog(
            initialSearchQuery = searchQuery,
            finalMarkers = finalMarkers,
            activeSpeakingMarker = activeSpeakingMarker,
            isMarkerCurrentlySpeaking = isMarkerCurrentlySpeaking,
            onSearchResult = { query, searchMarkersResult ->
                searchQuery = query
                searchMarkers.clear()
                searchMarkers.addAll(searchMarkersResult)
                isSearchDialogVisible = false
            },
            onShowMarkerDetails = { marker ->
                isSearchDialogVisible = false
                coroutineScope.launch {
                    onExpandBottomSheet()
                    onSetBottomSheetActiveScreen(
                        BottomSheetScreen.MarkerDetailsScreen(id=marker.id)
                    )
                    onCloseDrawer()
                }
            },
            onLocateMarker = { marker ->
                isSearchDialogVisible = false
                onLocateMarker(marker)
            },
            onClickStartSpeakingMarker = onClickStartSpeakingMarker,
            onClickStopSpeakingMarker = onClickStopSpeakingMarker,
            onDismiss = {
                isSearchDialogVisible = false
            },
            appSettings = appSettings,
            billingState = billingState,
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxHeight(),
        state = rememberLazyListState(),
        userScrollEnabled = true,
    ) {
        // Header

        items(searchMarkers.size) { markerIdx ->
            val marker = searchMarkers[markerIdx]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .animateItemPlacement(animationSpec = tween(250))
                    .clickable {
                        if (!data.appSettings.isProVersionEnabled(billingState)) {
                            onDisplayPurchaseProMessage()
                            return@clickable
                        }

                        coroutineScope.launch {
                            onExpandBottomSheet()
                            onSetBottomSheetActiveScreen(
                                BottomSheetScreen.MarkerDetailsScreen(id=marker.id)
                            )
                            onCloseDrawer()
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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun SearchMarkerDialog(
    initialSearchQuery: String = "",
    finalMarkers: List<Marker>,
    activeSpeakingMarker: RecentlySeenMarker? = null,
    isMarkerCurrentlySpeaking: Boolean = false,
    onSearchResult: (searchQuery: String, searchResult: List<Marker>) -> Unit =
        { _, _ -> },
    onShowMarkerDetails: (Marker) -> Unit = {},
    onLocateMarker: (Marker) -> Unit = {},
    onClickStartSpeakingMarker: (Marker, isSpeakDetailsEnabled: Boolean) -> Unit = { _, _ -> },
    onClickStopSpeakingMarker: () -> Unit = {},
    onDismiss: () -> Unit = {},
    appSettings: AppSettings,
    billingState: BillingState,
) {
    val coroutineScope = rememberCoroutineScope()
    var searchQuery by remember(initialSearchQuery) { mutableStateOf(initialSearchQuery) }
    var showDialogForProVersion by remember { mutableStateOf(false) }

    Dialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
        onDismissRequest = {
            onDismiss()
        },
    ) {
        val searchEntries = remember { mutableStateListOf<Marker>().also {
                calcSearchEntries(searchQuery, it, finalMarkers)
            } }
        val keyboardController = LocalSoftwareKeyboardController.current

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colors.onBackground.copy(alpha = 0.1f))
                .clickable {
                    keyboardController?.show()
                }
        ) {

            // Search box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Close button
                IconButton(
                    onClick = {
                        onSearchResult(searchQuery, searchEntries)
                        onDismiss()
                    },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .align(Alignment.End)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.onBackground.copy(alpha = 0.1f))
                ) {
                    TextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = "Search Marker Title...",
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            calcSearchEntries(searchQuery, searchEntries, finalMarkers)
                        },
                        singleLine = true,
                        maxLines = 1,
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = MaterialTheme.colors.background,
                            placeholderColor = MaterialTheme.colors.onBackground,
                            leadingIconColor = MaterialTheme.colors.onBackground,
                            textColor = MaterialTheme.colors.onBackground,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colors.onBackground,
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = ""
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        searchQuery = ""
                                        searchEntries.clear()
                                        searchEntries.addAll(finalMarkers.reversed())
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Clear search",
                                        modifier = Modifier
                                            .height(18.dp)
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = androidx.compose.ui.text.input.ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions {
                            keyboardController?.hide()
                        }
                    )
                }
            }

            // List of markers
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 118.dp),
                state = rememberLazyListState(),
                userScrollEnabled = true,
            ) {

                items(searchEntries.size) { markerIdx ->
                    val marker = searchEntries[markerIdx]

                    if(searchEntries.size == 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No markers found",
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = MaterialTheme.typography.h6.fontSize,
                            fontWeight = FontWeight.Normal,
                        )
                    }

                    // Marker item row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp, 4.dp, 8.dp, 8.dp)
                            .background(
                                color = MaterialTheme.colors.primary,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .animateItemPlacement(animationSpec = tween(250))
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
                                    if (!appSettings.isProVersionEnabled(billingState)) {
                                        showDialogForProVersion = true

                                        return@clickable
                                    }

                                    onShowMarkerDetails(marker)
                                }
                                .weight(3f)
                        ) {
                            Text(
                                text = marker.title,
                                color = MaterialTheme.colors.onPrimary,
                                fontStyle = FontStyle.Normal,
                                fontSize = MaterialTheme.typography.h6.fontSize,
                                fontWeight = FontWeight.Medium,
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = marker.id,
                                color = MaterialTheme.colors.onPrimary.copy(alpha = 0.50f),
                                fontStyle = FontStyle.Normal,
                                fontSize = MaterialTheme.typography.body1.fontSize,
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        //// Navigate to Marker // LEAVE FOR REFERENCE
                        //Column(
                        //    modifier = Modifier
                        //        .fillMaxWidth()
                        //        .weight(.5f)
                        //) {
                        //    NavigationActionButton(marker)
                        //}

                        // Locate on Map
                        IconButton(
                            onClick = {
                                onLocateMarker(marker)
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Show marker on map",
                                tint = MaterialTheme.colors.onBackground
                            )
                        }

                        // Speak Marker Button
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(.5f)
                        ) {
                            if (marker.isSpoken) {
                                // Show the stop icon if this marker is the currently speaking marker
                                if (activeSpeakingMarker?.id == marker.id && isMarkerCurrentlySpeaking) {
                                    // Stop speaking marker button
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
                                    // Show `Speak "Already spoken" marker` button
                                    IconButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                onClickStopSpeakingMarker()
                                                delay(1000)
                                                onClickStartSpeakingMarker(marker, true)
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
                                // Show `Speak "Never spoken" marker` button
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            onClickStartSpeakingMarker(marker, true)
                                            delay(1500) // allow `isSeen` to update
                                            calcSearchEntries(searchQuery, searchEntries, finalMarkers)
                                        }
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

        if(showDialogForProVersion) {
            ShowDialogForProVersion(
                onDismiss = {
                    showDialogForProVersion = false
                }
            )
        }
    }
}

@Composable
private fun ShowDialogForProVersion(
    onDismiss: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text("Pro Version Required")
        },
        text = {
            Text("This feature is only available in the Pro version of the app.")
            Text("Please purchase the Pro version to use this feature.")
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                }
            ) {
                Text("OK")
            }
        }
    )
}

private fun calcSearchEntries(
    searchQuery: String,
    searchEntries: SnapshotStateList<Marker>,
    finalMarkers: List<Marker>
) {
    if (searchQuery.isEmpty()) {
        searchEntries.clear()
        searchEntries.addAll(finalMarkers.reversed())
    } else {
        searchEntries.clear()
        searchEntries.addAll(
            finalMarkers.filter { marker ->
                marker.title.contains(searchQuery, ignoreCase = true)
            }.sortedBy { marker ->
                marker.title
            }
        )
    }
}

@Composable
private fun NavigationActionButton(
    marker: Marker,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier,
        onClick = {
            // Lookup the marker in the repo and open navigation to it
            openNavigationAction(
                lat = marker.position.latitude,
                lng = marker.position.longitude,
                markerTitle = marker.title
            )
        }
    ) {
        Icon(
            imageVector = Icons.Default.Navigation,
            contentDescription = "Navigate to Marker",
            tint = MaterialTheme.colors.onBackground
        )
    }
}

