package presentation.uiComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import appMetadata
import consumeProductAction
import data.appSettings
import data.billing.CommonBilling
import data.billing.CommonBilling.BillingState
import data.billing.calcTrialTimeRemainingString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import purchaseProductAction

// Set to true to enable `consume product` button for testing payments
private const val isTestingPayments_enableConsumeProduct = true

@Composable
fun PurchaseProductButton(
    billingState: BillingState,
    commonBilling: CommonBilling,
    coroutineScope: CoroutineScope,
    onCloseDrawer: () -> Unit
) {
    when (billingState) {
        is BillingState.NotPurchased -> {
            Spacer(modifier = Modifier.height(16.dp))
            // Show trial time remaining
            Text(
                text = calcTrialTimeRemainingString(appSettings.installAtEpochMilli),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                fontStyle = FontStyle.Normal,
                fontSize = MaterialTheme.typography.body1.fontSize,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        onCloseDrawer()
                        purchaseProductAction(commonBilling)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
            ) {
                Text(
                    "Purchase Pro Version",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp),
                    fontStyle = FontStyle.Normal,
                    fontSize = MaterialTheme.typography.body1.fontSize,
                    textAlign = TextAlign.Center,
                )
            }
            billingState.lastBillingMessage?.let {
                if(it.isNotBlank()) {
                    Text(
                        billingState.lastBillingMessage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 8.dp)
                            .background(MaterialTheme.colors.error),
                        fontStyle = FontStyle.Normal,
                        fontSize = MaterialTheme.typography.body1.fontSize,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colors.onError,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        is BillingState.Pending -> {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                enabled = false
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Processing purchase...",
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp),
                        fontStyle = FontStyle.Normal,
                        fontSize = MaterialTheme.typography.body1.fontSize,
                        textAlign = TextAlign.Center,
                    )
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(18.dp)
                            .width(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colors.onPrimary,
                        backgroundColor = MaterialTheme.colors.primary,
                        progress = 0.5f
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        is BillingState.Purchased -> {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Pro Version Enabled ✔︎",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                fontStyle = FontStyle.Normal,
                fontSize = MaterialTheme.typography.body1.fontSize,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        is BillingState.Disabled -> {
            Spacer(modifier = Modifier.height(16.dp))
            // Show trial time remaining
            Text(
                text = calcTrialTimeRemainingString(appSettings.installAtEpochMilli),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                fontStyle = FontStyle.Normal,
                fontSize = MaterialTheme.typography.body1.fontSize,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                enabled = false
            ) {
                Text(
                    "Purchase Pro Version",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp),
                    fontStyle = FontStyle.Normal,
                    fontSize = MaterialTheme.typography.body1.fontSize,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        is BillingState.Error -> {
            Spacer(modifier = Modifier.height(8.dp))
            // Show trial time remaining
            Text(
                text = calcTrialTimeRemainingString(appSettings.installAtEpochMilli),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp),
                fontStyle = FontStyle.Normal,
                fontSize = MaterialTheme.typography.body1.fontSize,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Purchase Error - ${billingState.errorMessage}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
                    .background(MaterialTheme.colors.onError),
                fontStyle = FontStyle.Normal,
                fontSize = MaterialTheme.typography.body1.fontSize,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.onError
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Show consume product button for testing payments
    if(isTestingPayments_enableConsumeProduct
        && appMetadata.isDebuggable
        && billingState is BillingState.Purchased
        && appMetadata.platformId == "android"
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    consumeProductAction(commonBilling)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
            ,
        ) {
            Text(
                "Consume Product (Pro Version)",
                modifier = Modifier
                    .background(MaterialTheme.colors.error)
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp)
                ,
                fontStyle = FontStyle.Normal,
                fontSize = MaterialTheme.typography.body1.fontSize,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.onError,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
