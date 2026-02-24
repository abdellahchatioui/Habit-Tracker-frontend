plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.habittracker"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.habittracker"
        minSdk = 24
        targetSdk = 36
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.kizitonwose.calendar:view:2.5.0")
    testImplementation(libs.junit)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}