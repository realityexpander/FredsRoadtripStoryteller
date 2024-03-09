//import com.google.firebase.appdistribution.ktx.appDistribution
//import com.google.firebase.ktx.Firebase

actual fun sendDeveloperFeedback() {
    // todo try again later, doesn't work with play store yet
    //        Firebase.appDistribution.startFeedback("We value your feedback!")
    sendEmailAction(
        to = "realityexpanderdev@gmail.com",
        subject = "${appMetadata.appNameStr} ${appMetadata.versionStr} Feedback",
        body = "Please provide feedback for ${appMetadata.appNameStr} ${appMetadata.versionStr}-" +
                "build-${appMetadata.androidBuildNumberStr} (Android) via your email app:"
    )
}
