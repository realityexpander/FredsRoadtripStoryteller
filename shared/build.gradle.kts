import org.jetbrains.compose.ComposeBuildConfig.composeVersion

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    @Suppress("OPT_IN_USAGE")
    targetHierarchy.default()

    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        version = "1.0.0"
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        ios.deploymentTarget = "14.1"
        podfile = project.file("../iosApp/Podfile") // why doesn't it load the cocoapods from the iosApp podfile?
        framework {
            baseName = "shared"
            isStatic = true
            transitiveExport = true
        }

        // Must define the pods that are in the Podfile (Is this just the way it works?)
        pod("GoogleMaps") {
            version = "8.2.0"
            //    version = "7.4.0" // for GoogleMapsUtils 4.2.2 (doesn't build for some unknown reason, waiting for 5.0.0)
        }

        //    pod("Google-Maps-iOS-Utils") {
        //        version = "4.2.2" // waiting for 5.0.0 to be released!
        //        //  source = path(project.file("../GoogleMapsUtils"))
        //        //  packageName = "Google_Maps_iOS_Utils"
        //    }
    }

    sourceSets {
        val ktorClientVersion = "2.2.4"
        val ktorClientCoreVersion = "2.3.5"
        val ksoupVersion = "0.2.1"
        val mapsComposeVersion = "3.1.0"
        @Suppress("SpellCheckingInspection") // the library is spelled like this(!)
        val kMutliplatformSettingsVersion = "1.1.0"

        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)
                implementation(compose.materialIconsExtended)

                // Ktor client for HTTP requests
                implementation("io.ktor:ktor-client-core:$ktorClientCoreVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorClientVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorClientVersion")

                // kotlinx serialization
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

                // coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

                // image loading
                implementation("media.kamel:kamel-image:0.5.1")

                // scraping & parsing HTML
                implementation("com.mohamedrejeb.ksoup:ksoup-html:$ksoupVersion")
                // Only for encoding and decoding HTML entities
                implementation("com.mohamedrejeb.ksoup:ksoup-entites:$ksoupVersion")

                // Settings persistence
                implementation("com.russhwolf:multiplatform-settings:$kMutliplatformSettingsVersion")
                implementation("com.russhwolf:multiplatform-settings-no-arg:$kMutliplatformSettingsVersion")
                implementation("com.russhwolf:multiplatform-settings-test:$kMutliplatformSettingsVersion")

                // Logging
                implementation("co.touchlab:kermit:2.0.1")
                implementation("org.slf4j:slf4j-nop:2.0.9") // removes this warning: https://www.slf4j.org/codes.html#StaticLoggerBinder

                // Date-time
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
            }
        }
        val androidMain by getting {
            dependencies {

                api("androidx.activity:activity-compose:1.8.0")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.12.0")

                // Google maps for Compose for Android
                api("com.google.android.gms:play-services-location:21.0.1")
                api("com.google.android.gms:play-services-maps:18.1.0")
                implementation("com.google.maps.android:maps-compose:$mapsComposeVersion")
                // clustering
                implementation("com.google.maps.android:maps-compose-utils:$mapsComposeVersion")

                // Ktor Client for Android
                implementation("io.ktor:ktor-client-android:$ktorClientVersion")

                // For Logger in Android todo (why doesn't this automatically put this in shared android module?)
                api("co.touchlab:kermit:2.0.1")

                // Firebase BoM
                api(platform("com.google.firebase:firebase-bom:32.3.1"))
                // Firebase SDK for Google Analytics
                api("com.google.firebase:firebase-analytics-ktx")
                // Firebase feedback // todo move to beta variant for final release
                api("com.google.firebase:firebase-appdistribution-api-ktx:16.0.0-beta10")
                api("com.google.firebase:firebase-appdistribution:16.0.0-beta10")

                // Splash Screen
                api("androidx.core:core-splashscreen:1.0.1")
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.realityexpander.common"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        minSdk = (findProperty("android.minSdk") as String).toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }
}
