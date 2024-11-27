plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.example.single_lottery"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.single_lottery"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.google.firebase.auth)
    implementation(libs.firebase.appcheck)
    implementation(libs.firebase.installations)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.bom.v3360)
    implementation(libs.firebase.messaging)
    implementation(libs.glide)
    implementation(libs.recyclerview)
    implementation(libs.play.services.maps.v1900)
    implementation(libs.play.services.location)
    implementation(libs.core.v341)
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation(libs.firebase.appcheck.debug)
    implementation("com.google.android.material:material:1.12.0")

    annotationProcessor(libs.compiler)
    implementation(libs.picasso)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}