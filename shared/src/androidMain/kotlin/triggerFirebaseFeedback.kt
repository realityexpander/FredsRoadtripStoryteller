import android.speech.tts.TextToSpeech
//import com.google.firebase.appdistribution.ktx.appDistribution
import com.google.firebase.ktx.Firebase

actual fun triggerDeveloperFeedback() {
    // Only in debug builds
//    if(BuildConfig.DEBUG) {
//        Firebase.appDistribution.startFeedback("We value your feedback!")
//    }
}
