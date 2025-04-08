plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("kotlin-parcelize")
}

android {
    namespace = "com.andriws.hello"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.andriws.hello"
        minSdk = 24
        targetSdk = 34 // Corregido: targetSdk debe ser 34 o superior para cumplir con las políticas de Google Play
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
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
    // AndroidX - Dependencias actualizadas (se mantienen las versiones estables actuales)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)

    // Firebase - Dependencias actualizadas (se mantienen las versiones estables actuales)
    implementation(platform(libs.firebase.bom)) // Corregido: Uso de platform() para el BoM
    implementation(libs.google.firebase.auth.ktx)
    implementation(libs.google.firebase.firestore.ktx)
    implementation(libs.google.firebase.storage.ktx)
    implementation(libs.google.firebase.messaging.ktx)

    // Navigation - Dependencias actualizadas (se mantienen las versiones estables actuales)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Glide para carga de imágenes (se mantiene la versión estable actual)
    implementation(libs.glide)
    kapt(libs.compiler)

    // CircleImageView (se mantiene la versión estable actual)
    implementation(libs.circleimageview)

    // Play Services - Dependencias actualizadas
    implementation(libs.play.services.auth.v2120)

    // Testing (se mantienen las versiones estables actuales)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // RecyclerView and CardView (se mantienen las versiones estables actuales)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.cardview)
    implementation(libs.flexbox)
}

apply(plugin = "com.google.gms.google-services")