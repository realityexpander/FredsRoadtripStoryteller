import android.speech.tts.TextToSpeech
import android.util.Log

actual fun speakTextToSpeech(text: String) {
    tts ?: Log.w("ttsSpeak", "tts is null")

    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
}
actual fun isTextToSpeechSpeaking(): Boolean {
    return tts?.isSpeaking ?: false
}

actual fun stopTextToSpeech() {
    tts?.stop()
}
