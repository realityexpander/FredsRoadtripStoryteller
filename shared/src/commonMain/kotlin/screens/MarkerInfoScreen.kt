import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.kamel.core.Resource
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterialApi::class, ExperimentalResourceApi::class)
@Composable
fun MarkerInfoScreen(
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    marker: MapMarker,
    // onShouldResetMarkerInfoCache: (() -> Unit) = {}
) {
    val scrollState = rememberScrollState()
//    var isResetCacheAlertDialogVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

//    var shouldStartTrackingAutomaticallyWhenAppLaunches by remember {
//        mutableStateOf(settings?.shouldAutomaticallyStartTrackingWhenAppLaunches() ?: false)
//    }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val painterResource: Resource<Painter> = asyncPainterResource(
//        data.images.first { it.orientation == Orientation.LANDSCAPE }.link,
//        data = "https://cdn.pixabay.com/photo/2020/06/13/17/51/milky-way-5295160_1280.jpg",
        data = "https://pixabay.com/get/g97fa90c2a6b16111677e7d80c1672ba85249d372ce7050d9d46bdb472ccbc48a7a245727102f1dca3faff7de3b00948e5fb00ad90d632225449f0029735f945b2aab206f521637b7cb9e0416af41819d_640.jpg",
        filterQuality = FilterQuality.Medium,
    )

//    val painterResource2: Painter = painterResource(
////            res = "./src/main/resources/cat-2536662_640.jpg"
////            res = "./shared/src/commonMain/resources/cat-2536662_640.jpg"
////            res = "./src/commonMain/resources/cat-2536662_640.jpg"
////            res = "src/commonMain/resources/cat-2536662_640.jpg"
////            res = "commonMain/resources/cat-2536662_640.jpg"
////            res = "resources/cat-2536662_640.jpg"
//            res = "cat-2536662_640.jpg"
//        )


    Column(
        Modifier.fillMaxWidth()
            .padding(16.dp)
            .scrollable(scrollState, orientation = Orientation.Vertical),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        Row {
            Text(
                "Marker Info",
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
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        Column {
            Text(
                "Marker Name: ${marker.title}",
                fontSize = MaterialTheme.typography.h6.fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
//                    .weight(1f)
            )
            Text(
                "Marker Subtitle: ${marker.subtitle}",
                fontSize = MaterialTheme.typography.subtitle1.fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
//                    .weight(1f)
            )
            Text(
                "Marker Latitude: ${marker.position.latitude}",
                fontSize = MaterialTheme.typography.body1.fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
//                    .weight(1f)
            )
            Text(
                "Marker Longitude: ${marker.position.longitude}",
                fontSize = MaterialTheme.typography.body1.fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
//                    .weight(1f)
            )
//            KamelImage(
//                resource = painterResource,
//                contentDescription = null,
//                modifier = Modifier.aspectRatio(16f / 9f),
//                contentScale = ContentScale.Crop,
//                onLoading = { CircularProgressIndicator(it) },
//                onFailure = { exception: Throwable ->
//                    scope.launch {
//                        snackbarHostState.showSnackbar(
//                            message = exception.message.toString(),
//                            actionLabel = "Hide",
//                        )
//                    }
//                },
//            )
            if(!LocalInspectionMode.current) {
                Image(
//                painter = painterResource("compose-multiplatform.xml"),
                    painter = painterResource("cat-2536662_640.jpg"),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
//                        .aspectRatio(16f / 9f)
                    ,
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(MaterialTheme.colors.background)
                    ,
                    contentAlignment = Alignment.Center,

                ) {
                    Text("Picture Here",
                        fontSize = MaterialTheme.typography.h6.fontSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.onBackground,
                        textAlign = TextAlign.Center,
                    )
                }
            }

        }
    }
}
