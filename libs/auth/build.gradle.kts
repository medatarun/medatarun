plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(libs.kotlinxSerialization)
    implementation(libs.auth0JavaJwt)
    implementation(libs.slf4j)
    implementation(libs.sqliteJdbc)
    implementation(project(":libs:lang"))
    implementation(project(":libs:model"))
    testImplementation(kotlin("test"))
    testImplementation(libs.jimfs)
}