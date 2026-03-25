// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    // KSP - replaces KAPT for better performance and stability
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
    // Firebase - Google Services Plugin
    id("com.google.gms.google-services") version "4.4.0" apply false
    // Firebase - Crashlytics Plugin
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
}
