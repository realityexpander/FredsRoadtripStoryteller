plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    kotlin("multiplatform").apply(false)
    id("com.android.application").apply(false)
    id("com.android.library").apply(false)

    id("org.jetbrains.compose").apply(false)
    id("org.jetbrains.kotlin.plugin.serialization").apply(false)

    id("com.google.firebase.appdistribution").apply(false)
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
