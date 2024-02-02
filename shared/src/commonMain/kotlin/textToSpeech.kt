import presentation.speech.CommonSpeech

var iosCommonSpeech: CommonSpeech = CommonSpeech() // Only needed here for iOS
var unspokenText: String? = null // used to speak text in chunks // todo: move to CommonSpeech

expect fun speakTextToSpeech(text: String)
expect fun isTextToSpeechSpeaking(): Boolean
expect fun stopTextToSpeech()
expect fun pauseTextToSpeech()
