---
sidebar_position: 30
---

# Use Medatarun modules

Each release publishes the Medatarun modules (group `io.medatarun`) in GitHub Packages.

GitHub Packages requires a token, even for public packages. Create a personal access token (classic) with the scope `read:packages`. Store it in `~/.gradle/gradle.properties`, never in a repository:

```properties
gpr.user=<your GitHub user name>
gpr.key=<your token>
```

In GitHub Actions, use `GITHUB_ACTOR` and `GITHUB_TOKEN` with `permissions: packages: read`. The package must grant access to your repository ("Manage Actions access"). Otherwise, use a personal access token stored in a secret.

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.pkg.github.com/medatarun/medatarun")
            credentials {
                username = providers.gradleProperty("gpr.user")
                    .orElse(providers.environmentVariable("GITHUB_ACTOR")).orNull
                password = providers.gradleProperty("gpr.key")
                    .orElse(providers.environmentVariable("GITHUB_TOKEN")).orNull
            }
        }
    }
}
```

```kotlin
// build.gradle.kts
dependencies {
    implementation("io.medatarun:app:0.17.0")
    testImplementation("io.medatarun:platform-storage-db-testkit:0.17.0")
}
```
