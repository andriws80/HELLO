plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt") // Para habilitar KAPT (procesador de anotaciones en Kotlin)
}

android {
    namespace = "com.andriws.hello"
    compileSdk = 34

    buildFeatures {
        viewBinding= true
    }

    defaultConfig {
        applicationId = "com.andriws.hello"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // Asegurar runner de tests
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11 // Cambiado de 1_8 a 11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11" // Cambiado de 1.8 a 11
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
}

dependencies {
    // Glide para carga de imágenes  <-- ÚNICA declaración, con kapt y versión 4.16.0
    implementation(libs.glide)
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // Dependencias de AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    // Firebase
    implementation("com.google.firebase:firebase-auth-ktx:22.2.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.10.0")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation("com.google.firebase:firebase-messaging-ktx:23.3.1") // 🔥 Cloud Messaging

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")
    implementation ("com.google.android.gms:play-services-appsearch:16.0.1")

    // Dependencias para pruebas
    testImplementation("junit:junit:4.13.2") // Pruebas unitarias
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // Pruebas en Android
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") // UI Testing
}

// Aplicar google-services al final
apply(plugin = "com.google.gms.google-services")