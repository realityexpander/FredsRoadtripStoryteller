plugins {
    kotlin("multiplatform")
    id("com.android.application")
    id("org.jetbrains.compose")

    // for Google Maps API key secrets gradle plugin
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")

    // Google Services - for feedback
    id("com.google.gms.google-services")
    // Crashlytics
    id("com.google.firebase.crashlytics")
}

kotlin {
    androidTarget()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":shared"))
            }
        }
    }
}

android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.realityexpander"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        applicationId = "com.realityexpander.talkinghistoricalmarkers"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = (findProperty("android.versionCode") as String).toInt()
        versionName = findProperty("android.versionName") as String
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    // For the Android App MainActivity (Android-specific only, not shared)
    dependencies {
        implementation(libs.androidx.compose.ui)
        implementation(libs.androidx.compose.ui.tooling.preview)
        implementation(libs.androidx.activity.compose)
        implementation(libs.androidx.compose.material)
        implementation(libs.androidx.ui.graphics.android)

        // Android-only Components
        implementation(libs.androidx.appcompat)

        // Splash Screen
        implementation(libs.androidx.core.splashscreen)
    }
}
