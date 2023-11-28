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

@OptIn(ExperimentalResourceApi::class)
fun configPropertyNullable(
    propertyKey: String,
    defaultValue: String? = null,
    propertyFile: String = "appConfig.properties",
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


fun configProperty(
    propertyKey: String,
    defaultValue: String,
    propertyFile: String = "appConfig.properties",
): String {
    return configPropertyNullable(
            propertyKey,
            defaultValue,
            propertyFile
        ) ?: defaultValue
}
