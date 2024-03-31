package previews

import android.content.res.Configuration
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.ExperimentalResourceApi
import presentation.app.onboarding.OnboardingDialog
import presentation.uiComponents.AppTheme

@OptIn(ExperimentalResourceApi::class)
@Preview(
    name = "Onboarding",
    group = "Light",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false,
    showBackground = true,
)
@Composable
fun OnboardingPreview() {
    AppTheme {
        Surface {
            OnboardingDialog()
        }
    }
}
