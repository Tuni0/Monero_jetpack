import org.gradle.kotlin.dsl.implementation

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.monero_jetpack"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.monero_jetpack"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "CLIENT_ID", "\"${project.properties["CLIENT_ID"]}\"")

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
        compose = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Compose Material 3
    implementation(libs.material3)

    // Compose UI
    implementation(libs.ui)
    // Import the BoM for the Firebase platform
    implementation(platform(libs.firebase.bom))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.google.firebase.auth)
    implementation(libs.firebase.auth.ktx)
    implementation("androidx.navigation:navigation-compose:2.8.9")


    implementation("androidx.media3:media3-exoplayer:1.6.1")
    implementation("androidx.media3:media3-ui:1.6.1")

    implementation (libs.accompanist.swiperefresh)
    implementation (libs.json)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation (libs.googleid)
    implementation (libs.play.services.auth)
    implementation(libs.firebase.firestore.ktx)
    implementation ("androidx.security:security-crypto:1.1.0-alpha07")



    implementation ("com.google.zxing:core:3.5.2")

    implementation ("com.google.mlkit:barcode-scanning:17.3.0")
    implementation ("com.google.android.gms:play-services-mlkit-barcode-scanning:18.2.0")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation (libs.accompanist.permissions)

    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.1")

    implementation (libs.androidx.lifecycle.viewmodel.ktx)
    implementation (libs.androidx.lifecycle.runtime.ktx.v260)
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation ("com.squareup.okhttp3:okhttp:4.10.0")

    implementation ("com.google.code.gson:gson:2.10.1")

    implementation ("io.github.ehsannarmani:compose-charts:0.1.2")
    implementation (libs.androidx.material.icons.extended)

    implementation (libs.accompanist.systemuicontroller)
    implementation (libs.androidx.lifecycle.runtime.ktx.v251) // or the latest version
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1") // if you're using ViewModel
    implementation ("androidx.lifecycle:lifecycle-process:2.5.1")

    // For Lottie animations
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // For sounds
    implementation("androidx.media3:media3-exoplayer:1.3.1")

}

