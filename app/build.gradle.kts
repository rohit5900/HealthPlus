plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.yourdomain.healthplus"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yourdomain.healthplus"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // Good to have for tests
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // If you plan to use ViewBinding (recommended over findViewById)
    // buildFeatures {
    //     viewBinding = true
    // }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // For ConstraintLayout if used
    implementation("androidx.activity:activity-ktx:1.8.0") // For modern Activity Result API

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.1")) // Or latest
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Google Sign-In SDK
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation(libs.androidx.activity)
    // <-- ADDED (Check for the latest version)
    implementation(platform("com.google.firebase:firebase-bom:32.7.1")) // Or latest
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") // For lifecycleScope
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3") // Or latest
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3") // Or latest
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3") // For Firebase await()

    // Testing dependencies (good to have from the start)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
