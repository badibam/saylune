plugins {
    id("com.android.application") version "9.3.1" apply false
    // Kotlin support ships inside AGP since 9.0 — no kotlin.android plugin.
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10" apply false
    // Room generates its data access code; KSP is what runs the generator. Since KSP 2.3.0
    // its version simply is the Kotlin version it is built against, so the two move as one.
    id("com.google.devtools.ksp") version "2.3.10" apply false
}
