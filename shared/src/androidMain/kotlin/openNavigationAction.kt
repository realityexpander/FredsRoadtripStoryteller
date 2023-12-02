import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch


actual fun openNavigationAction(lat: Double, lng: Double, markerTitle: String) {
    CoroutineScope(Dispatchers.Main).launch {
        _intentFlow.emit(
//            Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng")).also { // just shows location
//            Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$lat,$lng")).also {
//                it.setPackage("com.google.android.apps.maps")
//            }
            Intent("Navigation").also {
                it.putExtra("lat", lat)
                it.putExtra("lng", lng)
                it.putExtra("label", "📌���")
                it.putExtra("markerTitle", markerTitle)
            }
        )
    }
}


actual fun purchaseProVersionAction() {
    CoroutineScope(Dispatchers.Main).launch {
        _intentFlow.emit(
            Intent("PurchasePro")
        )
    }
}
