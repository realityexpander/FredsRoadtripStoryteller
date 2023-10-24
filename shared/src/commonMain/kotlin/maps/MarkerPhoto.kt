package maps

import kotlinx.serialization.Serializable

@Serializable
data class MarkerPhoto( // todo move to proper package
    val imageUrl: String = "",
    val caption: String = "",
    val attribution: String = ""
)
