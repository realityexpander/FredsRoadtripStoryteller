
actual fun sendDeveloperFeedback() {

    sendEmailAction(
        to = "realityexpanderdev@gmail.com",
        subject = "$appNameStr ${appMetadata.versionStr} Feedback",
        body = "Please provide feedback for $appNameStr ${appMetadata.versionStr}-" +
                "build-${appMetadata.iOSBundleVersionStr} via your email app:"
    )
}
