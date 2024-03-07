@file:Suppress("FunctionName")

package previews

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import data.billing.CommonBilling
import data.billing.CommonBilling.BillingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import presentation.uiComponents.AppTheme
import presentation.uiComponents.PurchaseProVersionButton


@PreviewLightDark
@Composable
fun PurchaseButtonPreview(
    @PreviewParameter(BillingStates::class)
    billingState: BillingState
) {
    AppTheme {
        Surface {
            Column {
                PurchaseProVersionButton(
                    billingState = billingState,
                    commonBilling = CommonBilling(),
                    coroutineScope = CoroutineScope(Dispatchers.Main),
                    onCloseDrawer = {},
                    trialTimeRemainingStr = "1 hour Remaining in Trial"
                )
            }
        }
    }
}

class BillingStates: PreviewParameterProvider<BillingState> {
    override val values: Sequence<BillingState>
        get() = sequenceOf(
            // 0
            BillingState.NotPurchased(),
            // 1
            BillingState.NotPurchased("Last billing Message"),
            // 2
            BillingState.Pending,
            // 3
            BillingState.Purchased,
            // 4
            BillingState.Error("Error message"),
            // 5
            BillingState.Disabled
        )
}
