@file:Suppress("PropertyName")

package com.realityexpander

import android.app.Activity
import com.android.billingclient.api.*
import data.billing.ProductPurchaseState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds
import co.touchlab.kermit.Logger as Log

private const val SLOW_PENDING_TRANSACTION = 4  // Billing hidden type for slow pending transactions
private const val kProProductId = "pro" // only supports one product for now

/**
 * PurchaseHelper
 *
 * This class handles the purchase flow for the Android app.
 *
 * It uses the Google BillingClient to handle the purchase flow.
 * Emits messages to the UI via a MutableSharedFlow<String>.
 * Emits the current purchase state to the UI via a MutableStateFlow<ProductPurchaseState>.
 *
 * @param activity: Activity - the Android activity that is using this class
 * @param _billingMessageFlow: MutableSharedFlow<String> - a flow that emits messages to the UI
 * @param _productPurchaseStateFlow: MutableStateFlow<ProductPurchaseState> - a flow that emits the current purchase state to the UI
 */
data class ProductPurchaseHelper(
    val activity: Activity,
    val _billingMessageFlow: MutableSharedFlow<String> = MutableStateFlow(""),
    val _productPurchaseStateFlow: MutableStateFlow<ProductPurchaseState> = MutableStateFlow(ProductPurchaseState.NotPurchased()),
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
                            completePurchase(purchase)
                        }
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    coroutineScope.launch {
                        //_billingMessageFlow.emit("User Canceled Purchase")
                        logd("Purchase Canceled")
                        _productPurchaseStateFlow.value = ProductPurchaseState.NotPurchased()
                    }
                }
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                    coroutineScope.launch {
                        _billingMessageFlow.emit("Card Declined, Code: ${billingResult.responseCode}")
                        logd("Card Declined, Response Code: ${billingResult.responseCode}")
                        _productPurchaseStateFlow.value = ProductPurchaseState.NotPurchased("Card Declined, Code: ${billingResult.responseCode}")
                    }
                }
                else -> {
                    coroutineScope.launch {
                        _billingMessageFlow.emit("Purchase Error, Code: ${billingResult.responseCode}")
                        logd("Purchase Error, Response Code: ${billingResult.responseCode}")
                        _productPurchaseStateFlow.value = ProductPurchaseState.Error("Billing Error, Code: ${billingResult.responseCode}")
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
                    //coroutineScope.launch {
                    //    _billingMessageFlow.emit("Billing Client Connected")
                    //}
                        queryProduct(proProductId)
                        reloadPurchase()
                    }
                    else -> {
                        coroutineScope.launch {
                            _billingMessageFlow.emit("Billing Client Connection Failure")
                        }
                        logd("Billing Client Connection Failure, Response Code: ${billingResult.responseCode}")
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                coroutineScope.launch {
                    _billingMessageFlow.emit("Billing Client Connection Lost")
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
                    //_billingMessageFlow.emit("No Matching Products Found")
                    logd("No Matching Products Found, Response Code: ${billingResult.responseCode}")
                }
                _productPurchaseStateFlow.value = ProductPurchaseState.Disabled
                return@queryProductDetailsAsync
            }

            coroutineScope.launch {
                _billingMessageFlow.emit("Product Query Failed")
                logd("Product Query Failed, Response Code: ${billingResult.responseCode}")
            }
            _productPurchaseStateFlow.value = ProductPurchaseState.Disabled
        }
    }

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

        _productPurchaseStateFlow.value = ProductPurchaseState.Pending
        isNewPurchaseGate1 = true
        isNewPurchaseGate2 = true
        coroutineScope.launch {
            //_billingMessageFlow.emit( "Attempting Purchase")
            logd("Attempting Purchase, ${productDetails.name}")
            pollReloadPurchase()
        }
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    private fun completePurchase(purchaseItem: Purchase) {
        if (purchaseItem.purchaseState == Purchase.PurchaseState.PURCHASED
            && purchaseItem.products.contains(proProductId)
        ) {
            coroutineScope.launch {
                _billingMessageFlow.emit("Purchase Submitted...")
                logd("Purchase Completed, ${this@ProductPurchaseHelper.purchase.products}, acknowledging...")
            }

            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(this.purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        coroutineScope.launch {
                            _billingMessageFlow.emit("Purchase Completed Successfully")
                            logd("Purchase Acknowledged, ${this@ProductPurchaseHelper.purchase.products}")
                        }
                        _productPurchaseStateFlow.value = ProductPurchaseState.Purchased
                    }
                    else -> {
                        coroutineScope.launch {
                            _billingMessageFlow.emit("Purchase Acknowledgement Failed")
                            logd("Purchase Acknowledgement Failed, Response Code: ${billingResult.responseCode}")
                        }
                        _productPurchaseStateFlow.value = ProductPurchaseState.NotPurchased(
                            "Purchase Acknowledgement Failed, " +
                                    "Code: ${billingResult.responseCode}"
                        )
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
                purchase = purchases.first() // check for more than one purchase?
                println("Purchase(s) Found, purchaseState: $purchase")

                if(purchase.products.contains(proProductId)) {
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
                                //_billingMessageFlow.emit("Previous Purchase Found, but is PENDING.")
                                logd("Previous Purchase Found, but is pending, $purchase")
                            }
                            _productPurchaseStateFlow.value = ProductPurchaseState.Pending
                            return@PurchasesResponseListener
                        }
                        3 -> {
                            logd("PURCHASE_STATE=3, Purchase(s) Found, purchaseState: $purchases")
                        }
                        SLOW_PENDING_TRANSACTION -> {
                            coroutineScope.launch {
                                // _billingMessageFlow.emit("Previous Purchase Found, but is SLOW_PENDING_TRANSACTION.")
                                logd("Previous Purchase Found, but is SLOW_PENDING_TRANSACTION, $purchase")
                            }
                            _productPurchaseStateFlow.value = ProductPurchaseState.Pending
                            coroutineScope.launch(Dispatchers.IO) {
                                pollReloadPurchase()
                            }
                            return@PurchasesResponseListener
                        }
                        Purchase.PurchaseState.PURCHASED -> {
                            if(purchase.isAcknowledged) {
                                coroutineScope.launch {
                                    //_billingMessageFlow.emit("Previous Purchase Found and isAcknowledged")
                                    logd("Previous Purchase Found and Acknowledged, ${purchase.products}")
                                }
                                _productPurchaseStateFlow.value = ProductPurchaseState.Purchased
                                return@PurchasesResponseListener
                            }

                            // NOT acknowledged, will attempt to acknowledge
                            coroutineScope.launch {
                                //_billingMessageFlow.emit("Previous Purchase Found, but not acknowledged. Acknowledging...")
                                logd("Previous Purchase Found, but not acknowledged. Acknowledging... $purchase")
                            }
                            _productPurchaseStateFlow.value = ProductPurchaseState.Pending
                            completePurchase(purchase) // attempt acknowledge
                            return@PurchasesResponseListener
                        }
                        Purchase.PurchaseState.UNSPECIFIED_STATE -> { // cancelled(?) / Finished(?)
                            if(purchase.isAcknowledged && purchase.products.contains(proProductId)) {
                                coroutineScope.launch {
                                    //_billingMessageFlow.emit("Previous Purchase Found, is UNSPECIFIED_STATE, but acknowledged and is Pro.")
                                    logd("Previous Purchase Found, is UNSPECIFIED_STATE, but acknowledged and is Pro.")
                                }
                                _productPurchaseStateFlow.value = ProductPurchaseState.Purchased
                                return@PurchasesResponseListener
                            }

                            coroutineScope.launch {
                                //_billingMessageFlow.emit("Previous Purchase Found, but is unknown product/unAcknowledged (UNSPECIFIED_STATE), $purchase")
                                logd("Previous Purchase Found, but is unknown product/unAcknowledged (UNSPECIFIED_STATE), $purchase")
                            }
                            _productPurchaseStateFlow.value = ProductPurchaseState.NotPurchased("Previous Purchase Found, but is unknown (UNSPECIFIED_STATE)")
                            return@PurchasesResponseListener
                        }
                    }
                }

                _productPurchaseStateFlow.value = ProductPurchaseState.NotPurchased("Previous purchase found, but not Acknowledged Pro.")
                coroutineScope.launch {
                    //_billingMessageFlow.emit("Previous Purchases Found, but no Acknowledged Pro.")
                    logd("Previous Purchases Found, but no Acknowledged Pro, $purchases")
                }

                return@PurchasesResponseListener
            }

            // When creating a new purchase, start polling for it to show up as completed in the UI.
            if(!isNewPurchaseGate1) {
                if(isNewPurchaseGate2) {
                    isNewPurchaseGate2 = false
                    coroutineScope.launch {
                        //_billingMessageFlow.emit("No Previous Purchases Found")
                        logd("Card processing timed out. Please try again.")
                    }
                    _productPurchaseStateFlow.value = ProductPurchaseState.NotPurchased("Card processing timed out. Please try again.")
                    return@PurchasesResponseListener
                }

                _productPurchaseStateFlow.value = ProductPurchaseState.NotPurchased()
            }
            logd("No Previous Purchases Found")
        }

    private fun reloadPurchase() {
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
                // _billingMessageFlow.emit("Polling pollReloadPurchase...")
                logd("Start Polling pollReloadPurchase...")
            }

            var i =0
            do {
                logd("Polling pollReloadPurchase... ${i++}")
                reloadPurchase()
                delay(2000.milliseconds)
            } while(_productPurchaseStateFlow.value == ProductPurchaseState.Pending)
            isPolling = false
        }
    }
}
