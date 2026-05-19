plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(libs.slf4j)
    implementation(project(":libs:platform-kernel"))
    implementation(project(":libs:lang"))
    implementation(libs.openTelemetryAutoconfigureSdk)
    implementation(libs.openTelemetryExporterOtlp)
    implementation(libs.openTelemetrySemconv)
    testImplementation(kotlin("test"))
    testImplementation(libs.logback)
}
