@file:Suppress("FunctionName")

package previews

import data.billing.CommonBilling
import data.billing.CommonBilling.BillingState
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import appNameStr
import data.AppSettings
import data.FakeSettings
import presentation.app.AppDrawerContent
import presentation.maps.LatLong
import presentation.maps.Marker
import presentation.maps.RecentlySeenMarker
import presentation.uiComponents.AppTheme

@Preview(
    name = "App Drawer (night)",
    group = "top element",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    device = "spec:width=1080px,height=1340px,dpi=440"
)
@Composable
fun AppDrawerPreview() {
    appNameStr = "Fred's Roadtrip Storyteller"

    AppTheme {
        Surface {
            Column(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AppDrawerContent(
                    finalMarkers = listOf(
                        Marker(
                            id = "M1234569",
                            title = "The story of the Falls Church Episcopal",
                            position = LatLong(38.882334, -77.171091),
                        ),
                        Marker(
                            id = "M2020202",
                            title = "Sears Home Kit",
                            position = LatLong(38.882334, -77.171091),
                        ),
                        Marker(
                            title = "Here is a very long title of a marker that will be truncated at this very long length due to the sordid details of this insanely long title",
                            id = "M123987",
                            position = LatLong(38.882334, -77.171091),
                        ),
                    ),
                    activeSpeakingMarker = RecentlySeenMarker(
                        id = "M69420",
                        // title = "Plans to fight the Ordinance of Nullification",
                        title = "Plans to fight the Ordinance",
                        insertedAtEpochMilliseconds = 0,
                    ),
                    isMarkerCurrentlySpeaking = true,
                    commonBilling = CommonBilling(),
                    billingState = BillingState.NotPurchased(),
                    calcTrialTimeRemainingStringFunc = { "1 hour Remaining in Trial" },
                    appSettings = AppSettings(FakeSettings()),
                )

            }
        }
    }
}
@Preview(
    name = "App Drawer (light)",
    group = "top element", device = "spec:width=1080px,height=1340px,dpi=440"
)
@Composable
fun AppDrawerLightPreview() {
    AppDrawerPreview()
}
