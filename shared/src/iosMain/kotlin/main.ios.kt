import androidx.compose.ui.window.ComposeUIViewController

actual fun getPlatformName(): String = "iOS"

fun MainViewController(
    commonBilling: CommonBilling,
    commonAppMetadata: CommonAppMetadata
) =
    ComposeUIViewController {
        App(commonBilling, commonAppMetadata)
    }
