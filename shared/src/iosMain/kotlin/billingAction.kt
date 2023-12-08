

//val listenForProducts: SKProductsRequestDelegateProtocol
//        = object : NSObject(), SKProductsRequestDelegateProtocol {
//    override fun productsRequest(request: SKProductsRequest, didReceiveResponse: SKProductsResponse) {
//        println("didReceiveResponse, " +
//                "request: $request, " +
//                "products: ${didReceiveResponse.products}"
//        )
//    }
//}
//
//val listenForTransactions: SKPaymentTransactionObserverProtocol
//        = object : NSObject(), SKPaymentTransactionObserverProtocol {
//    override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
//        println("updatedTransactions, " +
//                "queue: $queue, " +
//                "transactions: $updatedTransactions" +
//                "transactions: ${updatedTransactions.size}" +
//                "transactions: ${updatedTransactions[0]}"
//        )
//    }
//}


actual fun purchaseProductAction(commonBilling: CommonBilling) {
//    println("purchaseProAction")
//    SKPaymentQueue.defaultQueue().addTransactionObserver(listenForTransactions)
//    val x = SKMutablePayment().apply {
//        setProductIdentifier("pro")
//        setQuantity(1)
//    }
//    SKPaymentQueue.defaultQueue().addPayment(x)
//
//    SKProductsRequest(setOf("pro")).setDelegate(listenForProducts)
//    try {
//        SKProductsRequest(setOf("pro")).start()
//    } catch (e: Exception) {
//        println("Exception: $e")
//    }

    commonBilling.purchaseProCommand()
}

actual fun consumeProductAction(commonBilling: CommonBilling) {
    commonBilling.consumeProCommand()
}
