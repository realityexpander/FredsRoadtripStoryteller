package data.billing

import data.AppSettings
import data.loadMarkers.distanceBetweenInMiles
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import presentation.maps.Location
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * # Trial/Pro version handling
 *
 * ## Trial version
 * - Trial starts when user is outside the `kTrialStartOutsideRadiusMiles` of the app install location.
 * - Trial duration is `kTrialPeriodDuration` days.
 * - After trial ends, the following features are disabled:
 *   - Can't view the details of the markers.
 *   - Can't speak the details of the markers.
 *   - Can't announce the details of the markers.
 *
 * ## Pro version
 * - Pro version is enabled if the user has purchased the Pro version.
 * - Pro version features:
 *   - Unlimited access to all features.
 */

val kTrialPeriodDuration = 3.minutes
const val kTrialStartOutsideRadiusMiles = 1.0  // Distance from install location to start trial.

// Check if user location is outside the trial radius & start trial.
fun AppSettings.isTrialStartDetected(
    userLocation: Location,
): Boolean {  // `true` means it started the trial.
    if(isTrialStarted()) return false // Trial has already started.

    // Set the install location if not set yet.
    if(installAtLocation.latitude == 0.0 && installAtLocation.longitude == 0.0) {
        installAtLocation = userLocation
        return false
    }

    // Check if user is outside the trial radius.
    if (distanceBetweenInMiles(
            installAtLocation.latitude, installAtLocation.longitude,
            userLocation.latitude, userLocation.longitude
        ) > kTrialStartOutsideRadiusMiles
    ) {
        trialStartAtEpochMilli = Clock.System.now().toEpochMilliseconds()
        return true  // Trial started.
    }

    return false
}

fun AppSettings.isTrialStarted() = trialStartAtEpochMilli != 0L
fun AppSettings.isTrialEnded(): Boolean {
    if(!isTrialStarted()) return false  // Trial has not started yet.

    // Check if trial has ended.
    return trialStartAtEpochMilli != 0L &&
            (Clock.System.now().toEpochMilliseconds() >
                trialStartAtEpochMilli + kTrialPeriodDuration.inWholeMilliseconds)
}

fun AppSettings.isProVersionEnabled(billingState: CommonBilling.BillingState): Boolean {
    if(!isTrialEnded()) return true // Use Pro features if still in trial.

    return billingState is CommonBilling.BillingState.Purchased
}

fun AppSettings.calcTrialTimeRemainingString(): String {
    return calcTrialTimeRemainingString(trialStartAtEpochMilli)
}

private fun calcTrialTimeRemainingString(
    trialStartAtEpochMilli: Long,
    maxTrialTime: Duration = kTrialPeriodDuration
): String {
    if(trialStartAtEpochMilli == 0L)
        return "Trial has not started yet. " +
            "Trial starts when you are $kTrialStartOutsideRadiusMiles miles away from the app install location."

    val timeLeft = calcTrialTimeRemaining(trialStartAtEpochMilli, maxTrialTime)
    if(timeLeft <= Duration.ZERO) return "Trial expired - Please purchase Pro version for unlimited features."

    return timeLeft.toHumanReadableString() + " remaining for Trial version."
}
private fun calcTrialTimeRemaining(
    installAtEpochMilli: Long,
    maxTrialTime: Duration = kTrialPeriodDuration
): Duration {
    val now = Clock.System.now()
    val installAt = Instant.fromEpochMilliseconds(installAtEpochMilli)
    @Suppress("RedundantSuppression")  // I want to show explicit variable name here.
    val timeLeft = maxTrialTime - (now - installAt)

    return timeLeft
}

fun Duration.toHumanReadableString(): String {
    val days = this.inWholeDays
    val hours = this.inWholeHours - (days * 24)
    val minutes = this.inWholeMinutes - (days * 24 * 60) - (hours * 60)
    val seconds = this.inWholeSeconds - (days * 24 * 60 * 60) - (hours * 60 * 60) - (minutes * 60)

    return when {
        days > 0 -> "$days days, $hours hours, $minutes minutes"
        hours > 0 -> "$hours hours, $minutes minutes"
        minutes > 0 -> "$minutes minutes" + if(minutes < 2) ", $seconds seconds" else ""
        else -> "$seconds seconds"
    }
}
