plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("androidx.navigation.safeargs.kotlin") // Tambahkan plugin untuk Safe Args
}

android {
    namespace = "com.example.dompetkeluarga"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.dompetkeluarga"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        viewBinding = true
    }
}

dependencies {
    // AndroidX Core & Material Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase
    implementation(libs.play.services.auth)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)

    // Navigation (Fragment & UI KTX)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Material3 (Optional for modern UI)
    implementation(libs.androidx.material3.android)

    // Media3 (if needed for media handling)
    implementation(libs.androidx.media3.common.ktx)

    // UI Enhancements
    implementation(libs.github.glide)
    implementation(libs.squareup.picasso)
    implementation(libs.circleimageview)
    implementation(libs.androidx.work.runtime)
    // Testing Dependencies
    testImplementation(libs.junit)
    implementation(libs.androidx.swiperefreshlayout)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
