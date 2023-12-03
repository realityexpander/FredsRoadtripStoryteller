package data.billing

sealed class ProductPurchaseState {
    data class NotPurchased(val lastBillingMessage: String? = null) : ProductPurchaseState()
    data object Pending : ProductPurchaseState()
    data object Purchased : ProductPurchaseState()
    data class Error(val errorMessage: String) : ProductPurchaseState()
    data object Disabled : ProductPurchaseState()
}
