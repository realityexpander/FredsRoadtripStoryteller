package presentation.app.onboarding

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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import appMetadata
import debugLog
import fredsroadtripstoryteller.shared.generated.resources.Res
import fredsroadtripstoryteller.shared.generated.resources.about
import json
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.encodeToString
import openWebUrlAction
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import sendEmailAction

@OptIn(ExperimentalResourceApi::class)
@Composable
fun AboutBoxDialog(
    onDismiss: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

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
        Box(
            modifier = Modifier.fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.TopCenter
        ) {
            Image(
                painter = painterResource(Res.drawable.about),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.Center
            )

            // Close Button
            IconButton(
                onClick = {
                    onDismiss()
                },
                modifier = Modifier
                    .offset(y = 48.dp)  // skip top bar
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
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

            Box(
                modifier = Modifier
                    .offset(y = -(16).dp)
                    .padding(8.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.padding(16.dp))

                    // Link to app website
                    Button(
                        onClick = {
                            openWebUrlAction("https://FredsRoadtripStoryteller.com")
                            onDismiss()
                        },
                    ) {
                        Text("${appMetadata.appNameStr} website")
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(MaterialTheme.colors.background.copy(alpha = 0.5f)),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        // Link to HMDB.org
                        Button(
                            onClick = {
                                openWebUrlAction("https://hmdb.org")
                                onDismiss()
                            },
                        ) {
                            Text("Visit HMDB.org")
                        }
                        Text("Historical Marker data is from HMDB.org")
                        Spacer(modifier = Modifier.padding(8.dp))

                        // Version number
                        Text(
                            "${appMetadata.appNameStr} v${appMetadata.versionStr} " +
                                    if (appMetadata.platformId == "android")
                                        "build ${appMetadata.androidBuildNumberStr} "
                                    else
                                        "build ${appMetadata.iOSBundleVersionStr} "
                                                +
                                                if (appMetadata.isDebuggable)
                                                    "debug"
                                                else
                                                    "release",
                            style = MaterialTheme.typography.caption,
                            color = MaterialTheme.colors.onBackground
                        )
                        if (appMetadata.installAtEpochMilli > 0) {
                            val installTime =
                                Instant.fromEpochMilliseconds(appMetadata.installAtEpochMilli)
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                            Text(
                                "Installed: ${installTime.date} " +
                                        "${installTime.time.hour}:${installTime.time.minute} UTC"
                            )
                        }

                        Text("Debug log size: ${debugLog.size}")
                        // Send debug log
                        Button(
                            onClick = {
                                onDismiss()
                                coroutineScope.launch {
                                    debugLog.add(
                                        "AboutBoxDialog: Send debug log, debugLog.size=${debugLog.size}, " +
                                                "${appMetadata.appNameStr} version " +
                                                "${appMetadata.versionStr} build ${appMetadata.androidBuildNumberStr}"
                                    )
                                    sendEmailAction(body = json.encodeToString(debugLog))
                                }
                            },
                        ) {
                            Text("Send debug log")
                        }
                    }
                }
            }
        }
    }
}
