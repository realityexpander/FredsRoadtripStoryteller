import org.jetbrains.compose.ComposeBuildConfig.composeVersion

plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")
    id("org.jetbrains.compose")
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
        }

        // Must define the pods that are in the Podfile (unknown why?)
        pod("GoogleMaps") {
            version = "8.2.0"
        }
    }

    sourceSets {
        val ktorVersion = "2.2.4"
        val ksoupVersion = "0.2.1"
        val mapsComposeVersion = "3.1.0"

        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.components.resources)

                // For HTTP requests
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

                // For coroutines
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

                // For image loading
                implementation("media.kamel:kamel-image:0.5.1")

                // For scraping & parsing HTML
                implementation("com.mohamedrejeb.ksoup:ksoup-html:$ksoupVersion")
                // Only for encoding and decoding HTML entities
                implementation("com.mohamedrejeb.ksoup:ksoup-entites:$ksoupVersion")
            }
        }
        val androidMain by getting {
            dependencies {
                api("androidx.activity:activity-compose:1.7.2")
                api("androidx.appcompat:appcompat:1.6.1")
                api("androidx.core:core-ktx:1.10.1")

                // Google maps for Compose
                api("com.google.android.gms:play-services-location:21.0.1")
                api("com.google.android.gms:play-services-maps:18.1.0")
                implementation("com.google.maps.android:maps-compose:$mapsComposeVersion")
                // For clustering
                implementation("com.google.maps.android:maps-compose-utils:$mapsComposeVersion")

                // Ktor Client
                implementation("io.ktor:ktor-client-android:$ktorVersion")
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
