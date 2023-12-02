package com.realityexpander

import android.app.Activity
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import co.touchlab.kermit.Logger as Log

data class PurchaseHelper(
    val activity: Activity,
    val _billingMessageFlow: MutableSharedFlow<String> = MutableStateFlow(""),
    val _isProPurchasedFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var billingClient: BillingClient
    private lateinit var productDetails: ProductDetails
    private lateinit var purchase: Purchase

    private val proProductId = "pro" // only supports one product for now

    private val _productName = MutableStateFlow(null as String?)
    val productName = _productName.asStateFlow()

    val Logd: (msg: String) -> Unit = { msg -> Log.d(msg, tag ="PurchaseHelper") }

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
                        _billingMessageFlow.emit("Purchase Canceled")
                        Logd("Purchase Canceled")
                    }
                }
                else -> {
                    coroutineScope.launch {
                        _billingMessageFlow.emit("Purchase Error, Response Code: ${billingResult.responseCode}")
                        Logd("Purchase Error, Response Code: ${billingResult.responseCode}")
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
                        coroutineScope.launch {
                            _billingMessageFlow.emit("Billing Client Connected")
                        }
                        queryProduct(proProductId)
                        reloadPurchase()
                    }
                    else -> {
                        coroutineScope.launch {
                            _billingMessageFlow.emit("Billing Client Connection Failure")
                        }
                        Logd("Billing Client Connection Failure, Response Code: ${billingResult.responseCode}")
                    }

                }
            }

            override fun onBillingServiceDisconnected() {
                coroutineScope.launch {
                    //_billingMessageFlow.emit("Billing Client Connection Lost")
                    Logd("Billing Client Connection Lost")
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
                    Logd("Product Query Successful, ${productDetails.name}")

                    return@queryProductDetailsAsync
                }

                coroutineScope.launch {
                    _billingMessageFlow.emit("No Matching Products Found")
                    Logd("No Matching Products Found, Response Code: ${billingResult.responseCode}")
                }
                _isProPurchasedFlow.value = false
                return@queryProductDetailsAsync
            }

            coroutineScope.launch {
                _billingMessageFlow.emit("Product Query Failed")
                Logd("Product Query Failed, Response Code: ${billingResult.responseCode}")
            }
            _isProPurchasedFlow.value = false
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

        coroutineScope.launch {
            _billingMessageFlow.emit( "Attempting Purchase")
            Logd("Attempting Purchase, ${productDetails.name}")
        }

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    private fun completePurchase(item: Purchase) {
        purchase = item
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED
            && purchase.products.contains(proProductId)
        ) {
            coroutineScope.launch {
                _billingMessageFlow.emit("Purchase Completed")
                Logd("Purchase Completed, ${purchase.products}, acknowledging...")
            }

            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    coroutineScope.launch {
                        _billingMessageFlow.emit("Purchase Acknowledged")
                        Logd("Purchase Acknowledged, ${purchase.products}")
                    }
                    _isProPurchasedFlow.value = true
                } else {
                    coroutineScope.launch {
                        _billingMessageFlow.emit("Purchase Acknowledgement Failed")
                        Logd("Purchase Acknowledgement Failed, Response Code: ${billingResult.responseCode}")
                    }
                    _isProPurchasedFlow.value = false
                }
            }
        }
    }

    private val purchasesListener =
        PurchasesResponseListener { billingResult, purchases ->
            if (purchases.isNotEmpty()) {
                purchase = purchases.first()
                println("Purchase(s) Found, purchaseState: ${purchase}")
                if(purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                    && purchase.products.contains(proProductId)
                ) {
                    if(purchase.isAcknowledged) {
                        coroutineScope.launch {
                            _billingMessageFlow.emit("Previous Purchase Found and Acknowledged")
                            Logd("Previous Purchase Found and Acknowledged, ${purchase.products}")
                        }
                        _isProPurchasedFlow.value = true
                        return@PurchasesResponseListener
                    }

                    coroutineScope.launch {
                        _billingMessageFlow.emit("Previous Purchase Found, but not acknowledged.")
                        Logd("Previous Purchase Found, but not acknowledged, ${purchase.products}")
                    }
                    completePurchase(purchase)
                    return@PurchasesResponseListener
                }

                _isProPurchasedFlow.value = false
                coroutineScope.launch {
                    _billingMessageFlow.emit("Previous Purchases Found, but no Pro acknowledged.")
                    Logd("Previous Purchases Found, but no Pro acknowledged, ${purchase.products}")
                }

                return@PurchasesResponseListener
            }

            _isProPurchasedFlow.value = false
            Logd("No Previous Purchases Found")
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
}
