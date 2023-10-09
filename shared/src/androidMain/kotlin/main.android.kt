import android.content.Context
import androidx.compose.runtime.Composable

actual fun getPlatformName(): String = "Android"

@Composable fun MainView() = App()

lateinit var appContext: Context // Android specific context
