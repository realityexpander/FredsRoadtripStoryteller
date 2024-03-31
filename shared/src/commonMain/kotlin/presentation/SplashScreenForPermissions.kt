@file:Suppress("FunctionName")

package presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import fredsroadtripstoryteller.shared.generated.resources.Res
import fredsroadtripstoryteller.shared.generated.resources.fred_head_owl_1
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import presentation.uiComponents.PreviewPlaceholder

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SplashScreenForPermissions(
    isPermissionsGranted: Boolean = false,
) {
    if(isPermissionsGranted) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary)
        )
        return
    }

    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.primary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .weight(1f)
                ,
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if(LocalInspectionMode.current) {
                    PreviewPlaceholder(
                        "Freds head",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.Red)
                    )
                } else {
                    Image(
                        painter = painterResource(Res.drawable.fred_head_owl_1),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f),
                        contentScale = ContentScale.FillWidth,
                    )
                }
                Text(
                    text = "Fred's Roadtrip Storyteller",
                    color = Color.White,
                    textAlign = TextAlign.Start,
                    fontSize = MaterialTheme.typography.h4.fontSize,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.weight(2.5f))
    }
}
