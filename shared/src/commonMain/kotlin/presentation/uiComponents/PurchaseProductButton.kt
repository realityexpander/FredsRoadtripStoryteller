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
import consumeProductAction
import data.billing.ProductPurchaseState
import isDebuggable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import purchaseProductAction

// Set to true to enable `consume product` button for testing payments
private const val isTestingPayments_enableConsumeProduct = false

@Composable
fun PurchaseProductButton(
    productPurchaseState: ProductPurchaseState,
    coroutineScope: CoroutineScope,
    onCloseDrawer: () -> Unit
) {
    when (productPurchaseState) {
        is ProductPurchaseState.NotPurchased -> {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    coroutineScope.launch {
                        onCloseDrawer()
                        purchaseProductAction()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp),
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
            productPurchaseState.lastBillingMessage?.let {
                Text(
                    productPurchaseState.lastBillingMessage,
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
            Spacer(modifier = Modifier.height(16.dp))
        }

        is ProductPurchaseState.Pending -> {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp),
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
                        backgroundColor = MaterialTheme.colors.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        is ProductPurchaseState.Purchased -> {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Pro Version Enabled",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp),
                fontStyle = FontStyle.Normal,
                fontSize = MaterialTheme.typography.body1.fontSize,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        is ProductPurchaseState.Disabled -> {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp),
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

        is ProductPurchaseState.Error -> {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Purchase Error - ${productPurchaseState.errorMessage}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp, end = 8.dp)
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
        && isDebuggable
        && productPurchaseState is ProductPurchaseState.Purchased
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    consumeProductAction()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp)
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
