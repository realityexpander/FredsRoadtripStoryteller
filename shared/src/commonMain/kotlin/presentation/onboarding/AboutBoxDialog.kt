package presentation.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import buildNumberStr
import debugLog
import installAtEpochMilli
import json
import kAppNameStr
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import openWebLinkAction
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import sendEmailAction
import versionStr

@OptIn(ExperimentalResourceApi::class)
@Composable
fun AboutBoxDialog(
    onDismiss: () -> Unit = {}
) {
    Dialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
        onDismissRequest = {
            onDismiss()
        },
    ) {
        Surface(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource("about_box.png"),
                    contentDescription = null,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .offset(y = -(16).dp)
                        .fillMaxWidth()
                        .padding(8.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Link to app website
                        Button(
                            onClick = {
                                openWebLinkAction("https://github.com/realityexpander/FredsHistoryMarkers")
                                onDismiss()
                            },
                        ) {
                            Text("$kAppNameStr website")
                        }
                        Spacer(modifier = Modifier.padding(8.dp))

                        // Link to HMDB.org
                        Button(
                            onClick = {
                                openWebLinkAction("https://hmdb.org")
                                onDismiss()
                            },
                        ) {
                            Text("Visit HMDB.org")
                        }
                        Text("Historical Marker data is from HMDB.org")
                        Spacer(modifier = Modifier.padding(8.dp))

                        // Version number
                        Text("$kAppNameStr version $versionStr build $buildNumberStr") // todo  + $AppVersionStr)
                        if(installAtEpochMilli> 0) {
                            val it = Instant.fromEpochMilliseconds(installAtEpochMilli)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                            Text("Installed at: ${it.date} ${it.time.hour}:${it.time.minute}" )
                        }
                        // Send debug log
                        Button(
                            onClick = {
                                onDismiss()
                                sendEmailAction(body=json.encodeToString(debugLog))
                            },
                        ) {
                            Text("Send debug log")
                        }
                    }
                }

                // Close Button
                IconButton(
                    onClick = {
                        onDismiss()
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            MaterialTheme.colors.surface.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable { onDismiss() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.alpha(0.8f)
                    )
                }
            }
        }
    }
}
