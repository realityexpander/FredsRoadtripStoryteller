
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import data.billing.CommonBilling
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

actual fun getPlatformName(): String = "Android"

@Composable fun MainView(
    commonBilling: CommonBilling,
    appMetadata: CommonAppMetadata
) =
    App(commonBilling, appMetadata)

lateinit var appContext: Context // Android specific context

// Android specific intent flow
@Suppress("ObjectPropertyName")
var _androidIntentFlow: MutableSharedFlow<Intent> = MutableSharedFlow()
val androidIntentFlow: SharedFlow<Intent> = _androidIntentFlow  // read-only shared flow received on Android side
