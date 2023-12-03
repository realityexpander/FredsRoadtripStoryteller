import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class ProductPurchaseAction(val value: String) {
    PurchasePro("PurchasePro"),
    ConsumePro("ConsumePro"),
}

actual fun purchaseProductAction() {
    CoroutineScope(Dispatchers.Main).launch {
        _intentFlow.emit(
            Intent(ProductPurchaseAction.PurchasePro.value)
        )
    }
}

actual fun consumeProductAction() {
    CoroutineScope(Dispatchers.Main).launch {
        _intentFlow.emit(
            Intent(ProductPurchaseAction.ConsumePro.value)
        )
    }
}
