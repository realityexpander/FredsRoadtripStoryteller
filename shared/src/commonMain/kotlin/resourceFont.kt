@file:Suppress("SpellCheckingInspection")

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

// Resource Font is a font that is loaded from the resources of the platform.
// This is the common interface for Android and iOS.
// This version is specifically for Compose 1.5.x and below.
//
// Based on this article:
// https://medium.com/@kamal.lakhani56/fontresource-kmm-3df685804cb0
//
// - Alternative, didnt work for me :https://stackoverflow.com/a/75841745/2857200

@Composable
expect fun resourceFont(
    name: String,
    res: String,
    weight: FontWeight,
    style: FontStyle
): Font
