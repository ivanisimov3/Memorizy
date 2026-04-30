plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val memorizyBaseUrl = providers.gradleProperty("MEMORIZY_BASE_URL")
    .orElse(providers.environmentVariable("MEMORIZY_BASE_URL"))
    .orElse("http://10.0.2.2:8080/")
    .get()

android {
    namespace = "com.example.memorizy"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.memorizy"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "BASE_URL", "\"$memorizyBaseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        release {
            isMinifyEnabled = false
            manifestPlaceholders["usesCleartextTraffic"] = "false"
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
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.foundation)
    val roomVersion = "2.8.4"

    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    ksp("com.google.dagger:hilt-compiler:2.57.1")
    implementation("com.google.dagger:hilt-android:2.57.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    ksp("androidx.room:room-compiler:${roomVersion}")
    implementation("androidx.room:room-runtime:${roomVersion}")
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.common.jvm)

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.0.21"))

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test-jvm:1.10.2")
    testImplementation("io.mockk:mockk:1.14.6")
    testImplementation(libs.junit)
    testImplementation("androidx.room:room-testing:${roomVersion}")

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation("androidx.test:core-ktx:1.7.0")

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.leakcanary.android)

    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    implementation("androidx.datastore:datastore-preferences:1.0.0")

    implementation("androidx.work:work-runtime-ktx:2.9.0")

    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.25.0")

    // Source: https://mvnrepository.com/artifact/com.jsoizo/kotlin-csv
    implementation("com.jsoizo:kotlin-csv:1.10.0")

    // Source: https://github.com/patrykandpatrick/vico
    implementation("com.patrykandpatrick.vico:compose-m3:2.1.4")
}