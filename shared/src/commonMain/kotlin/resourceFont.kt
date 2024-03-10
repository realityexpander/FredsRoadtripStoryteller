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
// https://proandroiddev.com/custom-font-magic-in-compose-multiplatform-unlock-your-creativity-dcd0c9fa7756
// https://pnbharat.medium.com/using-custom-fonts-in-kmp-compose-multiplatform-967220873161
//
// - Alternative, didnt work for me :https://stackoverflow.com/a/75841745/2857200

@Composable
expect fun resourceFont(
    name: String,
    res: String,
    weight: FontWeight,
    style: FontStyle
): Font
