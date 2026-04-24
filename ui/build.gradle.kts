import org.siouan.frontendgradleplugin.infrastructure.gradle.InstallFrontendTask

plugins {
    alias(libs.plugins.frontendGradlePlugin)
}

frontend {
    nodeVersion = "22.12.0"
    assembleScript = "run build"
    verboseModeEnabled = true
}

// Detects if Gradle is running unit tests, in this case we don't need
// the frontend.
val runningUnitTests = gradle.startParameter.taskNames.any { taskName ->
    taskName == "test" || taskName.endsWith(":test")
}

tasks.register("viteBuild") {
    enabled = !runningUnitTests
    /**
     * Keep this task name stable because application packaging depends on `:ui:viteBuild`.
     * The actual frontend build is now delegated to the maintained frontend plugin.
     */
    if (!runningUnitTests) { dependsOn("assembleFrontend") }
    inputs.file("package.json")
    inputs.file("pnpm-lock.yaml")
    inputs.dir("src")
    inputs.file("index.html")
    outputs.dir(layout.projectDirectory.dir("dist"))
}
