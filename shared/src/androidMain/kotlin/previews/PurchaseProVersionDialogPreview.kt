@file:Suppress("FunctionName")

package previews

import CommonAppMetadata
import android.content.res.Configuration
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import appMetadata
import data.billing.CommonBilling
import presentation.app.PurchaseProVersionDialog
import presentation.uiComponents.AppTheme


@Preview(
    name = "Purchase Pro Version Dialog (night)",
    group = "top element",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:width=1080px,height=2040px,dpi=440"
)
@Composable
fun PurchaseProVersionDialogPreview() {
    appMetadata = CommonAppMetadata()

    AppTheme {
        Surface {
            PurchaseProVersionDialog(
                billingState = CommonBilling.BillingState.NotPurchased(),
                commonBilling = CommonBilling(),
                calcTrialTimeRemainingStringFunc = { "1 day remaining" },
                onDismiss = {},
                isTrialInProgressFunc = { true }
            )
        }
    }
}

@Preview(
    name = "Purchase Pro Version Dialog (day)",
    group = "top element",
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:width=1080px,height=2040px,dpi=440"
)
@Composable
fun PurchaseProVersionDialogPreviewDay() {
    appMetadata = CommonAppMetadata()

    AppTheme {
        Surface {
            PurchaseProVersionDialog(
                billingState = CommonBilling.BillingState.NotPurchased(),
                commonBilling = CommonBilling(),
                calcTrialTimeRemainingStringFunc = { "1 day remaining" },
                onDismiss = {},
                isTrialInProgressFunc = { true }
            )
        }
    }
}
