package presentation.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import data.util.LoadingState
import io.kamel.core.ExperimentalKamelApi
import io.kamel.core.Resource
import io.kamel.image.KamelImage
import io.kamel.image.KamelImageBox
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import openNavigationAction
import presentation.maps.Marker
import presentation.uiComponents.PreviewPlaceholder
import stopTextToSpeech

const val kMaxWeightOfBottomDrawer = 0.9f // 90% of screen height (10% peeking thru at the top)

@OptIn(ExperimentalKamelApi::class, ExperimentalFoundationApi::class)
@Composable
fun MarkerDetailsScreen(
    initialDisplayMarker: Marker,  // Which marker to display first.
    markers: StateFlow<List<Marker>>,
    isTextToSpeechCurrentlySpeaking: Boolean = false,
    onClickStartSpeakingMarker: (Marker) -> Unit = {},
    onLocateMarkerOnMap: (Marker) -> Unit = {},
    onDismiss: () -> Unit = {},
    loadMarkerDetailsFunc: @Composable (
            marker: Marker,
            useFakeData: Boolean,
        ) -> LoadingState<Marker>
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isPanZoomImageDialogVisible by remember { mutableStateOf(false) }
    var currentDisplayMarker by remember(initialDisplayMarker) { mutableStateOf(initialDisplayMarker) }

    // Setup HorizontalPager
    val pagerState = rememberPagerState(
        initialPage = markers.value.indexOf(initialDisplayMarker),
    ) { markers.value.size /* provide page count */ }
    LaunchedEffect(Unit, pagerState.currentPage) {
        currentDisplayMarker = markers.value[pagerState.currentPage] // update the current marker when the user swipes
    }

    // Loading States for each marker
    val markerDetailsLoadingStates by remember {
        mutableStateOf(mutableMapOf<Int, LoadingState<Marker>>())  // PageIndex to LoadingState
    }

    // URL of the image to show in the Pan/Zoom Image Dialog
    var panZoomDialogImageUrl by remember(pagerState.currentPage) {
        mutableStateOf("")
    }

    HorizontalPager(
        modifier = Modifier,
        state = pagerState,
        userScrollEnabled = true,
        reverseLayout = false,
        contentPadding = PaddingValues(0.dp),
        beyondBoundsPageCount = 1,
        flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
        key = { page -> markers.value[page].id },
    ) { PageIndex ->
        // Load Marker Details for this page
        markerDetailsLoadingStates[PageIndex] =
            loadMarkerDetailsFunc(
                // a reactive composable
                markers.value[PageIndex],
                false,  // todo setup for automated testing
            )
        val markerDetailsLoadingState = markerDetailsLoadingStates[PageIndex]

        val scrollState = rememberScrollState()

        // Show Error (if any)
        if (markerDetailsLoadingState is LoadingState.Error) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(kMaxWeightOfBottomDrawer)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {

                Spacer(modifier = Modifier.padding(8.dp))

                // Error message
                Text(
                    (markerDetailsLoadingState as LoadingState.Error).errorMessage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colors.error,
                            shape = MaterialTheme.shapes.medium
                        ),
                    fontSize = MaterialTheme.typography.h5.fontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onError,
                )
                Spacer(modifier = Modifier.padding(16.dp))

                // OK Button
                Button(
                    onClick = {
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(
                            MaterialTheme.colors.primary,
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Text(
                        "OK",
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        fontSize = MaterialTheme.typography.h6.fontSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.onPrimary,
                    )
                }
            }

            return@HorizontalPager
        }

        // Show Loading
        if (markerDetailsLoadingState is LoadingState.Loading) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(kMaxWeightOfBottomDrawer)
                    .padding(start = 16.dp, end = 16.dp, bottom = 0.dp, top = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
            ) {

                // Title & Close Button
                // Subtitle & Speak Button
                TitleCloseSubtitleSpeakSection(currentDisplayMarker, onDismiss)

                // ID, Navigate to Marker, Locate on Map, Speak
                MarkerIdWithNavigateLocateSpeakActionButtonSection(
                    currentDisplayMarker,
                    isTextToSpeechCurrentlySpeaking,
                    coroutineScope,
                    onLocateMarkerOnMap,
                    onClickStartSpeakingMarker,
                    onDismiss
                )
                Spacer(modifier = Modifier.padding(16.dp))

                Text(
                    "Loading Details...",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = MaterialTheme.typography.h6.fontSize,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onBackground,
                )
                Spacer(modifier = Modifier.padding(32.dp))

                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = MaterialTheme.colors.onBackground,
                    strokeWidth = 4.dp,
                    progress = 0.5f
                )
            }

            return@HorizontalPager
        }

        // Show Loaded Marker Details
        AnimatedVisibility(
            markerDetailsLoadingState is LoadingState.Loaded<Marker>,
            enter = fadeIn(TweenSpec(1000)),
            exit = fadeOut(TweenSpec(1000))
        ) {
            val loadedMarker = markerDetailsLoadingState as LoadingState.Loaded<Marker>
            val displayMarker = loadedMarker.data

            val painterResource: Resource<Painter> =
                asyncPainterResource(
                    data = displayMarker.mainPhotoUrl,
                    key = displayMarker.id,
                    filterQuality = FilterQuality.Medium,
                )

            // Marker Details
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(kMaxWeightOfBottomDrawer) // lets some map show through
                    .padding(start = 16.dp, end = 16.dp, bottom = 0.dp, top = 0.dp)
            ) {
                // Title & Close Button
                // Subtitle & Speak Button
                TitleCloseSubtitleSpeakSection(displayMarker, onDismiss)

                // ID, Navigate to Marker, Locate on Map, Speak
                MarkerIdWithNavigateLocateSpeakActionButtonSection(
                    displayMarker,
                    isTextToSpeechCurrentlySpeaking,
                    coroutineScope,
                    onLocateMarkerOnMap,
                    onClickStartSpeakingMarker,
                    onDismiss
                )
                Spacer(modifier = Modifier.padding(4.dp))

                // Marker Info Content
                Column(
                    Modifier
                        .weight(4f)
                        .verticalScroll(
                            scrollState,
                            enabled = true,
                        ),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Top,
                ) {

                    // Main Photo
                    if (!LocalInspectionMode.current) {
                        if (displayMarker.mainPhotoUrl.isNotEmpty()) {
                            Surface(
                                modifier = Modifier.background(
                                    MaterialTheme.colors.background,
                                    shape = MaterialTheme.shapes.medium,
                                )
                            ) {
                                var isFinishedLoading by remember {
                                    mutableStateOf(false)
                                }

                                BoxWithConstraints(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1280f / 959f)
                                        .background(
                                            MaterialTheme.colors.surface,
                                            shape = MaterialTheme.shapes.medium
                                        )
                                ) {

                                    KamelImageBox(
                                        resource = painterResource,
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        onLoading = { progress ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize(),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                if (progress < 0.05f) {
                                                    Text(
                                                        "Loading marker image...",
                                                        color = MaterialTheme.colors.onSurface,
                                                    )
                                                } else {
                                                    CircularProgressIndicator(
                                                        progress,
                                                        color = MaterialTheme.colors.onSurface,
                                                        backgroundColor = MaterialTheme.colors.onSurface.copy(
                                                            alpha = 0.4f
                                                        ),
                                                    )
                                                }

                                                // Prevent flicker of progress indicator (ugh)
                                                if (progress > 0.85f) {
                                                    isFinishedLoading = true
                                                }
                                            }
                                        },
                                        onFailure = { exception: Throwable ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = "Image loading error: " + exception.message.toString(),
                                                    duration = SnackbarDuration.Long
                                                )
                                            }
                                        },
                                        animationSpec = if (isFinishedLoading)
                                            TweenSpec(800)
                                        else
                                            null,
                                        onSuccess = { painter ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(MaterialTheme.shapes.medium)
                                            ) {
                                                Image(
                                                    painter,
                                                    displayMarker.title,
                                                    contentScale = ContentScale.Crop,
                                                    alignment = Alignment.Center,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                        }
                                    )

                                    // Zoom Image Button
                                    panZoomImageButton(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp),
                                        onClick = {
                                            coroutineScope.launch {
                                                panZoomDialogImageUrl =
                                                    displayMarker.mainPhotoUrl
                                                yield()
                                                isPanZoomImageDialogVisible = true
                                            }
                                        }
                                    )
                                }
                            }
                        } else {
                            PreviewPlaceholder(
                                "No image found for this marker",
                                placeholderKind = ""
                            )
                        }
                    } else {
                        PreviewPlaceholder("Another Image")
                    }

                    // Attributions for photo
                    if (displayMarker.photoAttributions.isNotEmpty()) {
                        if (displayMarker.photoAttributions[0].isNotBlank()) {
                            Text(
                                "Photo Credit: " + displayMarker.photoAttributions[0],
                                fontSize = MaterialTheme.typography.overline.fontSize,
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.padding(8.dp))

                    // Show loading error (if any)
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(0.dp, 64.dp)
                    ) { data ->
                        Text(
                            text = data.message,
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colors.error,
                                    shape = MaterialTheme.shapes.medium
                                ),
                            color = MaterialTheme.colors.onError,
                            textAlign = TextAlign.Center

                        )
                    }

                    // Inscription
                    if (displayMarker.englishInscription.isNotBlank()) { // todo add spanish translation
                        Text(
                            displayMarker.englishInscription,
                            fontSize = MaterialTheme.typography.body2.fontSize,
                        )
                    } else {
                        Text(
                            displayMarker.inscription,
                            fontSize = MaterialTheme.typography.body2.fontSize,
                        )
                    }
                    Spacer(modifier = Modifier.padding(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.padding(8.dp))

                    // More Photos
                    displayMarker.markerPhotos.forEachIndexed { index, photoUrl ->
                        if (index > 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1280f / 959f)
                                    .background(
                                        MaterialTheme.colors.surface,
                                        shape = MaterialTheme.shapes.medium
                                    )
                            ) {
                                KamelImage(
                                    resource = asyncPainterResource(
                                        data = photoUrl,
                                        key = displayMarker.id,
                                        filterQuality = FilterQuality.Medium,
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    contentScale = ContentScale.Crop,
                                )

                                // Zoom Image Button
                                panZoomImageButton(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp),
                                    onClick = {
                                        coroutineScope.launch {
                                            panZoomDialogImageUrl = photoUrl
                                            yield()
                                            isPanZoomImageDialogVisible = true
                                        }
                                    }
                                )
                            }
                            // Caption for photo
                            if (displayMarker.photoCaptions[index].isNotBlank()) {
                                Text(
                                    displayMarker.photoCaptions[index],
                                    fontSize = MaterialTheme.typography.caption.fontSize,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            // Attributions for photo
                            if (displayMarker.photoAttributions[index].isNotBlank()) {
                                Text(
                                    "Photo Credit: " + displayMarker.photoAttributions[index],
                                    fontSize = MaterialTheme.typography.overline.fontSize,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.padding(8.dp))
                        }
                    }

                    // Erected
                    if (displayMarker.erected.isNotBlank()) {
                        Text(
                            "Erected " + displayMarker.erected,
                            fontSize = MaterialTheme.typography.body1.fontSize,
                        )
                    }

                    // Location details
                    if (displayMarker.location.isNotBlank()) {
                        Text(
                            displayMarker.location,
                            fontSize = MaterialTheme.typography.body1.fontSize,
                        )
                    } else {
                        Text(
                            "Marker Latitude: ${displayMarker.position.latitude}",
                            fontSize = MaterialTheme.typography.body1.fontSize,
                            fontWeight = FontWeight.Normal,
                        )
                        Text(
                            "Marker Longitude: ${displayMarker.position.longitude}",
                            fontSize = MaterialTheme.typography.body1.fontSize,
                            fontWeight = FontWeight.Normal,
                        )
                    }
                }

            }
        }
    }

    // Show Pan/Zoom Image Dialog
    if (isPanZoomImageDialogVisible) {
        PanZoomImageDialog(
            onDismiss = {
                coroutineScope.launch {
                    isPanZoomImageDialogVisible = false
                }
            },
            imageUrl = panZoomDialogImageUrl
        )
    }
}


