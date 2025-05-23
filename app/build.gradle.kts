plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") // Убедитесь, что версия Kotlin используется здесь
    id ("kotlin-parcelize")
}

android {
    namespace = "com.example.fitbite"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fitbite"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            isDebuggable = true
            buildConfigField("boolean", "USE_MOCK_STEP_PROVIDER", "true")
        }
        release {
            isDebuggable = false
            buildConfigField("boolean", "USE_MOCK_STEP_PROVIDER", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {
    implementation ("com.google.android.material:material:1.12.0")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-ktx:1.9.0")

    //implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    implementation ("com.google.android.gms:play-services-auth:19.0.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.0")
    implementation ("androidx.datastore:datastore-preferences:1.0.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("androidx.fragment:fragment-ktx:1.6.2")
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation ("com.auth0.android:jwtdecode:2.0.1")

    // Jetpack Compose BOM — для Kotlin 2.1.0
    implementation(platform("androidx.compose:compose-bom:2024.05.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.activity:activity-compose:1.9.0") // <= ok для 2.1.0
    implementation("androidx.compose.ui:ui-viewbinding")
}

