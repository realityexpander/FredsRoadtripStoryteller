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
import data.LoadingState
import maps.LatLong
import maps.Marker
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
        subtitle = "Test Subtitle a very long subtitle with data n stuff",
        location = "Location Description",
        inscription = "Incised inscription",
        englishInscription = "This is the English inscription",
        spanishInscription = "This is the Inscripciôn en Espanol",
    ))
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

