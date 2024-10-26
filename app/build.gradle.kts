plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.walkingstepcounter"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.walkingstepcounter"
        minSdk = 26
        targetSdk = 34
        versionCode = 2
        versionName = "2.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    viewBinding{
        enable = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("com.google.dagger:hilt-android:2.52")
    kapt ("com.google.dagger:hilt-compiler:2.52")

    //ssp and sdp
    implementation ("com.intuit.sdp:sdp-android:1.1.1")
    implementation ("com.intuit.ssp:ssp-android:1.1.1")

    //room db
    val room_version = "2.6.1"
    implementation("androidx.room:room-runtime:$room_version")
//    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")
    kapt ("androidx.room:room-compiler:$room_version")

    //viewModel
    // ViewModel and LiveData
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.3.9")
    //chart
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.google.android.play:app-update:2.1.0")

    // For Kotlin users also import the Kotlin extensions library for Play In-App Update:
    implementation("com.google.android.play:app-update-ktx:2.1.0")

}
