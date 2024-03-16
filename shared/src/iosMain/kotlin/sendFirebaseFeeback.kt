
actual fun sendDeveloperFeedback() {

    sendEmailAction(
        to = "fredsroadtripstoryteller@mail.com",
        subject = "${appMetadata.appNameStr} ${appMetadata.versionStr} Feedback",
        body = "Please provide feedback for ${appMetadata.appNameStr} ${appMetadata.versionStr}-" +
                "build-${appMetadata.iOSBundleVersionStr} (iOS) via your email app:"
    )
}
