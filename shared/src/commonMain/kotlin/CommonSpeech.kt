import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import util.CommonFlow
import util.asCommonFlow

/**
 * CommonSpeech is a class that is used to communicate between the shared code and the platform
 * specific code. It is used to send commands to the platform specific code and to receive
 * information from the platform specific code about the state of the speech system & the
 * text to speech.
 *
 * Note: I tried to implement this from the Kotlin side, but it was not working. I think it
 * is because the object was not being held and getting garbage collected.. So I implemented it in
 * swift and just use this class to communicate between the two using flows.
 *
 * ```
 * @function speakTextToSpeech sends a command to the platform specific code to speak
 * the text to speech.
 * @function isTextToSpeechSpeaking checks the state of the speech system to see if it is speaking.
 * @function updateSpeechState is used to update the state of the CommonSpeech class.
 * @function stopTextToSpeech sends a command to the platform specific code to stop
 * the text to speech.
 * ```
 */

open class CommonSpeech {

    private val _speakTextCommandFlow: MutableStateFlow<String> =
        MutableStateFlow("")
    val speakTextCommandFlow: StateFlow<String> =
        _speakTextCommandFlow.asStateFlow()

    private val _speechStateFlow: MutableStateFlow<SpeechState> =
        MutableStateFlow(SpeechState.NotSpeaking)
    val speechStateFlow: StateFlow<SpeechState> =
        _speechStateFlow.asStateFlow()
    val speechStateCommonFlow: CommonFlow<SpeechState> =
        _speechStateFlow.asCommonFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    sealed class SpeechState {
        data object NotSpeaking : SpeechState()
        data object Speaking : SpeechState()
    }

    fun speakText(text: String) {
        coroutineScope.launch {
            _speakTextCommandFlow.emit("") // stops & clears any previous/current text
            _speakTextCommandFlow.emit(text)
        }
    }

    fun isTextToSpeechSpeaking(): Boolean {
        return _speechStateFlow.value is SpeechState.Speaking
    }

    fun updateSpeechState(speechState: SpeechState) {
        coroutineScope.launch {
            _speechStateFlow.emit(speechState)

            if(speechState is SpeechState.NotSpeaking) {
                // todo needed?
            }
        }
    }

    fun stopTextToSpeech() {
        coroutineScope.launch {
            _speakTextCommandFlow.emit("")
            // _speechStateFlow.emit(SpeechState.NotSpeaking) // todo needed?
        }
    }

    // Listen for speak text commands from the platform specific code
    fun speakTextCommandCommonFlow(): CommonFlow<String> {
        return _speakTextCommandFlow.asCommonFlow()
    }

}
