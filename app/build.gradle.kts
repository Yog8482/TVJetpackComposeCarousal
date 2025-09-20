plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose) // Apply the Kotlin Compose Compiler plugin
}

android {
    namespace = "com.example.tvcorousalwithjetcompose"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.tvcorousalwithjetcompose"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        // Reference the renamed version alias for clarity
        kotlinCompilerExtensionVersion = libs.versions.composeCompilerExtension.get()
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)

    // Activity Compose for ComponentActivity
    implementation(libs.androidx.activity.compose)

    // Compose UI Tooling for @Preview and advanced layout inspection
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Compose Animation
    implementation(libs.androidx.compose.animation)

    // Jetpack Media3 (replaces ExoPlayer)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
}