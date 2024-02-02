import platform.AVFAudio.AVSpeechBoundary
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechSynthesizerDelegateProtocol
import platform.AVFAudio.AVSpeechUtterance
import platform.NaturalLanguage.NLLanguageRecognizer
import platform.darwin.NSObject

// Implementation #1 - uses `TextToSpeechManager` natively in Kotlin
var textToSpeechManager: TextToSpeechManager = TextToSpeechManager()
actual fun speakTextToSpeech(text: String) {  // gives runtime error: [catalog] Unable to list voice folder
    textToSpeechManager.speak(text)
}
actual fun stopTextToSpeech() {
    textToSpeechManager.stopSpeaking()
    unspokenText = ""
}
actual fun isTextToSpeechSpeaking(): Boolean {
    return textToSpeechManager.isSpeaking
}
actual fun pauseTextToSpeech() {
    textToSpeechManager.pauseSpeaking()
}

// Implementation #1
// Kotlin-native Implementation of iOS Text-to-Speech
// Developers Note: must create an instance at runtime, cant use object here (!) https://github.com/JetBrains/kotlin-native/issues/3855
class TextToSpeechManager : NSObject(), AVSpeechSynthesizerDelegateProtocol {
    private var synthesizer: AVSpeechSynthesizer = AVSpeechSynthesizer()
    var isSpeaking = false
    var isPaused = false

    init {
        synthesizer.delegate = this
    }

    fun speak(text: String) {
        // Resume speaking if there is unspoken text
        if(!isSpeaking && isPaused) {
            continueSpeaking()
        }

        val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
        utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage("en-US") // default to english

        // Detect language
        val recognizer = NLLanguageRecognizer()
        recognizer.processString(text)
        val language = recognizer.dominantLanguage
        language ?: run {
            utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage(language)
        }

        isPaused = false
        isSpeaking = true
        synthesizer.speakUtterance(utterance)
    }

    override fun speechSynthesizer(
        synthesizer: AVSpeechSynthesizer,
        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") //https://youtrack.jetbrains.com/issue/KT-43791/cocoapods-generated-code-with-same-parameter-types-and-order-but-different-names#focus=Comments-27-4574011.0-0
        didFinishSpeechUtterance: AVSpeechUtterance
    ) {
        isSpeaking = false
    }

    // Stop speaking
    fun stopSpeaking() {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        isSpeaking = false
        isPaused = false
    }

    fun pauseSpeaking() {
        synthesizer.pauseSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
        isPaused = true
        isSpeaking = false
    }
    fun continueSpeaking() {
        synthesizer.continueSpeaking()
        isPaused = false
        isSpeaking = true
    }

    fun isSpeaking(): Boolean {
        return isSpeaking
    }
}

//// Implementation #2 - uses `presentation.speech.CommonSpeech` as a bridge to `TextToSpeechManager` in Swift
////  Sends commands to Swift Implementation via presentation.speech.CommonSpeech class
//actual fun speakTextToSpeech(text: String) {
//    iosCommonSpeech.speakText(text)
//}
//actual fun isTextToSpeechSpeaking(): Boolean =
//    iosCommonSpeech.isTextToSpeechSpeaking()
//actual fun stopTextToSpeech() {
//    iosCommonSpeech.stopTextToSpeech()
//}