@Composable
private fun TitleCloseSubtitleSpeakSection(
    marker: Marker,
    onDismiss: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .padding(bottom = 0.dp, top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Title & Subtitle
        Column(
            modifier = Modifier
                .weight(8f)
                .padding(bottom = 0.dp, top = 4.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
        ) {
            // Title
            Text(
                marker.title,
                modifier = Modifier
                    .padding(bottom = 0.dp, top = 4.dp),
                fontSize = MaterialTheme.typography.h5.fontSize,
                fontWeight = FontWeight.Bold,
            )
            // Subtitle
            Text(
                marker.subtitle,
                modifier = Modifier
                    .padding(top = 0.dp, bottom = 4.dp),
                fontSize = MaterialTheme.typography.h6.fontSize,
                fontWeight = FontWeight.Normal,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.padding(4.dp))
        }

        // Close Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Top,
        ) {
            // Close Button
            IconButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(12.dp, (-8).dp),
                onClick = {
                    onDismiss()
                },
            ) {
                Icon(
                    modifier = Modifier
                        .padding(4.dp),
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                )
            }
        }
    }
}

// ID, Navigate to Marker, Locate on Map, Speak
@Composable
private fun MarkerIdWithNavigateLocateSpeakActionButtonSection(
    marker: Marker,
    isTextToSpeechCurrentlySpeaking: Boolean,
    coroutineScope: CoroutineScope,
    onLocateMarkerOnMap: (Marker) -> Unit,
    onClickStartSpeakingMarker: (Marker) -> Unit,
    onDismiss: () -> Unit = {},
) {
    Row(
        modifier = Modifier.heightIn(0.dp, 36.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,

        ) {
        // Marker Id
        Text(
            "ID: ${marker.id}",
            modifier = Modifier.weight(2f),
            fontSize = MaterialTheme.typography.body1.fontSize,
            fontWeight = FontWeight.Bold,
        )

        // Navigate to Marker Button
        IconButton(
            modifier = Modifier.weight(.5f),
            onClick = {
                // close the bottom sheet
                coroutineScope.launch {
                    onDismiss()
                    onLocateMarkerOnMap(marker)
                    openNavigationAction(
                        marker.position.latitude,
                        marker.position.longitude,
                        marker.title
                    )
                }
            },
            enabled = true,
        ) {
            Icon(
                imageVector = Icons.Filled.Navigation,
                contentDescription = "Navigate to Marker",
                tint = MaterialTheme.colors.onBackground,
            )
        }

        // Locate on Map Button
        IconButton(
            modifier = Modifier.weight(.5f),
            onClick = {
                // close the bottom sheet
                coroutineScope.launch {
                    onDismiss()
                    onLocateMarkerOnMap(marker)
                }
            },
            enabled = true,
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = "Find Marker on Map",
                tint = MaterialTheme.colors.onBackground,
            )
        }

        // Speak Button
        IconButton(
            modifier = Modifier.weight(.5f),
            onClick = {
                if (isTextToSpeechCurrentlySpeaking)
                    stopTextToSpeech()
                else {
                    onClickStartSpeakingMarker(marker)
                }
            },
            enabled = true,
        ) {
            if (isTextToSpeechCurrentlySpeaking) {
                Icon(
                    imageVector = Icons.Filled.Stop,
                    contentDescription = "Stop Speaking Marker",
                    tint = MaterialTheme.colors.onBackground,
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.VolumeUp,
                    contentDescription = "Speak Marker",
                    tint = MaterialTheme.colors.onBackground,
                )
            }
        }
    }
}

