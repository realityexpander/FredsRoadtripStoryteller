
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

const val kProProductId = "pro" // only supports one product for now

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
}

fun <T> Flow<T>.asCommonFlow(): CommonFlow<T> = CommonFlow(this)
class CommonFlow<T>(private val origin: Flow<T>) : Flow<T> by origin {
    fun watch(block: (T) -> Unit): Closeable {
        val job = Job()

        onEach {
            block(it)
        }.launchIn(CoroutineScope(Dispatchers.Main + job))

        return object : Closeable {
            override fun close() {
                job.cancel()
            }
        }
    }
}
