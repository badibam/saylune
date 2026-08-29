plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "app.speakup"
    compileSdk = 37
    // Pinned rather than left to the AGP default: the F-Droid build server must
    // resolve the same toolchain we did, or the APKs cannot be compared.
    buildToolsVersion = "37.0.0"

    defaultConfig {
        applicationId = "app.speakup"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // PNG crunching varies from machine to machine, which would break
            // the reproducible build F-Droid verifies against.
            isCrunchPngs = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        // Only for BuildConfig.DEBUG, which is what holds the debug panel out of a
        // release. No secret is ever put here (`docs/reference.md`, "Les clés d'API").
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(platform("androidx.compose:compose-bom:2026.06.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")

    // The keys the user brings, kept encrypted. DataStore rather than
    // SharedPreferences is what the android wisdom prescribes for light
    // preferences; the cipher key itself lives in the Keystore, not here.
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    testImplementation("junit:junit:4.13.2")
    // Android ships org.json as a stub that throws in unit tests. The real one, on the
    // test classpath only, so the port is checked against the same parser it will meet.
    testImplementation("org.json:json:20240303")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // The embedded analysis is still a proof of concept, and which engine the
    // app ships with is not decided. Held to the debug build so that no release
    // carries a native dependency on a bet -- and so that F-Droid is not owed an
    // answer about a prebuilt AAR before the measure that would justify it.
    debugImplementation("com.microsoft.onnxruntime:onnxruntime-android:1.20.0")
}
