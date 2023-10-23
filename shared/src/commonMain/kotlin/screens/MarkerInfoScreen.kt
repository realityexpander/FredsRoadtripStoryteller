import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
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
import androidx.compose.ui.unit.dp
import io.kamel.core.Resource
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import components.PreviewPlaceholder

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
        data = "https://cdn.pixabay.com/photo/2020/06/13/17/51/milky-way-5295160_1280.jpg",
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
            .padding(start=16.dp, end=16.dp, bottom = 0.dp, top = 0.dp)
            .scrollable(scrollState, orientation = Orientation.Vertical)
        ,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
    ) {
        // Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
            ,
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                marker.title,
                modifier = Modifier
                    .weight(3f),
                fontSize = MaterialTheme.typography.h5.fontSize,
                fontWeight = FontWeight.Bold,
            )
            IconButton(
                modifier = Modifier
                    .offset(16.dp, 0.dp)
                ,
                onClick = {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.padding(bottom = 0.dp, top = 0.dp)
                )
            }
        }

        Text(
            marker.subtitle,
                modifier = Modifier.offset(0.dp, (-8).dp),  // Cant get rid of the padding
            fontSize = MaterialTheme.typography.h6.fontSize,
            fontWeight = FontWeight.Normal,
        )

        if(LocalInspectionMode.current) {
            PreviewPlaceholder("Another Image")
        } else {
            KamelImage(
                resource = painterResource,
                contentDescription = null,
                modifier = Modifier.aspectRatio(16f / 9f),
                contentScale = ContentScale.Crop,
                onLoading = { CircularProgressIndicator(it) },
                onFailure = { exception: Throwable ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = exception.message.toString(),
                            actionLabel = "Hide",
                        )
                    }
                },
            )
        }

        Spacer(modifier = Modifier.padding(8.dp))
        Divider()
        Spacer(modifier = Modifier.padding(8.dp))

        if(LocalInspectionMode.current) {
            PreviewPlaceholder("Freds head")
        } else {
            Image(
                painter = painterResource("fred-head-owl-1.png"),
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth,
            )
        }

        Text(
            "Marker Latitude: ${marker.position.latitude}",
            fontSize = MaterialTheme.typography.body1.fontSize,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Marker Longitude: ${marker.position.longitude}",
            fontSize = MaterialTheme.typography.body1.fontSize,
            fontWeight = FontWeight.Bold,
        )
    }
}



