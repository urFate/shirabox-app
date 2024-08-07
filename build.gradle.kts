buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.1")
    }
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    kotlin("android") version "1.9.20" apply false
    id ("com.android.application") version "8.5.1" apply false
    id ("com.android.library") version "8.5.1" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
}