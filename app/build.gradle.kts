plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.byway"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.byway"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // AndroidX & Test
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // --- OAuth 로그인용 의존성 추가 ---
    // Google Play services
    implementation 'com.google.gms:google-services:4.3.15'
    implementation 'com.google.firebase:firebase-auth:22.0.0'
    implementation 'com.google.firebase:firebase-bom:32.0.0'
    implementation 'com.google.android.gms:play-services-auth:20.5.0'

    // Kakao SDK (User API)
    implementation "com.kakao.sdk:v2-user:2.15.0"
}

