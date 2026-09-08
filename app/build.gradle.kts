plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

android {
    namespace = "app.saylune"
    compileSdk = 37
    // Pinned rather than left to the AGP default: the F-Droid build server must
    // resolve the same toolchain we did, or the APKs cannot be compared.
    buildToolsVersion = "37.0.0"

    // The schema, versioned, so a change to it is visible in a diff rather than only in
    // the generated code -- and so a migration has the two shapes it has to bridge.
    ksp { arg("room.schemaLocation", "$projectDir/schemas") }

    defaultConfig {
        applicationId = "app.saylune"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        // The engine ships for the two instruction sets real phones run. The other two the
        // library carries, x86 and x86_64, are emulators, and they cost 42 MB of the 72 the
        // archive holds. Splitting further -- one APK per architecture, which is what the
        // published precedent does -- halves what an arm64 phone downloads again, and waits
        // until there is a release to publish and a recipe to write.
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }
    }

    // The block of dependency metadata AGP adds is signed by Google and cannot be read
    // from the sources, so F-Droid refuses it.
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    // Release signing, driven by Gradle and never by the IDE -- building a release from an
    // IDE leaves the order of the archive's entries undetermined. The keystore and its
    // passwords live outside the repository and reach the build through the environment;
    // absent, there is simply no release config and the build signs with nothing rather
    // than failing, which is what F-Droid does anyway since it signs with its own key.
    signingConfigs {
        val store = System.getenv("SAYLUNE_KEYSTORE")
        if (store != null) {
            create("release") {
                storeFile = file(store)
                storePassword = System.getenv("SAYLUNE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("SAYLUNE_KEY_ALIAS")
                keyPassword = System.getenv("SAYLUNE_KEY_PASSWORD")
                // v2 alone is enough to install at minSdk 26, and AGP stops there. v3 is
                // what makes the key **replaceable**: it carries a proof, signed by the
                // current key, that a new one takes over, so a phone accepts an update
                // signed differently. Without it the key is the app's identity for good --
                // lost, nobody can be updated, only told to uninstall and lose their data.
                // It can only be armed while no release has shipped, which is now.
                enableV3Signing = true
            }
        }
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
            // AGP writes the repository's git state into the APK from 8.3 on. Two people
            // building the same commit would then produce two different files, which is
            // exactly the comparison a reproducible build rests on.
            vcsInfo { include = false }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
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
    testOptions {
        // The android.jar on the unit-test classpath is a stub whose every method throws.
        // `Trace` reads the clock the moment it is loaded, so anything that traces -- which
        // is every seam, on purpose -- could not be exercised in plain JVM at all. Returning
        // the type's default instead is what makes those seams provable without Robolectric,
        // which the facette `android` keeps out until a need asks for it. It never reaches a
        // running app: the real classes are there on the device.
        unitTests.isReturnDefaultValues = true
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

    // What was said, kept. The android wisdom prescribes Room, and the design asks for
    // things a store answers rather than a file: what an activity holds, in what order,
    // how many -- counts and orders being exactly what is recomputed rather than stored.
    // The audio stays in files beside it; only its path is a column.
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")

    testImplementation("junit:junit:4.13.2")
    // Android ships org.json as a stub that throws in unit tests. The real one, on the
    // test classpath only, so the port is checked against the same parser it will meet.
    testImplementation("org.json:json:20240303")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // What runs the acoustic model, and therefore what makes the app the app: without it
    // there are no pronunciation marks at all. MIT, and taken from Maven Central rather
    // than committed here, which is the distinction F-Droid draws -- a binary sitting in
    // the repository is refused, a dependency from a trusted Maven repository is not
    // (`../TODO.md`).
    //
    // The runtime is not the bet the acoustic model is: `bench/export.py` turns whichever
    // candidate wins into the same kind of file, and they all run on this.
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.20.0")
}
