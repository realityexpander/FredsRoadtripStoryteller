import data.billing.CommonBilling

actual fun purchaseProductAction(commonBilling: CommonBilling) {
    commonBilling.purchaseProCommand()
}

actual fun consumeProductAction(commonBilling: CommonBilling) {
    commonBilling.consumeProCommand()
}
