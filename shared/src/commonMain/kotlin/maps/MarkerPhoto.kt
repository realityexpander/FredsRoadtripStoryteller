package maps

import kotlinx.serialization.Serializable

@Serializable
data class MarkerPhoto(
    val imageUrl: String = "",
    val caption: String = "",
    val attribution: String = ""
)
