import android.speech.tts.TextToSpeech
import android.util.Log

var textToSpeech: TextToSpeech? = null  // Android specific TextToSpeech

actual fun speakTextToSpeech(text: String) {
    textToSpeech ?: Log.w("ttsSpeak", "tts is null")
    textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "freds_markers")
}
actual fun isTextToSpeechSpeaking(): Boolean {
    return textToSpeech?.isSpeaking ?: false
}

actual fun stopTextToSpeech() {
    textToSpeech?.stop()
    unspokenText = ""
}
