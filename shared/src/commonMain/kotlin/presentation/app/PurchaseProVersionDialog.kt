@file:Suppress("FunctionName")

package presentation.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import data.billing.CommonBilling
import kotlinx.coroutines.CoroutineScope
import presentation.uiComponents.PurchaseProVersionButton

@Composable
fun PurchaseProVersionDialog(
    billingState: CommonBilling.BillingState,
    commonBilling: CommonBilling,
    calcTrialTimeRemainingStringFunc: () -> String,
    onDismiss: () -> Unit,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) {

    Dialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
        onDismissRequest = onDismiss,
    ) {
        Box(
            modifier = Modifier.background(Color.Black),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colors.surface)
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "Trial has Ended",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h4,
                    color = Color.Red,
                    modifier = Modifier.fillMaxWidth()
                )
                Divider(modifier = Modifier.height(2.dp))
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "To continue using these features:",
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))

                Column (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "☑️ View Details of Markers",
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.h5,
                    )
                    Text(
                        "☑️ Speak Details of Markers",
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.h5,
                    )
                    Text(
                        "☑️ Announce Marker Details",
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.h5,
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Purchase Pro version",
                    color = Color.Cyan,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h4,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "🔻🔻BUY NOW!🔻🔻",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h5,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                PurchaseProVersionButton(
                    billingState,
                    commonBilling,
                    coroutineScope,
                    onDismiss,
                    calcTrialTimeRemainingStringFunc()
                )

                Text(
                    "Markers will continue to be collected, but you will not be able " +
                            "to view or speak details of the markers without Pro version.",
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Close")
                }

            }
        }
    }
}
