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

## Use the frontend package

Each release also publishes the frontend as the npm package `@medatarun/ui` in GitHub Packages. The version is the tag without the `v`. Pre-releases (`-alpha`, `-beta`, `-rc`) use the dist-tag `next`. Stable releases use `latest`.

The package contains the TypeScript sources of `ui/src`, not compiled code. The consuming application compiles them with Vite and `tsc`. Import paths are the same as inside Medatarun:

```ts
import { ViewLayoutHeader } from "@medatarun/ui/components/layout/ViewLayoutHeader.tsx";
import { createActionCtxVoid } from "@medatarun/ui/business/action-performer";
```

The `exports` field of `ui/package.json` maps `@medatarun/ui/<path>` to `src/<path>`. Each target is a list: Vite uses the first entry and resolves extensions and `index` files itself. TypeScript does not do that for `exports` targets, so it tries the next entries (`.ts`, `.tsx`, `/index.ts`). This supports imports with an extension, without an extension, and of a folder with an `index.ts`.

Add an `.npmrc` next to the consumer `package.json`. It contains no secret, only a reference to an environment variable:

```ini
@medatarun:registry=https://npm.pkg.github.com
//npm.pkg.github.com/:_authToken=${NODE_AUTH_TOKEN}
```

On a development machine, set `NODE_AUTH_TOKEN` to a personal access token (classic) with the scope `read:packages`.

In GitHub Actions, use `GITHUB_TOKEN` with `permissions: packages: read`, and give the package access to your repository ("Manage Actions access"):

```yaml
- uses: actions/setup-node@v7.1.0
  with:
    node-version: "24.15.0"
    registry-url: "https://npm.pkg.github.com"
    scope: "@medatarun"
- run: pnpm install --frozen-lockfile
  env:
    NODE_AUTH_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

Declare an explicit version in the consumer `package.json`:

```json
{
  "dependencies": {
    "@medatarun/ui": "0.17.0"
  }
}
```
