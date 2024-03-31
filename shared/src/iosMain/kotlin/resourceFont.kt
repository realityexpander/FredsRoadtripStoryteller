
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes

private val cache: MutableMap<String, Font> = mutableMapOf()

@OptIn(ExperimentalResourceApi::class, InternalResourceApi::class)
@Composable
actual fun resourceFont(
    name: String,
    res: String,
    weight: FontWeight,
    style: FontStyle
): Font {
    return cache.getOrPut(res) {
        val byteArray = runBlocking {
//            resource("font/$res.ttf").readBytes() // Resource is in `src/commonMain/resources/font/`
            readResourceBytes("font/$res.ttf")
        }
        androidx.compose.ui.text.platform.Font(res, byteArray, weight, style)
    }
}
