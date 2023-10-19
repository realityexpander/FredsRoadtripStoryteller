import com.google.firebase.appdistribution.ktx.appDistribution
import com.google.firebase.ktx.Firebase

actual fun triggerFirebaseFeedback() {
    Firebase.appDistribution.startFeedback("We value your feedback!")
}
