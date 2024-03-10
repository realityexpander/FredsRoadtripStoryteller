@file:Suppress("FunctionName")

package presentation.uiComponents

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import appMetadata
import consumeProductAction
import data.billing.CommonBilling
import data.billing.CommonBilling.BillingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import purchaseProductAction
import kotlin.time.Duration.Companion.seconds

// Set to true to enable `consume product` button for testing payments
private const val isTestingPayments_enableConsumeProduct = true

@Composable
fun PurchaseProVersionButton(
    billingState: BillingState,
    commonBilling: CommonBilling,
    coroutineScope: CoroutineScope,
    onCloseDrawer: () -> Unit,
    trialTimeRemainingStr: String = "",
    calcTimeRemainingStrFunc: () -> String = {""},
    isTrialInProgress: Boolean = false
) {
    var trialTimeRemaining by remember { mutableStateOf(trialTimeRemainingStr) }
    LaunchedEffect(Unit) {
        while(true) {
            trialTimeRemaining = calcTimeRemainingStrFunc()
            delay(1.seconds)
        }
    }

    when (billingState) {
        is BillingState.NotPurchased -> {

            Spacer(modifier = Modifier.height(16.dp))
            // Show trial time remaining
            DisplayTrialTimeRemaining(isTrialInProgress, trialTimeRemaining)
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
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.body1.fontSize,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.onPrimary
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
            var indeterminateProgress by remember { mutableStateOf(0.0f) }

            LaunchedEffect(Unit) {
                while (true) {
                    indeterminateProgress += 0.05f
                    if (indeterminateProgress > 1) {
                        indeterminateProgress = 0.0f
                    }
                    kotlinx.coroutines.delay(100)
                }
            }

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
                        color = MaterialTheme.colors.onBackground
                    )
                    CircularProgressIndicator(
                        modifier = Modifier
                            .height(18.dp)
                            .width(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colors.onPrimary,
                        backgroundColor = MaterialTheme.colors.primary,
                        progress = indeterminateProgress
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
                fontWeight = FontWeight.Bold,
                fontSize = MaterialTheme.typography.body1.fontSize,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        is BillingState.Disabled -> {
            Spacer(modifier = Modifier.height(16.dp))
            // Show trial time remaining
            DisplayTrialTimeRemaining(isTrialInProgress, trialTimeRemaining)
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
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        is BillingState.Error -> {
            Spacer(modifier = Modifier.height(8.dp))
            // Show trial time remaining
            DisplayTrialTimeRemaining(isTrialInProgress, trialTimeRemaining)
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Purchase Error - ${billingState.errorMessage}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
                    .background(MaterialTheme.colors.error),
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

@Composable
private fun DisplayTrialTimeRemaining(
    isTrialInProgress: Boolean,
    trialTimeRemainingStr: String
) {
    var componentWidth by remember { mutableStateOf(0.dp) }

    if(!isTrialInProgress) {
        Text(
            trialTimeRemainingStr,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            fontStyle = FontStyle.Normal,
            fontSize = MaterialTheme.typography.body1.fontSize,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))

        return
    }

    // Display the WOPR-style Countdown Dots
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp)
            .border(3.dp, MaterialTheme.colors.primary.copy(alpha = 0.75f))
            .padding(top=12.dp, bottom=12.dp, start=8.dp, end=8.dp)

    ) {
        Text(
            text = trialTimeRemainingStr,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp)
                .onGloballyPositioned {
                    componentWidth = it.size.width.dp
                },
            fontStyle = FontStyle.Normal,
            fontSize = MaterialTheme.typography.body2.fontSize * 0.75f,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.subtitle2
        )
        Spacer(modifier = Modifier.height(8.dp))

        WOPRDotScanner(componentWidth)
    }
}


@Composable
fun WOPRDotScanner(componentWidth: Dp) {
    val dotSize = 8.dp
    val delayUnit = 125
    val spaceBetween = 7.dp
    val minAlpha = 0.1f
    val dotColor: Color = MaterialTheme.colors.onPrimary

    val numberOfDots = (((componentWidth) / (dotSize + spaceBetween)).toInt() / 1.6).toInt()

    @Composable
    fun Dot(alpha: Float) = Spacer(
        Modifier
            .size(dotSize)
            .alpha(alpha)
            .background(
                color = dotColor, shape = RoundedCornerShape(0)
            )
    )

    val infiniteTransition = rememberInfiniteTransition()

    @Composable
    fun animateAlphaWithDelay(delay: Int) =
        infiniteTransition.animateFloat(
            initialValue = minAlpha,
            targetValue = minAlpha,
            animationSpec = infiniteRepeatable(animation = keyframes {
                durationMillis = numberOfDots * delayUnit

                minAlpha at delay with LinearEasing
                1f at delay + (delayUnit/5) with LinearEasing
                minAlpha at delay + durationMillis / 5
            })
        )

    val alphas = arrayListOf<State<Float>>()
    for (i in 0 until numberOfDots) {
        alphas.add(animateAlphaWithDelay(delay = i * delayUnit))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        alphas.forEach {
            Spacer(Modifier.width(spaceBetween))
            Dot(it.value)
        }
    }
}
