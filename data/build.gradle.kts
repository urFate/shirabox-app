plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "org.shirabox.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    runtimeOnly(libs.kotlin.refrect)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.org.jetbrains.kotlinx.serialization.json)
    implementation (libs.androidx.datastore.preferences)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)

    // Network
    implementation(libs.com.github.kittinunf.fuel)
    implementation(libs.com.squareup.okhttp3.okhttp)

    // Date-time
    implementation(libs.org.jetbrains.kotlinx.datetime)

    // Project modules
    implementation(project(":core"))
}