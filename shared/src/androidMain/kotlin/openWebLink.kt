import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.NoLiveLiterals
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Android
actual fun openWebLink(url: String) {
    CoroutineScope(Dispatchers.Main).launch {
        _intentFlow.emit(
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        )
    }
}


@NoLiveLiterals
actual fun sendEmailAction(
    to: String,
    subject: String,
    body: String
) {
    // Replacement for:
    // """mailto:realityexpanderdev@gmail.com?subject=Fred's Markers Debug Log""" +
    // "&body=" + json.encodeToString(debugLog.joinToString(separator = "\n")))

    CoroutineScope(Dispatchers.Main).launch {
        _intentFlow.emit(
            Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
            }
        )
    }
}
