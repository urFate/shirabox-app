plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("plugin.serialization") version "1.9.20"
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
}

android {
    namespace = "org.shirabox.core"
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
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("androidx.room:room-common:2.6.1")
    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    implementation ("androidx.media3:media3-common:1.4.1")
    implementation ("androidx.datastore:datastore-preferences:1.1.1")

    // Room
    val roomVersion = "2.6.1"

    implementation ("androidx.room:room-common:$roomVersion")
    api ("androidx.room:room-runtime:$roomVersion")
    ksp ("androidx.room:room-compiler:$roomVersion")
    implementation ("androidx.room:room-ktx:$roomVersion")
}