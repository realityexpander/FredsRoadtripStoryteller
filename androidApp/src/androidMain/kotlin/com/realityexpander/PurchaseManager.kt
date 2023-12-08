@file:Suppress("PropertyName")

package com.realityexpander

import BillingState
import CommonBilling
import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kProProductId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import co.touchlab.kermit.Logger as Log

private const val SLOW_PENDING_TRANSACTION = 4  // Billing hidden type for slow pending transactions
/**
 * ## PurchaseManager - Android
 *
 * This class handles the purchase flow for the Android app.
 *
 * It uses the Google BillingClient to handle the purchase flow on the server side.
 * Emits messages & state to the UI via CommonBilling class.
 *
 * A purchase is considered complete when it is acknowledged.
 * ####
 *
 * ### **Purchase States:**
 *  - **`NotPurchased:`** No purchase has been made. (may have a message, such as an error from a previous purchase attempt)
 *  - **`Pending:`** A purchase has been made, but is still pending.
 *  - **`Purchased:`** A purchase has been made and is acknowledged.
 *  - **`Disabled:`** BillingClient is disabled.
 *  - **`Error:`** An error has occurred. (message to user is required)
 *
 * @param activity: Activity - the Android activity that is using this class
 * @param commonBilling: Billing - the Billing class that is used to update the UI with billing messages & state
 */
