# medatarun
Universal executable model engine: define, run, and evolve living domain models for humans and AI. Storage-agnostic and model-driven, it auto-exposes REST, GraphQL, and MCP interfaces through an extensible model-execution runtime.

## Quickstart

See Installation for installation prerequisites (clone the repo, be sure to have Java 21+ and add an alias for CLI).

Create a new directory that will act as your project root. Il will store your domain model and data 

Create two folders:
- to store the domain model : `mkdir -p medatarun/models`
- to store the data : `mkdir -p medatarun/data`

Create an empty directory with a `package.json` file, like this: 

```package.json
{
  "name": "myproject",
  "medatarun": {
    "modeljson.repository.path": "medatarun/models",
    "data-md-file.repository.path": "medatarun/data"
  }
}
```

▶️ Launch server : `metadarun serve`

🖥️ Use with CLI

- CLI: list available commands, for example, `medatarun help`, `medatarun help model`
- CLI: get description of each command, for example, `medatarun help model inspect`

🌐 Use with RestAPI

- Rest API : get list of resources, commands and arguments `curl http://localhost:8080/api | jq`
- Rest API : `curl http://localhost:8080/api/model/createModel?id=mymode`, it's the same commands as CLI, you can use GET and send query parameters or POST with a form body.
- Rest API : `curl http://localhost:8080/api/model/inspect`

⭐ The fun part, use with AI  

```prompt
Using medatarun, create a new model named "contacts". We'll store basic informations for persons and companies
including a summary of what the company activity (in Markdown). 
Include persons linkedin profile and company websites. 
```

```prompt
Using medatarun, add to model the number of employees of companies and Glassdoor like informations.
```

```prompt
Search top biggest companies on the web, add them using medatarun with description in info field
add linkedin profiles of C-level. 
```



### Installation

Installation: there is no installer yet. You will be running with the source code directly

- clone the repository
- install Java 21+ (many ways to do that, one simple way is to install https://sdkman.io/ then, `sdk install java 21-tem`)
- create an alias in your `~/.bash_aliases` (bash on Linux or Windows with bash somewhere) or `~/.zshrc` (MacOS or Linux with zsh) and open new terminal (to load the alias)

```bash
alias medatarun='f() { 
    export MEDATARUN_APPLICATION_DATA=$(pwd);
    if [ $# -gt 0 ]; then
        /path/to/medatarun/gradlew -p /path/to/medatarun run --args="$*";
    else
        /path/to/medatarun/gradlew -p /path/to/medatarun run;
    fi
}; f'
```


Add to your Codex / ChatGPT / Claude configuration :

```toml
[mcp_servers.medatarun]
url = "http://localhost:8080/mcp"
```




## Build and run

This project uses [Gradle](https://gradle.org/).
To build and run the application, use the *Gradle* tool window by clicking the Gradle icon in the right-hand toolbar,
or run it directly from the terminal:

* Run `./gradlew run` to build and run the application.
* Run `./gradlew build` to only build the application.
* Run `./gradlew check` to run all checks, including tests.
* Run `./gradlew clean` to clean all build outputs.

Note the usage of the Gradle Wrapper (`./gradlew`).
This is the suggested way to use Gradle in production projects.

[Learn more about the Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).

[Learn more about Gradle tasks](https://docs.gradle.org/current/userguide/command_line_interface.html#common_tasks).

This project follows the suggested multi-module setup and consists of the `app` and `utils` subprojects.
The shared build logic was extracted to a convention plugin located in `buildSrc`.

This project uses a version catalog (see `gradle/libs.versions.toml`) to declare and version dependencies
and both a build cache and a configuration cache (see `gradle.properties`).