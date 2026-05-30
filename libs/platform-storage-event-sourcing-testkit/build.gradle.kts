plugins {
    id("buildsrc.convention.kotlin-jvm")
}

dependencies {
    implementation(project(":libs:lang"))
    implementation(project(":libs:platform-storage-event-sourcing"))
    implementation(libs.kotlinReflect)
    implementation(libs.kotlinxSerialization)
    implementation(kotlin("test-junit5"))
}
