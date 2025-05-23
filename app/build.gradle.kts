plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.byway"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.byway"
        minSdk = 24
        targetSdk = 35
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
    // Firebase BOM - 버전 통일
    implementation(platform(libs.firebase.bom)) // libs.versions.toml에서 관리됨

    // Firebase (버전 없이 BOM 기준으로 가져감)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    // Google Play 서비스
    implementation(libs.play.services.auth)
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Kakao SDK
    implementation(libs.kakao.user)

    // Naver Maps
    implementation("com.naver.maps:map-sdk:3.21.0")

    // Retrofit & JSON
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.json:json:20240303")

    // AndroidX & 테스트
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}