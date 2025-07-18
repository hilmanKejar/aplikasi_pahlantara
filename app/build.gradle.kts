plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.aplikasi_pahlantara"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.aplikasi_pahlantara"
        minSdk = 24
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
// AndroidX Libraries
    implementation("androidx.core:core-ktx:1.13.1") // Jika Anda menggunakan Kotlin
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.12.0") // Untuk Material Design Components
    implementation("androidx.recyclerview:recyclerview:1.3.2")// Untuk RecyclerView
    implementation("androidx.cardview:cardview:1.0.0") // Untuk CardView

    // Third-party Libraries
    implementation("com.android.volley:volley:1.2.1") // Untuk HTTP Networking
    implementation("com.github.bumptech.glide:glide:4.12.0") // Untuk Image Loading
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0") // Annotation Processor untuk Glide

    // Testing Libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}