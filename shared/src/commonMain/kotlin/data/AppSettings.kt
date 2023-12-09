package data

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import data.loadMarkers.LoadMarkersResult
import json
import kForceClearAllSettingsAtLaunch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import presentation.maps.Location
import presentation.maps.RecentlySeenMarker
import presentation.maps.RecentlySeenMarkersList
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.safeCast
import co.touchlab.kermit.Logger as Log

val appSettings = AppSettings.create()
    .apply {
        if(kForceClearAllSettingsAtLaunch) { clearAllSettings() }
        // Log.setMinSeverity(Severity.Warn)
        printAppSettings()
    }

@Suppress("MemberVisibilityCanBePrivate") // for settingsInstance - will be used in tests
class AppSettings(val settingsInstance: Settings) {

    // Typesafe accessors for settings
    // is there a way to put these into the map automatically & keep typesafe w/o using casts?
    // REMEMBER TO ADD NEW SETTINGS TO THE MAP! - FORMATTING IS INTENTIONAL
    // ALSO REMEMBER TO ADD NEW TYPES TO THE WHEN STATEMENT IN THE DELEGATE GETTER/SETTER
    var loadMarkersResult by
        SettingsDelegate(settingsInstance,
       kMarkersResult, defaultValue = LoadMarkersResult())
    var markersLastUpdatedLocation by
        SettingsDelegate(settingsInstance,
       kMarkersLastUpdatedLocation, defaultValue = Location(0.0, 0.0))
    var lastKnownUserLocation by
        SettingsDelegate(settingsInstance,
       kLastKnownUserLocation, defaultValue = Location(0.0, 0.0))
    var isRecentlySeenMarkersPanelVisible by
        SettingsDelegate(settingsInstance,
       kIsRecentlySeenMarkersPanelVisible, defaultValue = false)
    var isPermissionsGranted by
        SettingsDelegate(settingsInstance,
       kIsPermissionsGranted, defaultValue = false)
    var recentlySeenMarkersSet by
        SettingsDelegate(settingsInstance,
       kRecentlySeenMarkersSet, defaultValue = RecentlySeenMarkersList())
    var uiRecentlySeenMarkersList by
        SettingsDelegate(settingsInstance,
       kUiRecentlySeenMarkersList, defaultValue = RecentlySeenMarkersList())
    var lastSpokenRecentlySeenMarker: RecentlySeenMarker by
        SettingsDelegate(settingsInstance,
       kLastSpokenRecentlySeenMarker, defaultValue = RecentlySeenMarker("",""))
    var installAtEpochMilli: Long by
        SettingsDelegate(settingsInstance,
       kInstallAtEpochMilli, defaultValue = Clock.System.now().toEpochMilliseconds())

    // • For Settings panel
    var isSpeakWhenUnseenMarkerFoundEnabled by
        SettingsDelegate(settingsInstance,
       kIsSpeakWhenUnseenMarkerFoundEnabled, defaultValue = true)
    var isSpeakDetailsWhenUnseenMarkerFoundEnabled by
        SettingsDelegate(settingsInstance,
       kIsSpeakDetailsWhenUnseenMarkerFoundEnabled, defaultValue = false)
    var isStartBackgroundTrackingWhenAppLaunchesEnabled by
        SettingsDelegate(settingsInstance,
       kIsStartBackgroundTrackingWhenAppLaunchesEnabled, defaultValue = false)
    var seenRadiusMiles by
        SettingsDelegate(settingsInstance,
       kSeenRadiusMiles, defaultValue = 0.5)
    var isMarkersLastUpdatedLocationVisible by
        SettingsDelegate(settingsInstance,
       kIsMarkersLastUpdatedLocationVisible, defaultValue = false)

    // - REMEMBER TO ADD NEW SETTINGS TO THIS MAP! - FORMATTING IS INTENTIONAL
    var settingsMap = createSettingsMap()
    private fun createSettingsMap() = mutableMapOf<String, Any?>(
        kMarkersResult to
         loadMarkersResult,
        kMarkersLastUpdatedLocation to
         markersLastUpdatedLocation,
        kLastKnownUserLocation to
         lastKnownUserLocation,
        kIsPermissionsGranted to
         isPermissionsGranted,
        kIsRecentlySeenMarkersPanelVisible to
         isRecentlySeenMarkersPanelVisible,
        kIsSpeakWhenUnseenMarkerFoundEnabled to
         isSpeakWhenUnseenMarkerFoundEnabled,
        kIsStartBackgroundTrackingWhenAppLaunchesEnabled to
         isStartBackgroundTrackingWhenAppLaunchesEnabled,
        kSeenRadiusMiles to
         seenRadiusMiles,
        kIsMarkersLastUpdatedLocationVisible to
         isMarkersLastUpdatedLocationVisible,
        kRecentlySeenMarkersSet to
         recentlySeenMarkersSet,
        kUiRecentlySeenMarkersList to
         uiRecentlySeenMarkersList,
        kIsSpeakDetailsWhenUnseenMarkerFoundEnabled to
         isSpeakDetailsWhenUnseenMarkerFoundEnabled,
        kLastSpokenRecentlySeenMarker to
         lastSpokenRecentlySeenMarker,
        kInstallAtEpochMilli to
         installAtEpochMilli
    )

