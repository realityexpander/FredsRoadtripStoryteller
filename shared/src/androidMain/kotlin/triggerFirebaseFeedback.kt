import android.speech.tts.TextToSpeech
import com.google.firebase.appdistribution.ktx.appDistribution
import com.google.firebase.ktx.Firebase

actual fun triggerDeveloperFeedback() {
    Firebase.appDistribution.startFeedback("We value your feedback!")
}
