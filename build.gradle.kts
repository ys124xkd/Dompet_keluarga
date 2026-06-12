buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Use double quotes instead of single quotes for strings
        classpath(libs.google.services)
        classpath(libs.androidx.navigation.safe.args.gradle.plugin)
    }
}
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}