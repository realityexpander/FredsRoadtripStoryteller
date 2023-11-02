package previews

import android.content.res.Configuration
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import presentation.SplashScreenForPermissions
import presentation.app.AppTheme

@Preview(
    name = "Splash Screen",
    group = "Splash Screen",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false,
    showBackground = true,
)
@Composable
fun SplashScreenPreview() {
    AppTheme {
        Surface {
            SplashScreenForPermissions()
        }
    }
}
