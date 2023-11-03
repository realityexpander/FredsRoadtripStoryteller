package maps

import kotlinx.serialization.Serializable

@Serializable
data class RecentlySeenMarker(
    val id: MarkerIdStr,
    val title: String,
    val insertedAtEpochMilliseconds: Long = 0,
)

@Serializable
data class RecentlySeenMarkersList(
    val list: List<RecentlySeenMarker> = listOf(),
)
