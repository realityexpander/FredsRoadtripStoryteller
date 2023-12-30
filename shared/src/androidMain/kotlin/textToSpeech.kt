import android.speech.tts.TextToSpeech
import android.util.Log

var androidTextToSpeech: android.speech.tts.TextToSpeech? = null  // Android specific TextToSpeech

actual fun speakTextToSpeech(text: String) {
    androidTextToSpeech ?: Log.w("ttsSpeak", "tts is null")
    androidTextToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "freds_markers")
}
actual fun isTextToSpeechSpeaking(): Boolean {
    return androidTextToSpeech?.isSpeaking ?: false
}

actual fun stopTextToSpeech() {
    androidTextToSpeech?.stop()
    unspokenText = ""
}