    // - REMEMBER TO ADD NEW SETTINGS TO THE CONST LIST! - FORMATTING IS INTENTIONAL
    companion object {
        fun create(): AppSettings {
            return AppSettings(Settings())
        }
        fun use(settings: Settings): AppSettings {
            return AppSettings(settings)
        }

        // Settings Keys
        const val kMarkersResult =
            "kMarkersResult"
        const val kMarkersLastUpdatedLocation =
            "kMarkersLastUpdatedLocation"
        const val kLastKnownUserLocation =
            "kLastKnownUserLocation"
        const val kIsPermissionsGranted =
            "kIsPermissionsGranted"
        const val kIsRecentlySeenMarkersPanelVisible =
            "kIsRecentlySeenMarkersPanelVisible"
        const val kRecentlySeenMarkersSet =
            "kRecentlySeenMarkersSet"
        const val kUiRecentlySeenMarkersList =
            "kUiRecentlySeenMarkersList"
        const val kLastSpokenRecentlySeenMarker =
            "kLastSpokenRecentlySeenMarker"
        const val kInstallAtEpochMilli =
            "kInstallAtEpochMilli"

        // • For Settings panel
        const val kIsSpeakWhenUnseenMarkerFoundEnabled =
            "kIsSpeakWhenUnseenMarkerFoundEnabled"
        const val kIsSpeakDetailsWhenUnseenMarkerFoundEnabled =
            "kIsSpeakDetailsWhenUnseenMarkerFoundEnabled"
        const val kIsStartBackgroundTrackingWhenAppLaunchesEnabled =
            "kIsStartBackgroundTrackingWhenAppLaunchesEnabled"
        const val kSeenRadiusMiles =
            "kSeenRadiusMiles"
        const val kIsMarkersLastUpdatedLocationVisible =
            "kIsMarkersLastUpdatedLocationVisible"
    }

    // Use [] access operator
    // Note: Not typesafe, so you have to cast the type for `get`.
    // Use get:
    //     val markers: MarkersResult = settings[kMarkersResult]
    //                  ^^^^^^^^^^^^^-- note: type is NOT inferred and must be specified.
    // Use set:
    //     settings.settingsMap[kMarkersResult] = MarkersResult() // no need to cast for setting.
    inline operator fun <reified TValue> get(key: String): TValue {
        // Guard
        settingsMap[key] ?: throw IllegalArgumentException("No setting delegate found for key= $key")

        val settingType = settingsMap[key]!!::class
        val tValueType = TValue::class
        if(tValueType.simpleName != settingType.simpleName) {
            throw IllegalArgumentException("Type mismatch, key= $key, " +
                    "type of delegate for settingMap[$key]= ${settingType.simpleName}, " +
                    "passed in type of TValue= ${TValue::class.simpleName}, " +
                    "Expecting same as settings type TValue= ${settingType.simpleName}")
        }

        return settingType.safeCast(settingsMap[key]) as TValue
    }
    // Note: Not typesafe, so you have to cast the return type for `set`.
    operator fun set(key: String, value: Any?) {
        settingsMap[key] = value
    }

    // Note: Not typesafe, so you have to cast the type for `getDelegate`.
    // Use:
    //     val markers = settings.getDelegate(kMarkersResult) as MarkersResult
    //                                  Note: must cast the type-^^^^^^^^^^^^^
    fun getDelegate(key: String): Any? {
        return settingsMap[key]
    }

