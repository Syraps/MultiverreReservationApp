// Root build.gradle.kts (Project-level)
buildscript {
    dependencies {
        // Ajoute le plugin Google Services si nécessaire (par sécurité)
        classpath("com.google.gms:google-services:4.4.0")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.google.services) apply false
}
