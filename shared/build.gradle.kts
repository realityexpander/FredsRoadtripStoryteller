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
        }

        // Must define the pods that are in the Podfile (Is this just the way it works?)
        pod("GoogleMaps") {
            version = libs.versions.pods.google.maps.get()
            //    version = "7.4.0" // for GoogleMapsUtils 4.2.2 (doesn't build for some c-interop reason, waiting for 5.0.0)
        }

        //    pod("Google-Maps-iOS-Utils") {
        //        version = libs.versions.pods.google.mapsUtils.get() // waiting for 5.0.0 to be released!
        //        //  source = path(project.file("../GoogleMapsUtils"))
        //        //  packageName = "Google_Maps_iOS_Utils"
        //    }
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.ui)
                implementation(libs.compose.material)
                // implementation(libs.compose.material3) // todo use material 3
                implementation(libs.compose.material.icons.extended)
                implementation(libs.compose.components.resources)

                // Ktor client for HTTP requests
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)

                // kotlinx serialization
                implementation(libs.kotlinx.serialization.json)

                // coroutines
                implementation(libs.kotlinx.coroutines.core)

                // image loading
                implementation(libs.kamel.image)

                // scraping & parsing HTML
                implementation(libs.ksoup.html)
                // Only for encoding and decoding HTML entities
                implementation(libs.ksoup.entites)

                // Settings persistence
                implementation(libs.multiplatform.settings)
                implementation(libs.multiplatform.settings.no.arg)
                implementation(libs.multiplatform.settings.test)

                // Logging
                implementation(libs.logger.kermit)
                implementation(libs.logger.slf4j.nop) // removes this warning: https://www.slf4j.org/codes.html#StaticLoggerBinder

                // Date-time
                implementation(libs.kotlinx.datetime)
            }
        }
        val androidMain by getting {
            dependencies {
                // Kotlin compose
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.ui)
                implementation(libs.compose.ui.tooling)
                implementation(libs.compose.ui.tooling.preview)

                // Android compose
                implementation(libs.androidx.compose.foundation)
                implementation(libs.androidx.compose.ui)
                implementation(libs.androidx.compose.ui.tooling)
                implementation(libs.androidx.compose.ui.tooling.preview)
                implementation(libs.androidx.compose.material)
                implementation(libs.androidx.activity.compose)

                // Compose previews
                implementation(libs.compose.ui.tooling.preview)
                implementation(libs.compose.ui.tooling)

                // Android-only UI
                api(libs.androidx.appcompat)

                // Google maps for Android
                api(libs.google.play.services.android.location)
                api(libs.google.play.services.android.maps)
                // Google maps for Compose for Android
                implementation(libs.google.maps.android.compose)
                // Clustering
                implementation(libs.google.maps.android.compose.utils)

                // Ktor Client for Android
                implementation(libs.ktor.client.android)

                // Firebase BoM
                api(platform(libs.google.firebase.bom.get())) // use .get bc its just a simple string and no version id
                // Firebase SDK for Google Analytics
                api(libs.google.firebase.analytics.ktx.get())  // use .get bc its just a simple string and no version id
                // Firebase feedback // todo move to beta variant for final release
                api(libs.google.firebase.appdistribution.api.ktx)
                api(libs.google.firebase.appdistribution)

                // Logger
                api(libs.logger.kermit)

                // Splash Screen
                api(libs.androidx.core.splashscreen)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test:${libs.versions.kotlin.get()}")
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
