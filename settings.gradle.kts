enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS") // allows this: project.ext.properties["isTrialVersion"]

rootProject.name = "FredsRoadtripStoryteller"
include(":androidApp")
include(":shared")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")  // for Compose dev builds
        google()
    }

    plugins {
        // From `gradle.properties`
        val kotlinVersion = extra["kotlin.version"] as String
        val agpVersion = extra["agp.version"] as String
        val composeVersion = extra["compose.version"] as String
        val googleMapsSecretsPluginVersion = extra["google.maps.secrets-gradle-plugin.version"] as String
        val googleServicesVersion = extra["google.services.version"] as String
        val googleFirebaseCrashlyticsVersion = extra["google.firebase.crashlytics.version"] as String
        val googleFirebaseAppDistributionVersion = extra["google.firebase.appdistribution.version"] as String
        val atomicFuVersion = extra["kotlinx.atomicfu.version"] as String
        val gmazzoBuildConfigVersion = extra["gmazzo.buildconfig.version"] as String

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

        // Crashlytics
        id("com.google.firebase.crashlytics").version(googleFirebaseCrashlyticsVersion)

        // Firebase App Distribution Gradle plugin
        id("com.google.firebase.appdistribution").version(googleFirebaseAppDistributionVersion)

        // For AtomicFu
        id("kotlinx-atomicfu").version(atomicFuVersion)

        // For BuildConfig in Kotlin Multiplatform
        id("com.github.gmazzo.buildconfig").version(gmazzoBuildConfigVersion)
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.4.0")
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")  // for Compose dev builds
    }
}