data class PurchaseManager(
    val activity: Activity,
    val commonBilling: CommonBilling = CommonBilling()
) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var billingClient: BillingClient
    private lateinit var productDetails: ProductDetails
    private lateinit var purchase: Purchase

    // Check if a new purchase is being made & if it has been attempted to check for cancellations/timeout
    private var isNewPurchaseGate1 = false // checks if a new purchase is being made, for polling purchase progress.
    private var isNewPurchaseGate2 = false // checks if a new purchase timed out.

    // Stores the product name/id
    private val _productName = MutableStateFlow(null as String?)
    val productName = _productName.asStateFlow()

    // local logger
    val logd: (msg: String) -> Unit = { msg -> Log.d(msg, tag ="PurchaseHelper") }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            when(billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (purchases != null) {
                        for (purchase in purchases) {
                            acknowledgeAndCompletePurchase(purchase)
                        }
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    coroutineScope.launch {
                        //billing.updateMessage("User Canceled Purchase Attempt")
                        logd("Purchase Canceled")
                        commonBilling.updateState(BillingState.NotPurchased())
                    }
                }
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                    coroutineScope.launch {
                        commonBilling.updateMessage("Card Declined, Code: ${billingResult.responseCode}")
                        logd("Card Declined, Response Code: ${billingResult.responseCode}")
                        commonBilling.updateState(BillingState.NotPurchased("Card Declined, Code: ${billingResult.responseCode}"))
                    }
                }
                else -> {
                    coroutineScope.launch {
                        commonBilling.updateMessage("Purchase Error, Code: ${billingResult.responseCode}")
                        logd("Purchase Error, Response Code: ${billingResult.responseCode}")
                        commonBilling.updateState(BillingState.Error("Billing Error, Code: ${billingResult.responseCode}"))
                    }
                }
            }
        }

    fun billingSetup() {
        billingClient = BillingClient.newBuilder(activity)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(
                billingResult: BillingResult
            ) {
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                    //coroutineScope.launch { // Leave for debugging
                    //    billing.updateMessage("Billing Client Connected")
                    //}
                        coroutineScope.launch {
                            queryProduct(kProProductId)
                            delay(250)
                            reloadPurchases()
                        }
                    }
                    else -> {
                        coroutineScope.launch {
                            commonBilling.updateMessage("Billing Client Connection Failure")
                        }
                        logd("Billing Client Connection Failure, Response Code: ${billingResult.responseCode}")
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                coroutineScope.launch {
                    commonBilling.updateMessage("Billing Client Connection Lost")
                    logd("Billing Client Connection Lost")
                }
            }
        })
    }

    fun queryProduct(productId: String) {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(
            queryProductDetailsParams
        ) { billingResult, productDetailsList ->
            if(billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                if (productDetailsList.isNotEmpty()) {
                    productDetails = productDetailsList[0] // only supports one product for now
                    _productName.value = productDetails.name
                    logd("Product Query Successful, found: $productDetails")

                    return@queryProductDetailsAsync
                }

                coroutineScope.launch {
                    //billing.updateMessage("No Matching Products Found")
                    logd("No Matching Products Found, Response Code: ${billingResult.responseCode}")
                }
                commonBilling.updateState(BillingState.Disabled)
                return@queryProductDetailsAsync
            }

            coroutineScope.launch {
                commonBilling.updateMessage("Product Query Failed")
                logd("Product Query Failed, Response Code: ${billingResult.responseCode}")
            }
            commonBilling.updateState(BillingState.Disabled)
        }
    }

    // Only supports one product for now
    fun makePurchase() {
        val billingFlowParams =
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                )
                .build()

        commonBilling.updateState(BillingState.Pending)
        isNewPurchaseGate1 = true
        isNewPurchaseGate2 = true
        coroutineScope.launch {
            //billing.updateMessage( "Attempting Purchase")
            logd("Attempting Purchase, ${productDetails.name}")
            pollReloadPurchase()
        }
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    // For testing purposes in this particular app. Release app doesn't need this.
    fun consumeProduct() {
        // Guard
        if(!this::purchase.isInitialized) {
            coroutineScope.launch {
                commonBilling.updateMessage("No Previous Purchase Found")
                logd("No Previous Purchase Found")
            }
            commonBilling.updateState(BillingState.NotPurchased("No Previous Purchase Found"))
            return
        }
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { billingResult, _ ->
            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    coroutineScope.launch {
                        commonBilling.updateMessage("Product Consumed")
                        logd("Product Consumed, ${purchase.products}")
                    }
                    commonBilling.updateState(BillingState.NotPurchased("Product Consumed"))
                }
                else -> {
                    coroutineScope.launch {
                        commonBilling.updateMessage("Purchase Consumption Failed")
                        logd("Purchase Consumption Failed, Response Code: ${billingResult.responseCode}")
                    }
                    commonBilling.updateState(BillingState.Error("Purchase Consumption Failed, Code: ${billingResult.responseCode}"))
                }
            }
        }
    }

    private fun acknowledgeAndCompletePurchase(purchaseItem: Purchase) {
        if (purchaseItem.purchaseState == Purchase.PurchaseState.PURCHASED
            && purchaseItem.products.contains(kProProductId)
        ) {
            coroutineScope.launch {
                commonBilling.updateMessage("Completing Purchase...")
                logd("Completing Purchase, ${purchaseItem.products}, acknowledging...")
            }

            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseItem.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        coroutineScope.launch {
                            commonBilling.updateMessage("Purchase Completed Successfully")
                            logd("Purchase Acknowledged & Completed Successfully, ${purchaseItem.products}")
                        }
                        commonBilling.updateState(BillingState.Purchased)
                    }
                    else -> {
                        coroutineScope.launch {
                            commonBilling.updateMessage("Purchase Acknowledgement Failed")
                            logd("Purchase Acknowledgement Failed, Response Code: ${billingResult.responseCode}")
                        }
                        commonBilling.updateState(BillingState.NotPurchased(
                            "Purchase Acknowledgement Failed, " +
                                    "Code: ${billingResult.responseCode}"
                        ))
                    }
                }
            }
        }
    }

    private val purchasesListener =
        PurchasesResponseListener { billingResult, purchases ->
            logd("PurchasesResponseListener, Response Code: ${billingResult.responseCode}, purchases: $purchases")
            if (purchases.isNotEmpty()) {
                isNewPurchaseGate1 = false
                purchase = purchases.first() // Only supports one product for now
                logd("Purchase(s) Found, purchaseState: $purchase")

                if(purchase.products.contains(kProProductId)) {
                    logd("Purchase(s) Found, purchaseState: ${purchase.purchaseState}")
                    logd("Purchase(s) Found, purchase.originalJson: ${purchase.originalJson}")
                    // parse originalJson for purchaseState bc `purchase.purchaseState` doesn't support cancelled purchases. (WHY???)
                    val purchaseState = purchase.originalJson
                        .substringAfter("purchaseState\":")
                        .substringBefore(",")
                        .trim()
                        .toInt()
                    when (purchaseState) {
                        Purchase.PurchaseState.PENDING -> {
                            coroutineScope.launch {
                                //billing.updateMessage("Previous Purchase Found, but is PENDING.")
                                logd("Previous Purchase Found, but is pending, $purchase")
                            }
                            commonBilling.updateState(BillingState.Pending)
                            return@PurchasesResponseListener
                        }
                        3 -> { // Note: this enum is never used. (unknown why)
                            logd("PURCHASE_STATE=3, Purchase(s) Found, purchaseState: $purchases")
                        }
                        SLOW_PENDING_TRANSACTION -> { // NOTE: This is a hidden enum, not in the docs (!)
                            coroutineScope.launch {
                                // billing.updateMessage("Previous Purchase Found, but is SLOW_PENDING_TRANSACTION.")
                                logd("Previous Purchase Found, but is SLOW_PENDING_TRANSACTION, $purchase")
                            }
                            commonBilling.updateState(BillingState.Pending)
                            coroutineScope.launch(Dispatchers.IO) {
                                pollReloadPurchase()
                            }
                            return@PurchasesResponseListener
                        }
                        Purchase.PurchaseState.PURCHASED -> {
                            if(purchase.isAcknowledged) {
                                coroutineScope.launch {
                                    //billing.updateMessage("Previous Purchase Found and isAcknowledged")
                                    logd("Previous Purchase Found and Acknowledged, ${purchase.products}")
                                }
                                commonBilling.updateState(BillingState.Purchased)
                                return@PurchasesResponseListener
                            }

                            // NOT acknowledged, will attempt to acknowledge
                            coroutineScope.launch {
                                //billing.updateMessage("Previous Purchase Found, but not acknowledged. Acknowledging...")
                                logd("Previous Purchase Found, but not acknowledged. Acknowledging... $purchase")
                            }
                            commonBilling.updateState(BillingState.Pending)
                            acknowledgeAndCompletePurchase(purchase) // attempt acknowledge
                            return@PurchasesResponseListener
                        }
                        Purchase.PurchaseState.UNSPECIFIED_STATE -> { // cancelled(?) / Finished(?)
                            if(purchase.products.contains(kProProductId)) {
                                if(purchase.isAcknowledged) {
                                    coroutineScope.launch {
                                        //billing.updateMessage("Previous Purchase Found, in UNSPECIFIED_STATE, but acknowledged and is Pro.")
                                        logd("Previous Purchase Found, in UNSPECIFIED_STATE, and isAcknowledged and is Pro.")
                                    }
                                    commonBilling.updateState(BillingState.Purchased)
                                    return@PurchasesResponseListener
                                }
                                if(purchase.isAcknowledged.not()) {
                                    coroutineScope.launch {
                                        //billing.updateMessage("Previous Purchase Found, in UNSPECIFIED_STATE, but not acknowledged. Acknowledging...")
                                        logd("Previous Purchase Found, in UNSPECIFIED_STATE, but not acknowledged. Acknowledging... $purchase")
                                    }
                                    commonBilling.updateState(BillingState.Pending)
                                    acknowledgeAndCompletePurchase(purchase) // attempt acknowledge
                                    return@PurchasesResponseListener
                                }
                            }
                        }
                    }
                }

                commonBilling.updateState(BillingState.NotPurchased("Previous purchase found, but not Acknowledged Pro."))
                coroutineScope.launch {
                    //billing.updateMessage("Previous Purchases Found, but no Acknowledged Pro.")
                    logd("Previous Purchases Found, but no Acknowledged Pro, $purchases")
                }

                return@PurchasesResponseListener
            }

            // When creating a new purchase, start polling for it, to check for cancellations/timeout & to update UI.
            if(!isNewPurchaseGate1) {
                if(isNewPurchaseGate2) {
                    isNewPurchaseGate2 = false
                    coroutineScope.launch {
                        //billing.updateMessage("Card processing timed out. Please try again.")
                        logd("Card processing timed out. Please try again.")
                    }
                    commonBilling.updateState(BillingState.NotPurchased("Card processing timed out. Please try again."))
                    return@PurchasesResponseListener
                }

                commonBilling.updateState(BillingState.NotPurchased())
            }
            logd("No Previous Purchases Found")
        }

    private fun reloadPurchases() {
        val queryPurchasesParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(
            queryPurchasesParams,
            purchasesListener
        )
    }

    private var isPolling = false
    // Listen for a purchase to be completed, then update the UI.
    private suspend fun pollReloadPurchase() {
        if(!isPolling) {
            isPolling = true
            coroutineScope.launch {
                // billing.updateMessage("Polling pollReloadPurchase...")
                logd("Start Polling pollReloadPurchase...")
            }

            var i =0
            do {
                logd("Polling pollReloadPurchase... ${i++}")
                reloadPurchases()
                delay(3000.milliseconds)
            } while(commonBilling.billingStateFlow.value == BillingState.Pending)
            isPolling = false
        }
    }
}
