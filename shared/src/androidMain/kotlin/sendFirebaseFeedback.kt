//import com.google.firebase.appdistribution.ktx.appDistribution
//import com.google.firebase.ktx.Firebase

actual fun sendDeveloperFeedback() {
    // todo try again later, doesn't work with play store yet
    //        Firebase.appDistribution.startFeedback("We value your feedback!")
    sendEmailAction(
        to = "realityexpanderdev@gmail.com",
        subject = "$appNameStr ${appMetadata.versionStr} Feedback",
        body = "Please provide feedback for $appNameStr ${appMetadata.versionStr}-" +
                "build-${appMetadata.androidBuildNumberStr} via your email app:"
    )
}
