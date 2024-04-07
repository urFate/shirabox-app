plugins {
    id ("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    kotlin("plugin.serialization") version "1.9.20"
}

android {
    namespace = "live.shirabox.shirabox"
    compileSdk = 34

    defaultConfig {
        applicationId = "live.shirabox.shirabox"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
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
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {
    // Core dependencies
    val composeVersion = "1.6.5"

    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation ("androidx.activity:activity-compose:1.8.2")
    implementation ("androidx.compose.ui:ui:$composeVersion")
    implementation ("androidx.compose.ui:ui-graphics:$composeVersion")
    implementation ("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation ("androidx.core:core-ktx:1.12.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation (platform("androidx.compose:compose-bom:2024.04.00"))
    implementation (platform("org.jetbrains.kotlin:kotlin-bom:1.9.0"))
    implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:$composeVersion")
    androidTestImplementation (platform("androidx.compose:compose-bom:2024.04.00"))
    debugImplementation ("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:$composeVersion")

    // Compose UI
    implementation ("de.mr-pine.utils:zoomables:1.4.0")
    implementation ("androidx.compose.material3:material3:1.2.1")
    implementation ("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation ("androidx.navigation:navigation-compose:2.7.7")
    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    implementation ("com.google.accompanist:accompanist-placeholder-material3:0.30.1")

    // Kotlin Serialization
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Exo Player
    val media3Version = "1.3.0"

    implementation ("androidx.media3:media3-exoplayer:$media3Version")
    implementation ("androidx.media3:media3-exoplayer-dash:$media3Version")
    implementation ("androidx.media3:media3-ui:$media3Version")
    implementation ("androidx.media3:media3-exoplayer-hls:$media3Version")

    // Room
    val roomVersion = "2.6.1"

    implementation ("androidx.room:room-common:$roomVersion")
    implementation ("androidx.room:room-runtime:$roomVersion")
    ksp ("androidx.room:room-compiler:$roomVersion")
    implementation ("androidx.room:room-ktx:$roomVersion")

    // Firebase
    implementation (platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation ("com.google.firebase:firebase-crashlytics-ktx")
    implementation ("com.google.firebase:firebase-analytics-ktx")
    implementation ("com.google.firebase:firebase-messaging-ktx")

    // Network
    implementation ("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil-svg:2.6.0")

    // Datastore
    implementation ("androidx.datastore:datastore-preferences:1.0.0")

    // Project modules
    implementation (project(":core"))
    implementation (project(":data"))
}

