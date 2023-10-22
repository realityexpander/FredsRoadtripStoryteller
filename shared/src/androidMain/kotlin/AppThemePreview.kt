import android.content.res.Configuration
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import screens.SettingsScreen

@OptIn(ExperimentalMaterialApi::class)
@Preview(showBackground = true, showSystemUi = false, backgroundColor = 0xFF8F8F8F,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun SettingsPreview() {
    AppTheme {
        SettingsScreen(
            settings = null,
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
            talkRadiusMiles = 1.0,
        )
    }
}
