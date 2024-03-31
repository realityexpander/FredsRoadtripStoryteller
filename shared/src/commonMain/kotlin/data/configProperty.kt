package data

import fredsroadtripstoryteller.shared.generated.resources.Res
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi

private const val kAppConfigDefault = "appConfig.properties"

/**
 * Returns the value of the property key from the property file.
 *
 * Property file format:
 *
 * `key1=value1`
 *
 * ```
 * configPropertyString() : Returns the value as a string.
 * configPropertyFloat() : Returns the value as a float.
 * configPropertyInt() : Returns the value as an int.
 * configPropertyBoolean() : Returns the value as a boolean.
 * configPropertyNullable() : Returns the value as a string, or null if the property key is not found.
 * ```
 *
 * @param propertyKey The key of the property to return.
 * @param defaultValue The default value to return if the property key is not found.
 * @param propertyFile The name of the property file to read from.
 * @return The value of the property key, or the default value if the property key is not found.
 *
 * Note: Property file must be in the shared/src/commonMain/resources directory.
 */
@OptIn(ExperimentalResourceApi::class)
fun configPropertyNullable(
    propertyKey: String,
    defaultValue: String? = null,
    propertyFile: String = kAppConfigDefault
): String? {
    var finalValue = defaultValue

    runBlocking {
        try {
            Res.readBytes("files/$propertyFile") // in src/commonMain/composeResources/
                .decodeToString()
                .split("\n")
                .forEach { line ->
                    if(line.isBlank()) return@forEach

                    val key = line.split("=")[0]
                    val value = line.split("=")[1]
                    if (key == propertyKey) {
                        finalValue = value
                        return@runBlocking
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    return finalValue
}


fun configPropertyString(
    propertyKey: String,
    defaultValue: String,
    propertyFile: String = kAppConfigDefault
): String {
    return configPropertyNullable(
            propertyKey,
            defaultValue,
            propertyFile
        ) ?: defaultValue
}

fun configPropertyFloat(
    propertyKey: String,
    defaultValue: Float,
    propertyFile: String = kAppConfigDefault
): Float {
    return configPropertyNullable(
        propertyKey,
        defaultValue.toString(),
        propertyFile
    )?.toFloat() ?: defaultValue
}

fun configPropertyInt(
    propertyKey: String,
    defaultValue: Int,
    propertyFile: String = kAppConfigDefault
): Int {
    return configPropertyNullable(
        propertyKey,
        defaultValue.toString(),
        propertyFile
    )?.toInt() ?: defaultValue
}

fun configPropertyBoolean(
    propertyKey: String,
    defaultValue: Boolean,
    propertyFile: String = kAppConfigDefault
): Boolean {
    return configPropertyNullable(
        propertyKey,
        defaultValue.toString(),
        propertyFile
    )?.toBoolean() ?: defaultValue
}

