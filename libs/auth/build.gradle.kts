plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(libs.kotlinxSerialization)
    implementation(libs.auth0JavaJwt)
    implementation(libs.slf4j)
    implementation(project(":libs:model"))
    testImplementation(kotlin("test"))
    testImplementation(libs.jimfs)
}