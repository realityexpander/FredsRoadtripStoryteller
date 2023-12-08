import androidx.compose.ui.window.ComposeUIViewController

actual fun getPlatformName(): String = "iOS"

fun MainViewController(commonBilling: CommonBilling) =
    ComposeUIViewController {
        App(commonBilling)
    }
