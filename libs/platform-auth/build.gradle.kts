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
    implementation(libs.auth0JwksRsa)
    implementation(libs.slf4j)
    implementation(project(":libs:platform-kernel"))
    implementation(project(":libs:platform-actions"))
    implementation(project(":libs:platform-security"))
    implementation(project(":libs:platform-type-system"))
    implementation(project(":libs:platform-type-commons"))
    implementation(project(":libs:platform-storage-db"))
    implementation(project(":libs:platform-storage-db-sqlite"))
    implementation(project(":libs:lang"))
    testImplementation(kotlin("test"))
    testImplementation(project(":libs:platform-storage-db-postgresql"))
    testImplementation(project(":libs:platform-storage-db-testkit"))
    testImplementation(libs.testcontainersPostgresql)
    testImplementation(libs.jimfs)
    testImplementation(libs.logback)
}
