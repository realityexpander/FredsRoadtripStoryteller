rootProject.name = "TalkingHistoricalMarkers"
include(":androidApp")
include(":shared")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
//        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }

    plugins {
        val kotlinVersion = extra["kotlin.version"] as String
        val agpVersion = extra["agp.version"] as String
        val composeVersion = extra["compose.version"] as String
        val googleMapsSecretsPluginVersion = extra["google.maps.secrets-gradle-plugin.version"] as String
        val googleServicesVersion = extra["google.services.version"] as String

        kotlin("jvm").version(kotlinVersion)
        kotlin("multiplatform").version(kotlinVersion)
        kotlin("android").version(kotlinVersion)

        id("com.android.application").version(agpVersion)
        id("com.android.library").version(agpVersion)
        id("org.jetbrains.compose").version(composeVersion)

         //For Kotlinx Serialization
        id("org.jetbrains.kotlin.plugin.serialization").version(kotlinVersion)

        // For Google Maps Secrets Gradle Plugin
        id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin").version(googleMapsSecretsPluginVersion)

        // For Google Services (Analytics, Feedback)
        id("com.google.gms.google-services").version(googleServicesVersion)
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.4.0")
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
//        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
