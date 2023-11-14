import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.datetime.Clock

actual fun getPlatformName(): String = "Android"

@Composable fun MainView() = App()

lateinit var appContext: Context // Android specific context
var tts: TextToSpeech? = null  // Android specific TextToSpeech

// Android specific intent flow
@Suppress("ObjectPropertyName")
var _intentFlow: MutableSharedFlow<Intent> = MutableSharedFlow()
val intentFlow: SharedFlow<Intent> = _intentFlow  // read-only shared flow received on Android side

//var debugLog = mutableListOf("Android debug log: start time:" + Clock.System.now())
