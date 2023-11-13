package previews

import android.content.res.Configuration
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import presentation.app.AppTheme
import data.util.LoadingState
import presentation.maps.LatLong
import presentation.maps.Marker
import presentation.MarkerDetailsScreen


@OptIn(ExperimentalMaterialApi::class)
@Preview(
    name = "Marker Details (Loaded)",
    group = "Marker Details",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false,
    showBackground = true,
)
@Composable
fun MarkerDetailsPreview(
 loadingState: LoadingState<Marker> = LoadingState.Loaded(
    Marker(
        id = "M73739",
        position = LatLong(
            latitude = 0.0,
            longitude = 0.0
        ),
        // title = "First City Council of Tepoztlan",
        title = "El Tepozteco National Park with a long title",
        alpha = 1f,
        subtitle = "Test Subtitle a very long subtitle with data n stuff and this line goes on and on and its really long and unusually long and even goes to four lines on a landscape phone mode",
        location = "Location Description",
        inscription = "Incised inscription",
        englishInscription = "This is the English inscription - Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis.\n\n et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. ",
        spanishInscription = "This is the Inscripciôn en Espanol - Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis.\n\n et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. ",
        photoAttributions = listOf("Photographed by Bilbo Baggins", "Photographed by Frodo Baggins"),
    )
 )
){
    AppTheme {
        Surface {
            MarkerDetailsScreen(
                bottomSheetScaffoldState = BottomSheetScaffoldState(
                    bottomSheetState = BottomSheetState(
                        initialValue = BottomSheetValue.Collapsed,
                        density = LocalDensity.current,
                        confirmValueChange = {
                            true
                        },
                    ),
                    drawerState = DrawerState(
                        initialValue = DrawerValue.Closed,
                        confirmStateChange = {
                            true
                        }
                    ),
                    snackbarHostState = SnackbarHostState()
                ),
                markerLoadingState = loadingState
            )
        }
    }
}

@Preview(
    name = "Marker Details (landscape)",
    group = "Marker Details - Landscape",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false,
    showBackground = true, device = "spec:parent=pixel_5,orientation=landscape",
)
@Composable
fun MarkerDetailsPreviewLandscape() {
    MarkerDetailsPreview()
}

@Preview(
    name = "Marker Details (loading error)",
    group = "Marker Details",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false,
    showBackground = true,
)
@Composable
fun MarkerDetailsPreviewError() {
    MarkerDetailsPreview(
        loadingState = LoadingState.Error(
            errorMessage = "Error loading marker"
        )
    )
}

@Preview(
    name = "Marker Details (error)",
    group = "Marker Details",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false,
    showBackground = true,
)
@Composable
fun MarkerDetailsPreviewLoading() {
    MarkerDetailsPreview(
        loadingState = LoadingState.Loading
    )
}

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

