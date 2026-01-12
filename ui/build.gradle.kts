plugins {
    alias(libs.plugins.gradleNodePlugin)
}

node {
    version.set("22.12.0")
    download.set(true)
    pnpmVersion.set("10.14.0")
}

/** Tells if we are running in Continuous Integration. GitHub Actions shall put that to true */
val isCI = System.getenv("CI") == "true"

tasks.register<com.github.gradle.node.npm.task.NpmTask>("viteBuild") {
    if (isCI) dependsOn(tasks.pnpmInstall)
    npmCommand.set(listOf("run", "build"))
    inputs.file("package.json")
    inputs.file("pnpm-lock.yaml")
    inputs.dir("src")
    inputs.file("index.html")
    outputs.dir(layout.projectDirectory.dir("dist"))
}