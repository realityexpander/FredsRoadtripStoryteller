package data.billing
import data.billing.CommonBilling.Companion.kMaxTrialTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import util.CommonFlow
import util.asCommonFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

/**
 * CommonBilling is a class that is used to communicate between the shared code and the platform
 * specific code. It is used to send commands to the platform specific code and to receive
 * information from the platform specific code about the state of the billing system & the
 * product purchase.
 *
 * ```
 * @function purchaseProCommand is used to send a command to the platform specific code to purchase
 * the pro version of the app.
 * @function consumeProCommand is used to send a command to the platform specific code to consume
 * the pro version of the app. This is only used for testing purposes in this particular app.
 * @function updateState is used to update the state of the CommonBilling class.
 * @function updateMessage is used to update the message of the CommonBilling class.
 * ```
 *
 * @property _billingMessageFlow is used to send messages from the platform specific code to the shared code.
 * @property _billingStateFlow is used to send state updates from the platform specific code to the shared code.
 * @property _billingCommandFlow is used to send commands from the shared code to the platform specific code.
 */
open class CommonBilling {
    var billingState: BillingState = BillingState.NotPurchased()
        private set(value) {
            field = value
            updateState(value)
        }
    private val _billingStateFlow: MutableStateFlow<BillingState> =
        MutableStateFlow(BillingState.NotPurchased())
    val billingStateFlow: StateFlow<BillingState> =
        _billingStateFlow.asStateFlow()

    private val _billingCommandFlow: MutableSharedFlow<BillingCommand> =
        MutableSharedFlow()
    private val _billingMessageFlow =
        MutableStateFlow("")

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    sealed class BillingState {
        data class NotPurchased(val lastBillingMessage: String? = null) : BillingState()
        data object Pending : BillingState()
        data object Purchased : BillingState()
        data class Error(val errorMessage: String) : BillingState()
        data object Disabled : BillingState()
    }

    sealed class BillingCommand {
        data class Purchase(val productId: String) : BillingCommand()
        data class Consume(val productId: String) : BillingCommand()
    }

    fun purchaseProCommand() {
        coroutineScope.launch {
            _billingCommandFlow.emit(
                BillingCommand.Purchase(kProProductId)
            )
        }
    }

    // For testing purposes in this particular app. Release app doesn't need this.
    fun consumeProCommand() {
        coroutineScope.launch {
            _billingCommandFlow.emit(
                BillingCommand.Consume(kProProductId)
            )
        }
    }

    fun updateState(billingState: BillingState) {
        coroutineScope.launch {
            _billingStateFlow.emit(billingState)
        }
    }

    fun updateMessage(message: String) {
        coroutineScope.launch {
            _billingMessageFlow.emit(message)
        }
    }

    // Receive commands from the shared code
    fun commandFlow(): CommonFlow<BillingCommand> {
        return _billingCommandFlow.asCommonFlow()
    }

    // Receive billing state updates from the platform specific code
    fun billingStateFlow(): CommonFlow<BillingState> {
        return _billingStateFlow.asCommonFlow()
    }

    // Receive billing messages from the platform specific code
    fun billingMessageFlow(): CommonFlow<String> {
        return _billingMessageFlow.asCommonFlow()
    }

    companion object {
        const val kProProductId = "pro"
        val kMaxTrialTime = 3.days
    }
}

fun calcTrialTimeRemaining(
    installAtEpochMilli: Long,
    maxTrialTime: Duration = kMaxTrialTime
): Duration {
    val now = Clock.System.now()
    val installAt = Instant.fromEpochMilliseconds(installAtEpochMilli)
    val timeLeft = maxTrialTime - (now - installAt)

    return timeLeft
}

fun calcTrialTimeRemainingString(
    installAtEpochMilli: Long,
    maxTrialTime: Duration = kMaxTrialTime
): String {
    val timeLeft = calcTrialTimeRemaining(installAtEpochMilli, maxTrialTime)
    if(timeLeft <= Duration.ZERO) return "Trial expired - Please purchase Pro version for unlimited features"

    return timeLeft.toHumanReadableString() + " remaining for Trial version"
}

fun Duration.toHumanReadableString(): String {
    val days = this.inWholeDays
    val hours = this.inWholeHours - (days * 24)
    val minutes = this.inWholeMinutes - (days * 24 * 60) - (hours * 60)
    val seconds = this.inWholeSeconds - (days * 24 * 60 * 60) - (hours * 60 * 60) - (minutes * 60)

    return when {
        days > 0 -> "$days days, $hours hours, $minutes minutes"
        hours > 0 -> "$hours hours, $minutes minutes"
        minutes > 0 -> "$minutes minutes"
        else -> "$seconds seconds"
    }
}
