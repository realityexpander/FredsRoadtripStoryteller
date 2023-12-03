plugins {
    kotlin("multiplatform")
    kotlin("native.cocoapods")
    id("com.android.library")

    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.serialization")

    id("com.github.gmazzo.buildconfig")
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
                api(libs.multiplatform.settings)
                api(libs.multiplatform.settings.no.arg)
                api(libs.multiplatform.settings.test)

                // Logging
                api(libs.logger.kermit)
                api(libs.logger.kermit.crashlytics)
                api(libs.logger.slf4j.nop) // removes this warning: https://www.slf4j.org/codes.html#StaticLoggerBinder

                // Date-time
                implementation(libs.kotlinx.datetime)

                // AtomicFu
                api(libs.kotlinx.atomicfu)
            }
        }
        // Android dependencies that are accessed from the shared module (expect/actual implementations)
        //   & native bridges to kotlin-only libraries
        val androidMain by getting {
            dependencies {
                // Compose previews
                implementation(libs.compose.ui.tooling.preview)  // previews only work on Android side
                implementation(libs.compose.ui.tooling)

                // Google maps for Android
                implementation(libs.google.play.services.android.location)
                api(libs.google.play.services.android.maps)  // api means its exposed to the pure-android app (for init)
                // Google maps for Compose for Android
                implementation(libs.google.maps.android.compose)
                // Clustering
                implementation(libs.google.maps.android.compose.utils)

                // Ktor Client for Android
                implementation(libs.ktor.client.android)  // native bridge to kotlin-only
                implementation(libs.ktor.client.cio)

                // Firebase BoM
                api(project.dependencies.platform(libs.google.firebase.bom.get())) // use `.get` bc its just a simple string with no version id
                // Firebase SDK for Google Analytics
                api(libs.google.firebase.analytics.ktx.get())
                api(libs.google.firebase.crashlytics.get())

//                // Firebase feedback // todo bump version to final for release
//                implementation(libs.google.firebase.appdistribution.api.ktx)
//                implementation(libs.google.firebase.appdistribution)

                // Billing
                api(libs.android.billingclient.billing)
                api(libs.android.billingclient.billing.ktx)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }
    }
}

// For the Shell Android Application (no core kotlin code, just android-specific)
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

    // enable previews
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        // kotlinx.atomicfu has to be on the classpath
        //  it's an implementation detail of kotlinx.atomicfu gradle plugin
        classpath(libs.atomicfu.gradle.plugin)
    }
}

apply(plugin = "kotlinx-atomicfu") // Needs to be applied after the buildscript block
