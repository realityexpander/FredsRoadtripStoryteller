import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
@SuppressLint("DiscouragedApi") // for `getIdentifier` complaining about not using compile-time id's
actual fun resourceFont(
    name: String,
    res: String,
    weight: FontWeight,
    style: FontStyle
): Font {
    val context = LocalContext.current
    val id = context.resources.getIdentifier(res, "font", context.packageName) // Resource is in `src/commonMain/resources/font/`

    return Font(id, weight, style)
}
