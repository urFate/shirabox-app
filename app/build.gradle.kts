plugins {
    kotlin("android")
    kotlin("plugin.serialization") version "1.9.20"
    id ("com.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "org.shirabox.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "org.shirabox.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.2"

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

        getByName("debug") {
            applicationIdSuffix = ".debug"
            isDebuggable = true
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
}

dependencies {
    // Core dependencies
    val composeVersion = "1.6.7"

    implementation ("androidx.core:core-ktx:1.13.1")
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation ("androidx.activity:activity-compose:1.9.0")
    implementation ("androidx.compose.ui:ui:$composeVersion")
    implementation ("androidx.compose.ui:ui-graphics:$composeVersion")
    implementation ("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation (platform("androidx.compose:compose-bom:2024.05.00"))
    implementation (platform("org.jetbrains.kotlin:kotlin-bom:1.9.23"))
    implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation ("androidx.core:core-splashscreen:1.0.1")
    implementation ("androidx.graphics:graphics-core:1.0.0-rc01")

    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4:$composeVersion")
    androidTestImplementation (platform("androidx.compose:compose-bom:2024.05.00"))
    debugImplementation ("androidx.compose.ui:ui-tooling:$composeVersion")
    debugImplementation ("androidx.compose.ui:ui-test-manifest:$composeVersion")

    // Compose UI
    val richtextVersion = "1.0.0-alpha01"

    implementation ("de.mr-pine.utils:zoomables:1.4.0")
    implementation ("androidx.compose.material3:material3:1.2.1")
    implementation ("androidx.compose.material:material:$composeVersion")
    implementation ("androidx.compose.material:material-icons-extended:$composeVersion")
    implementation ("androidx.navigation:navigation-compose:2.7.7")
    implementation ("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    implementation ("com.google.accompanist:accompanist-placeholder-material3:0.30.1")
    implementation("com.halilibo.compose-richtext:richtext-ui-material3:$richtextVersion")
    implementation("com.halilibo.compose-richtext:richtext-markdown:$richtextVersion")
    implementation("com.halilibo.compose-richtext:richtext-commonmark:$richtextVersion")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")


    // Kotlin Serialization
    implementation ("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Exo Player
    val media3Version = "1.3.1"

    implementation ("androidx.media3:media3-exoplayer:$media3Version")
    implementation ("androidx.media3:media3-exoplayer-dash:$media3Version")
    implementation ("androidx.media3:media3-ui:$media3Version")
    implementation ("androidx.media3:media3-exoplayer-hls:$media3Version")

    // Firebase
    implementation (platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation ("com.google.firebase:firebase-crashlytics-ktx")
    implementation ("com.google.firebase:firebase-analytics-ktx")
    implementation ("com.google.firebase:firebase-messaging-ktx")

    // Image processing
    implementation ("io.coil-kt:coil-compose:2.5.0")
    implementation("io.coil-kt:coil-svg:2.6.0")

    // Network
    implementation("com.github.kittinunf.fuel:fuel:3.0.0-alpha1")

    // Datastore
    implementation ("androidx.datastore:datastore-preferences:1.1.1")

    // Project modules
    implementation (project(":core"))
    implementation (project(":data"))
}

