plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(project(":libs:lang"))
    implementation(project(":libs:platform-storage-db"))
    implementation(project(":libs:platform-storage-db-sqlite"))
    implementation(project(":libs:platform-storage-db-postgresql"))
    implementation(kotlin("test-junit5"))
    implementation(libs.testcontainers)
    implementation(libs.testcontainersPostgresql)
}