@Composable
private fun panZoomImageButton(
    modifier : Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .padding(4.dp)
            .background(
                MaterialTheme.colors.surface.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.medium
            )
    ) {
        Icon(
            imageVector = Icons.Default.ZoomIn,
            contentDescription = "Zoom In",
            modifier = Modifier
                .size(36.dp)
                .alpha(0.8f)
        )
    }
}

@Composable
fun PanZoomImageDialog(
    onDismiss: () -> Unit = {},
    imageUrl: String = "",
) {
    val kMaxZoomInFactor = 5f

    val painterResource: Resource<Painter> =
        asyncPainterResource(
            data = imageUrl,
            filterQuality = FilterQuality.Medium,
        )

    var scale by remember {
        mutableStateOf(1f)
    }
    var offset by remember {
        mutableStateOf(Offset.Zero)
    }
    //    var rotationZ by remember {
    //        mutableStateOf(0f)
    //    }
    var isFinishedLoading by remember {
        mutableStateOf(false)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colors.surface,
                shape = MaterialTheme.shapes.medium
            )
    ) {

        Dialog(
            properties =
                DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true,
                    usePlatformDefaultWidth = false,
                ),
            onDismissRequest = {
                onDismiss()
            },
        ) {

            // Setup pan/zoom (not rotation) transformable state
            @Suppress("UNUSED_ANONYMOUS_PARAMETER")
            val state =
                rememberTransformableState { zoomChange, panChange, rotationChange ->
                    scale = (scale * zoomChange).coerceIn(1f, kMaxZoomInFactor)

                    val extraWidth = (scale - 1) * constraints.maxWidth
                    val extraHeight = (scale - 1) * constraints.maxHeight

                    val maxX = extraWidth  / 2
                    val maxY = extraHeight / 2

                    offset = Offset(
                        x = (offset.x + scale * panChange.x).coerceIn(-maxX, maxX),
                        y = (offset.y + scale * panChange.y).coerceIn(-maxY, maxY)
                    )

                    // rotationZ += rotationChange
                }
            Box {
                KamelImage(
                    resource = painterResource,
                    contentDescription = "Marker Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                            // this.rotationZ = rotationZ  // allow rotation?
                        }
                        .transformable(
                            state,
                            lockRotationOnZoomPan = true
                        ),
                    contentScale = ContentScale.Fit,
                    onLoading = { progress ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (progress < 0.05f) {
                                Text(
                                    "Loading marker image...",
                                    color = MaterialTheme.colors.onSurface,
                                )
                            } else {
                                CircularProgressIndicator(
                                    progress,
                                    color = MaterialTheme.colors.onSurface,
                                    backgroundColor = MaterialTheme.colors.onSurface.copy(
                                        alpha = 0.4f
                                    ),
                                )
                            }

                            // Prevent flicker of progress indicator (ugh)
                            if (progress > 0.85f) {
                                isFinishedLoading = true
                            }
                        }
                    },
                    onFailure = { exception: Throwable ->
                        Text(
                            "Image loading error: " + exception.message.toString(),
                            color = MaterialTheme.colors.error,
                        )
                    },
                )

                // Hint Text
                Text(
                    "Pinch to zoom, drag to pan",
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            MaterialTheme.colors.surface.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.medium
                        )
                )

                // Close Button
                IconButton(
                    onClick = {
                        onDismiss()
                    },
                    enabled = true,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            MaterialTheme.colors.surface.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.alpha(0.8f)
                    )
                }
            }

        }
    }
}


