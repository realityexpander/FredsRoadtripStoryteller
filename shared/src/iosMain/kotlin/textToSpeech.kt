import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechUtterance
import platform.darwin.NSObject

//// must create an instance at runtime, cant use object here (!) https://github.com/JetBrains/kotlin-native/issues/3855
class TextToSpeechManager : NSObject(), AVSpeechSynthesizerDelegateProtocol {
    private var synthesizer: AVSpeechSynthesizer = AVSpeechSynthesizer()
    var isSpeaking = false

    init {
        synthesizer.delegate = this
    }

    fun speak(text: String) {
        isSpeaking = true
        synthesizer.delegate = this
        val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
        utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage("en-US")
        println("speakTextToSpeech: $text")
        synthesizer.speakUtterance(utterance)
    }

    override fun speechSynthesizer(
        synthesizer: AVSpeechSynthesizer,
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") //https://youtrack.jetbrains.com/issue/KT-43791/cocoapods-generated-code-with-same-parameter-types-and-order-but-different-names#focus=Comments-27-4574011.0-0
        didFinishSpeechUtterance: AVSpeechUtterance
    ) {
        isSpeaking = false
    }

    fun stopSpeaking() {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
    }

    fun isSpeaking(): Boolean {
        return isSpeaking
    }
}

var ttsm: TextToSpeechManager = TextToSpeechManager()

actual fun speakTextToSpeech(text: String) {  // gives runtime error: [catalog] Unable to list voice folder
    ttsm.speak(text) // Cant use this from Kotlin due to unresolved Build Error
}
actual fun stopTextToSpeech() {
    ttsm.stopSpeaking()
}
actual fun isTextToSpeechSpeaking(): Boolean {
    return ttsm.isSpeaking
}

//actual fun speakTextToSpeech(text: String) {
//    iosCommonSpeech.speakText(text)
//}
//actual fun isTextToSpeechSpeaking(): Boolean =
//    iosCommonSpeech.isTextToSpeechSpeaking()
//actual fun stopTextToSpeech() {
//    iosCommonSpeech.stopTextToSpeech()
//}
