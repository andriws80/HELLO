plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") // Firebase Services
    id("kotlin-kapt") // ✅ KSP sin versión aquí
}

android {
    namespace = "com.andriws.hello"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.andriws.hello"
        minSdk = 23
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true // ✅ Habilita DataBinding
    }
}

dependencies {
    // 🔥 Firebase (Se usa BoM para manejar versiones automáticamente)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)       // Autenticación Firebase
    implementation(libs.firebase.firestore.ktx)  // Firestore
    implementation(libs.firebase.storage.ktx)    // Almacenamiento Firebase
    implementation(libs.firebase.messaging.ktx)  // ✅ Ahora coincide con libs.versions.toml

    // 🔹 UI y utilidades
    implementation(libs.circleimageview)
    implementation(libs.glide)
    kapt(libs.ksp) // ✅ Usa la versión correcta de Glide Compiler


    // 🔹 Google Play Services
    implementation(libs.play.services.auth)

    // 🔹 AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material) // ✅ Ahora coincide con libs.versions.toml
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity.ktx)

    // 🔹 Biblioteca para cargar imágenes fácilmente
    implementation(libs.picasso)
    implementation(libs.play.services.auth.v2070)


    // 🔹 Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core) // ✅ Ahora coincide con libs.versions.toml
}


