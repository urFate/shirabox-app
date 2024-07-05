plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.serialization") version "1.9.20"
}

android {
    namespace = "org.shirabox.data"
    compileSdk = 34

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":core"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.9.23")
    implementation ("androidx.datastore:datastore-preferences:1.1.1")

    // Network
    implementation("com.github.kittinunf.fuel:fuel:3.0.0-alpha1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Date-time
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.1")
}