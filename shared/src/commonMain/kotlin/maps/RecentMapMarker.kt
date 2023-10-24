package maps

class RecentMapMarker( // todo move to proper package
    val marker: MapMarker,
    val timeAddedToRecentList: Long = 0,
    val seenOrder: Int = 0
) {
    fun mapMarker() = marker
    fun key() = marker.key
}
