plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(project(":libs:lang"))
    implementation(project(":libs:platform-storage-db"))
    implementation(project(":libs:platform-storage-db-sqlite"))
    implementation(project(":libs:platform-storage-db-postgresql"))
    implementation(libs.testcontainers)
    implementation(libs.testcontainersPostgresql)
}
