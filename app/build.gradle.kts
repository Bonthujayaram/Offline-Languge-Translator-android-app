plugins {
   id("com.android.application")
    id("com.google.gms.google-services")
    kotlin("android") version "1.9.0"

        }

android {
    compileSdk=33


    namespace = "com.example.languagetranslatorapp"

    defaultConfig {
        applicationId = "com.example.languagetranslatorapp"
        minSdk = 24
        targetSdk= 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildToolsVersion = "34.0.0"
}

dependencies {
    implementation ("com.google.android.gms:play-services-maps:17.0.1")
    implementation("androidx.core:core:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1") 
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.mlkit:translate:17.0.1")
    implementation("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:1.9.10")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    androidTestImplementation("androidx.test.ext:junit:1.2.0-alpha01")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}