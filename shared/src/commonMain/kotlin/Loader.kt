import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.ktor.client.call.body
import io.ktor.client.request.get

@Composable
inline fun <reified T> load(assetUrl: String): State<LoadingState<T>> {
    val loadingState = remember { mutableStateOf<LoadingState<T>>(LoadingState.Loading()) }

    LaunchedEffect(assetUrl) {
        try {
            val response = httpClient.get(assetUrl)
            val data: T = response.body()
            loadingState.value = LoadingState.Loaded(data)
        } catch (e: Exception) {
            loadingState.value = LoadingState.Error(e.cause?.message ?: "error")
        }
    }

    return loadingState
}

