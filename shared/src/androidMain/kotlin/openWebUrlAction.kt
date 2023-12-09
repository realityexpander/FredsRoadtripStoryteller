import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Android
actual fun openWebUrlAction(urlStr: String) {
    CoroutineScope(Dispatchers.Main).launch {
        _intentFlow.emit(
            Intent(Intent.ACTION_VIEW, Uri.parse(urlStr))
        )
    }
}
