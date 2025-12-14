// Top-level build file for HealthPay Keyboard
// NO HILT - Using manual dependency injection

plugins {
    id("com.android.application") version "8.1.1" apply false
    id("com.android.library") version "8.1.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    // NO HILT - Removed: id("com.google.dagger.hilt.android") version "2.48.1" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
