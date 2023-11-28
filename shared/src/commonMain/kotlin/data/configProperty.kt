package data

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.resource

/**
 * Returns the value of the property key from the property file.
 *
 * Property file format:
 * ```
 * key1=value1
 * key2=value2
 * ```
 * Note: Property file must be in the shared/src/commonMain/resources directory.
 */

private const val kAppConfigDefault = "appConfig.properties"

@OptIn(ExperimentalResourceApi::class)
fun configPropertyNullable(
    propertyKey: String,
    defaultValue: String? = null,
    propertyFile: String = kAppConfigDefault
): String? {
    var finalValue = defaultValue

    runBlocking {
        try {
            resource(propertyFile)
                .readBytes()
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

