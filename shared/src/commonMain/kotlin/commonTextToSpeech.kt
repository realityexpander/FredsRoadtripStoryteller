import presentation.speech.CommonSpeech

var iosCommonSpeech: CommonSpeech = CommonSpeech() // Only needed here for iOS

expect fun speakTextToSpeech(text: String)
expect fun isTextToSpeechSpeaking(): Boolean
expect fun stopTextToSpeech()
