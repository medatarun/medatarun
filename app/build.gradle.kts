plugins {
    // Apply the shared build logic from a convention plugin.
    // The shared code is located in `buildSrc/src/main/kotlin/kotlin-jvm.gradle.kts`.
    id("buildsrc.convention.kotlin-jvm")

    // Apply the Application plugin to add support for building an executable JVM application.
    application

    // Apply Kotlin Serialization plugin from `gradle/libs.versions.toml`.
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    // Project "app" depends on project "utils". (Project paths are separated with ":", so ":utils" refers to the top-level "utils" project.)
    implementation(project(":utils"))
    implementation(project(":libs:model"))
    implementation(project(":libs:lang"))
    implementation(project(":libs:auth"))
    implementation(project(":extensions:config"))
    implementation(project(":extensions:modeljson"))
    implementation(project(":extensions:data-md-file"))
    implementation(project(":extensions:db"))
    implementation(project(":extensions:frictionlessdata"))
    implementation(libs.kotlinReflect)
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.ktorServerCore)
    implementation(libs.ktorServerNetty)
    implementation(libs.ktorServerContentNegotiation)
    implementation(libs.ktorSerializationKotlinxJson)
    implementation(libs.ktorServerSse)
    implementation(libs.ktorClientCore)
    implementation(libs.ktorClientCio)
    implementation(libs.ktorClientContentNegotiation)
    implementation(libs.mcpKotlinServer)
    implementation(libs.bundles.slf4jImpl)
    implementation(libs.kotlinxHtml)
    implementation(libs.bundles.commonmark)
    implementation(libs.ktorServerStatusPages)
    implementation(libs.microprofileConfigApi)
    implementation(libs.smallryeConfig)
    testImplementation(kotlin("test"))
}

application {
    // Define the Fully Qualified Name for the application main class
    // (Note that Kotlin compiles `App.kt` to a class with FQN `com.example.app.AppKt`.)
    mainClass = "io.medatarun.AppKt"
}

distributions {
    main {
        distributionBaseName = "medatarun"
    }
}


val copyWebBuild by tasks.registering(Copy::class) {
    dependsOn(":ui:viteBuild")
    from(project(":ui").layout.projectDirectory.dir("dist"))
    into(layout.buildDirectory.dir("resources/main/static"))
}

tasks.named("processResources") {
    dependsOn(copyWebBuild)
}
