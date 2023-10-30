import android.speech.tts.TextToSpeech
import android.util.Log

actual fun ttsSpeak(text: String) {
    tts ?: Log.w("ttsSpeak", "tts is null")

    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
}
actual fun isTTSSpeaking(): Boolean {
    return tts?.isSpeaking ?: false
}
