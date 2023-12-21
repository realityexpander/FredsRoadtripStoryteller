
import org.jetbrains.compose.internal.utils.localPropertiesFile
import java.util.Properties

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
    // Firebase App Distribution Gradle plugin
//    id("com.google.firebase.appdistribution") // note: must be disabled to add to play store

    id("com.github.gmazzo.buildconfig")
}

kotlin {
    androidTarget()
    sourceSets {
        val androidMain by getting {
            dependencies {
                implementation(project(":shared"))

                // Firebase feedback // todo bump version to final for release // note: must be disabled to add to play store
//                implementation(libs.google.firebase.appdistribution)
//                implementation(libs.google.firebase.appdistribution.api.ktx)
            }
        }
        androidMain.kotlin // to remove useless warning: "Variable 'androidMain' is never used"
    }
}

android {
    signingConfigs {
        create("release") {
            storeFile =
                file("/Volumes/TRS-83/dev/FredTalkingMarkers-keystore/freds-talking-markers.keystore")
            // storePassword = System.getenv("KEYSTORE_PASSWORD")
            // keyPassword = System.getenv("KEY_PASSWORD")
            val properties = Properties().apply {
                load(localPropertiesFile.reader())
            }
            storePassword = properties.getProperty("KEYSTORE_PASSWORD")
            keyPassword = properties.getProperty("KEY_PASSWORD")

            if(storePassword == null || keyPassword == null) {
                throw Exception("Keystore password(s) not found in `local.properties`")
            }

            keyAlias = "key0"
        }
    }
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "com.realityexpander"

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

    defaultConfig {
        applicationId = "com.realityexpander.talkinghistoricalmarkers"
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = (findProperty("android.versionCode") as String).toInt()
        versionName = findProperty("android.versionName") as String
        signingConfig = signingConfigs.getByName("debug")
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
        // buildConfig = true // creates a java-only class for Android
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    buildTypes {
        // for APK: ./gradlew assembleDebug appDistributionUploadDebug
        // Note: AAB must be in Play Store
        // for AAB:  ./gradlew bundleDebug appDistributionUploadDebug
        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            isJniDebuggable = true
            isDefault = true


//            firebaseAppDistribution { // note: must be disabled to add to play store
//                artifactType = "APK"
//                releaseNotesFile = "release-notes.txt"
//                testersFile = "testers-debug.txt"
//            }
            //buildConfigField("String", "android", "\"true\"") // android only
        }

        // for APK: ./gradlew assembleRelease appDistributionUploadRelease
        // Note: AAB must be in Play Store
        // for AAB  ./gradlew bundleRelease appDistributionUploadRelease
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

//            firebaseAppDistribution { // note: must be disabled to add to play store
//                artifactType = "APK"
//                releaseNotesFile = "release-notes.txt"
//                testersFile = "testers-debug.txt" // NOTE: This is the same as debug testers
//            }
        }
    }

    // check if the current build is being run
    if(project.ext.properties.containsKey("isTrialVersion")) {
        // if so, set the buildConfigField
        buildConfig {
            packageName = "com.realityexpander.common"
            forClass(packageName="", className="RootConfig") {
                buildConfigField("String", "TRIAL_VERSION",
                    "\"${project.ext.properties["isTrialVersion"]}\""
                )
            }
        }
    }

    // note: must set `buildConfig=true` in `buildFeatures` block above (leave for reference)
    // https://developer.android.com/build/build-variants?utm_source=studio
    //flavorDimensions.add("version")
    //productFlavors {
    //    // PRIMARY ./gradlew assembleTrialDebug appDistributionUploadTrialDebug
    //    // ./gradlew assembleTrialRelease appDistributionUploadTrialRelease
    //    create("trial") {
    //        dimension = "version"
    //        //applicationIdSuffix = ".debug"
    //        //versionNameSuffix = "-debug"
    //        firebaseAppDistribution {
    //            artifactType = "APK"
    //            releaseNotesFile = "release-notes.txt"
    //            testersFile = "testers-debug.txt"
    //        }
    //    }
    //
    //    // PRIMARY ./gradlew assembleProDebug appDistributionUploadProDebug
    //    // ./gradlew assembleProRelease appDistributionUploadProRelease
    //    create("pro") {
    //        dimension = "version"
    //        //applicationIdSuffix = ".release"
    //        //versionNameSuffix = "-release"
    //        signingConfig = signingConfigs.getByName("release")
    //        firebaseAppDistribution {
    //            artifactType = "AAB"
    //            releaseNotesFile = "release-notes.txt"
    //            testersFile = "testers-release.txt"
    //        }
    //    }
    //}

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

        // Firebase feedback // todo bump version to final for release
        debugImplementation(libs.google.firebase.appdistribution.api.ktx)
        debugImplementation(libs.google.firebase.appdistribution)
    }
}