    // Obliterates all values in the settings store
    fun clearAllSettings() { // This is useful for testing
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            settingsInstance.clear()

            // Re-add all the settings
            settingsMap.clear()
            settingsMap = createSettingsMap()

            Log.w { "AppSettings: Cleared all settings!" }
        }
    }

    fun clear(key: String) {
        settingsInstance[key] = null
    }

    fun hasKey(key: String): Boolean {
        return settingsInstance.hasKey(key)
    }

    fun printAppSettings() {
        val coroutineScope = CoroutineScope(Dispatchers.IO)

        coroutineScope.launch {
            Log.d("markersResult.size= ${loadMarkersResult.markerIdToMarkerMap.size}")

            // Show all keys
            Log.d { "All keys from settings: ${settingsInstance.keys.joinToString("") { "$it\n ⎣_" }}" }
            if (settingsInstance.keys.isEmpty()) Log.d { "No keys in settings" }

            // Show current settings
            settingsMap.forEach { entry ->
                if (entry.key == kMarkersResult) { // Don't want to display all the markers
                    Log.d { "Settings: ${entry.key} = ${(entry.value as LoadMarkersResult).markerIdToMarkerMap.size} markers" }
                    return@forEach
                }
                if (entry.key == kRecentlySeenMarkersSet) { // Don't want to display all the markers
                    Log.d { "Settings: ${entry.key} = ${(entry.value as RecentlySeenMarkersList).list.size} markers in seen set" }
                    return@forEach
                }
                if (entry.key == kUiRecentlySeenMarkersList) { // Don't want to display all the markers
                    Log.d { "Settings: ${entry.key} = ${(entry.value as RecentlySeenMarkersList).list.size} markers in seen list" }
                    return@forEach
                }

                Log.d { "Settings: ${entry.key} = ${entry.value}" }
            }
        }
    }
}

@Suppress("UNCHECKED_CAST") // You can see we are checking the type in the `when` statement... so this is safe, Kotlin compiler people...
class SettingsDelegate<T>(
    private val settings: Settings,
    private val key: String,
    private val defaultValue: T?
): ReadWriteProperty<Any?, T?> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return when (defaultValue) {
            is Boolean -> settings.getBoolean(key, defaultValue) as T
            is Double -> settings.getDouble(key, defaultValue) as T
            is Float -> settings.getFloat(key, defaultValue) as T
            is Int -> settings.getInt(key, defaultValue) as T
            is Long -> settings.getLong(key, defaultValue) as T
            is String -> settings.getString(key, defaultValue) as T
            is Location -> {
                json.decodeFromString<Location>(
                    settings.getStringOrNull(key) ?: return defaultValue
                ) as T
            }
            is List<*> -> { // Note: can't use List<String> here, because of generic type erasure, and assumes List<String>
                json.decodeFromString<List<String>>(
                    settings.getStringOrNull(key) ?: return defaultValue
                ) as T
            }
            is LoadMarkersResult -> {
                json.decodeFromString<LoadMarkersResult>(
                    settings.getStringOrNull(key) ?: return defaultValue
                ) as T
            }
            is RecentlySeenMarkersList -> {
                json.decodeFromString<RecentlySeenMarkersList>(
                    settings.getStringOrNull(key) ?: return defaultValue
                ) as T
            }
            is RecentlySeenMarker -> {
                json.decodeFromString<RecentlySeenMarker>(
                    settings.getStringOrNull(key) ?: return defaultValue
                ) as T
            }
            else -> throw IllegalArgumentException("Unsupported type, key= $key, " +
                    "type of defaultValue= ${defaultValue!!::class.simpleName}")
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        if(value == null) {
            settings[key] = null  // Useful for clearing a setting, and the default value will be returned upon next get.
            return
        }

        when (value) {
            is Boolean -> settings.putBoolean(key, value)
            is Double -> settings.putDouble(key, value)
            is Float -> settings.putFloat(key, value)
            is Int -> settings.putInt(key, value)
            is Long -> settings.putLong(key, value)
            is String -> settings.putString(key, value)
            is Location ->
                settings.putString(key, json.encodeToString(value as Location))
            is List<*> -> // Note: can't use List<String> here, because of type erasure, assumes List<String>
                settings.putString(key, json.encodeToString(value as List<String>))
            is LoadMarkersResult ->
                settings.putString(key, json.encodeToString(value as LoadMarkersResult))
            is RecentlySeenMarkersList ->
                settings.putString(key, json.encodeToString(value as RecentlySeenMarkersList))
            is RecentlySeenMarker ->
                settings.putString(key, json.encodeToString(value as RecentlySeenMarker))
            else -> throw IllegalArgumentException("Unsupported type, key= $key, " +
                    "type of defaultValue= ${defaultValue!!::class.simpleName}")
        }
    }
}
