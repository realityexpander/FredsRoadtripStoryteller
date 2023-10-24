import androidx.compose.animation.core.TweenSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import components.PreviewPlaceholder
import data.LoadingState
import io.kamel.core.Resource
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalMaterialApi::class, ExperimentalResourceApi::class)
@Composable
fun MarkerInfoScreen(
    bottomSheetScaffoldState: BottomSheetScaffoldState,
//    marker: MapMarker,
    marker: LoadingState<MapMarker>,
) {
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show Error
    if(marker is LoadingState.Error) {
        Text(
            marker.errorMessage,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.9f)
                .padding(start=16.dp, end=16.dp, bottom = 0.dp, top = 8.dp)
            ,
            fontSize = MaterialTheme.typography.h5.fontSize,
            fontWeight = FontWeight.Bold,
        )
        return
    }

    // Show Loading
    if(marker is LoadingState.Loading) {
        Text(
            "Loading...",
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.9f)
                .padding(start=16.dp, end=16.dp, bottom = 0.dp, top = 8.dp)
            ,
            fontSize = MaterialTheme.typography.h5.fontSize,
            fontWeight = FontWeight.Bold,
        )
        return
    }

    // Show Loaded Marker Info
    if(marker is LoadingState.Loaded<MapMarker>) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.9f)
                .padding(start = 16.dp, end = 16.dp, bottom = 0.dp, top = 8.dp)
        ) {
            val painterResource: Resource<Painter> = asyncPainterResource(
//        data = "https://cdn.pixabay.com/photo/2020/06/13/17/51/milky-way-5295160_1280.jpg",
//        data = "https://www.hmdb.org/Photos/7/Photo7252.jpg?11252005",
                data = marker.data.mainPhotoUrl,
                filterQuality = FilterQuality.Medium,
            )

            // Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 0.dp, top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    marker.data.title,
                    modifier = Modifier
                        .weight(3f)
                        .padding(bottom = 0.dp, top = 4.dp),
                    fontSize = MaterialTheme.typography.h5.fontSize,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(
                    modifier = Modifier
                        .offset(16.dp, (-8).dp)
                        .padding(8.dp),
                    onClick = {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                    )
                }
            }
            Text(
                marker.data.subtitle,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 0.dp, bottom = 4.dp),
                fontSize = MaterialTheme.typography.h6.fontSize,
                fontWeight = FontWeight.Normal,
            )

            Column(
                Modifier
                    .verticalScroll(
                        scrollState,
                        enabled = true,
                    ),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
            ) {

                if (LocalInspectionMode.current) {
                    PreviewPlaceholder("Another Image")
                } else {
                    if (marker.data.mainPhotoUrl.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.background(
                                MaterialTheme.colors.background,
                                shape = MaterialTheme.shapes.medium,
                            )
                        ) {
                            var scale by remember {
                                mutableStateOf(1f)
                            }
                            var offset by remember {
                                mutableStateOf(Offset.Zero)
                            }
                            //    var rotationZ by remember {
                            //        mutableStateOf(0f)
                            //    }

                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1280f / 959f)
                            ) {

                                // Setup pan/zoom (not rotation) transformable state
                                @Suppress("UNUSED_ANONYMOUS_PARAMETER")
                                val state =
                                    rememberTransformableState { zoomChange, panChange, rotationChange ->
                                        scale = (scale * zoomChange).coerceIn(1f, 2.5f)

                                        val extraWidth = (scale - 1) * constraints.maxWidth
                                        val extraHeight = (scale - 1) * constraints.maxHeight

                                        val maxX = extraWidth / 2
                                        val maxY = extraHeight / 2

                                        offset = Offset(
                                            x = (offset.x + scale * panChange.x).coerceIn(
                                                -maxX,
                                                maxX
                                            ),
                                            y = (offset.y + scale * panChange.y).coerceIn(
                                                -maxY,
                                                maxY
                                            ),
                                        )

                                        // rotationZ += rotationChange
                                    }

                                KamelImage(
                                    resource = painterResource,
                                    contentDescription = marker.data.title,
                                    modifier = Modifier
                                        // .aspectRatio(16f / 9f)
                                        .fillMaxWidth()
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            translationX = offset.x
                                            translationY = offset.y
                                            // this.rotationZ = rotationZ
                                        }
                                        .transformable(state, lockRotationOnZoomPan = true),
                                    contentScale = ContentScale.Crop,
                                    onLoading = { progress ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize(),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            if (progress < 0.05f) {
                                                Text(
                                                    "Loading image..."
                                                )
                                            } else {
                                                CircularProgressIndicator(progress)
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
                                    animationSpec = TweenSpec(500)
                                )
                            }
                        }
                    } else {
                        PreviewPlaceholder("No image found for this marker", placeholderKind = "")
                    }
                }
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

                Spacer(modifier = Modifier.padding(8.dp))
                Divider()
                Spacer(modifier = Modifier.padding(8.dp))

//            if(LocalInspectionMode.current) {
//                PreviewPlaceholder("Freds head")
//            } else {
//                Image(
//                    painter = painterResource("fred-head-owl-1.png"),
//                    contentDescription = null,
//                    modifier = Modifier.fillMaxWidth(),
//                    contentScale = ContentScale.FillWidth,
//                )
//            }

                if(marker.data.englishInscription.isNotBlank()) { // todo add spanish translation
                    Text(
                        marker.data.englishInscription,
                        fontSize = MaterialTheme.typography.body2.fontSize,
                    )
                } else {
                    Text(
                        marker.data.inscription,
                        fontSize = MaterialTheme.typography.body2.fontSize,
                    )
                }
                Spacer(modifier = Modifier.padding(8.dp))
                Divider()
                Spacer(modifier = Modifier.padding(8.dp))

                Text(
                    marker.data.erected,
                    fontSize = MaterialTheme.typography.body1.fontSize,
                )
                Text(
                    "Marker Latitude: ${marker.data.position.latitude}",
                    fontSize = MaterialTheme.typography.body1.fontSize,
                    fontWeight = FontWeight.Normal,
                )
                Text(
                    "Marker Longitude: ${marker.data.position.longitude}",
                    fontSize = MaterialTheme.typography.body1.fontSize,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
    }
}



