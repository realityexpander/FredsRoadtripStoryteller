import io.ktor.client.HttpClient
import io.ktor.client.plugins.Charsets
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.charsets.Charsets
import kotlinx.serialization.json.Json

val httpClient = HttpClient {
    developmentMode = true

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    install(UserAgent) {
        agent = "Historical Crawler(1.0ß1)"
    }

    Charsets {
        // Marker page html response uses "windows-1252" encoding (aka "ISO-8859-1")
        // - This responseCharsetFallback is required for iOS (Android doesn't care)
        responseCharsetFallback = Charsets.ISO_8859_1
        sendCharset = Charsets.ISO_8859_1
    }

    expectSuccess = true
    followRedirects = true
}
