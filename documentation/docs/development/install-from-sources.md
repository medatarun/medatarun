---
sidebar_position: 20
---

# Install from source code

You will be running with the source code directly. This is the option to use
when you develop.

## Requirements

- Git : `git clone git@github.com:medatarun/medatarun.git`
- Java 21+ : [download Java here](https://adoptium.net/fr) or if you need to
  manage multiple versions of Java on your computer, use https://sdkman.io/
  then, `sdk install java 21-tem`
- Bash or Zsh shell installed: already installed on Mac/Linux. On Windows you
  can use WSDL2

## Process to run from sources

- Clone this repository
- Create an alias `medatarun` in your `~/.bash_aliases` (bash on Linux or
  Windows with bash installed) or `~/.zshrc` (MacOS or Linux with zsh) and open
  new terminal (to load the alias)
- Additionnal aliases maybe useful
    - `medatarun_buid` rebuilds the distribuction from your cloned directory
    - `medatarun_login <user> <pass>` that will set an environment variable for
      the CLI

```bash
MEDATARUN_SRC=/path/to/medatarun
alias medatarun='f() { 
    $MEDATARUN_SRC/app/build/install/medatarun/medatarun "$@"
}; f'
alias medatarun_login='f() { 
    unset MEDATARUN_AUTH_TOKEN
    export MEDATARUN_AUTH_TOKEN=$(
        medatarun auth login --username="$1" --password="$2" | jq -r '.access_token'
    )
}; f'
alias medatarun_build='f() {
    $MEDATARUN_SRC/gradlew -p $MEDATARUN_SRC/medatarun :app:installDist
}; f'
```

See [configuration](../installation/configuration.md) for environment variables
explanations.

# Build and run

This project uses [Gradle](https://gradle.org/).
To build and run the application, use the *Gradle* tool window by clicking the
Gradle icon in the right-hand toolbar,
or run it directly from the terminal:

* Run `./gradlew run` to build and run the application.
* Run `./gradlew build` to only build the application.
* Run `./gradlew check` to run all checks, including tests.
* Run `./gradlew clean` to clean all build outputs.

Note the usage of the Gradle Wrapper (`./gradlew`).
This is the suggested way to use Gradle in production projects.

[Learn more about the Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).

[Learn more about Gradle tasks](https://docs.gradle.org/current/userguide/command_line_interface.html#common_tasks).

This project follows the suggested multi-module setup and consists of the
following subprojects:

- `app` (main application)
- `libs/*` (main libraries)
- `extensions/*` (plugins, one directory per plugin).

The shared build logic was extracted to a convention plugin located in
`buildSrc`.

This project uses a version catalog (see `gradle/libs.versions.toml`) to declare
and version dependencies
and both a build cache and a configuration cache (see `gradle.properties`).

# Build and run documentation

Documentation is managed by Docusaurus.

Instructions to start, build the documentation are in `documentation/README.md`
file.

