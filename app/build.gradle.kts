plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.cookeduitv.launcher"
    compileSdk = 35
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    defaultConfig {
        applicationId = "com.cookeduitv.launcher"
        minSdk = 23
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"
    }
}
