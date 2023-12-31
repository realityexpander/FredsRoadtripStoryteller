import androidx.compose.ui.window.ComposeUIViewController
import data.billing.CommonBilling
import presentation.speech.CommonSpeech

actual fun getPlatformName(): String = "iOS"

fun MainViewController(
    commonBilling: CommonBilling,
    commonAppMetadata: CommonAppMetadata,
    commonSpeech: CommonSpeech
) =
    ComposeUIViewController {
        App(commonBilling, commonAppMetadata, commonSpeech)
    }
