plugins {
    id("com.android.application") version "9.3.1" apply false
    // Kotlin support ships inside AGP since 9.0 — no kotlin.android plugin.
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10" apply false
}
