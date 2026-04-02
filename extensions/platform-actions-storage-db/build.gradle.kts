plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.slf4j)
    implementation(project(":libs:lang"))
    implementation(project(":libs:platform-kernel"))
    implementation(project(":libs:platform-actions"))
    implementation(project(":libs:platform-security"))
    implementation(project(":libs:platform-type-commons"))
    implementation(project(":libs:platform-type-system"))
    implementation(project(":libs:platform-storage-db"))
    implementation(project(":libs:platform-storage-db-sqlite"))
    testImplementation(kotlin("test"))
    testImplementation(project(":libs:platform-storage-db-testkit"))
    testImplementation(libs.jimfs)
    testImplementation(libs.logback)
}
