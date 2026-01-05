---
sidebar_position: 20
---

# Install and run from sources

You will be running with the source code directly. This is the option to use when you develop. 

## Requirements 

- Git : `git clone git@github.com:medatarun/medatarun.git`
- Java 21+ : [download Java here](https://adoptium.net/fr) or if you need to manage multiple versions of Java on your computer, use https://sdkman.io/ then, `sdk install java 21-tem`
- Bash or Zsh shell installed: already installed on Mac/Linux. On Windows you can use WSDL2

## Process

- Clone this repository
- Create an alias `medatarun` in your `~/.bash_aliases` (bash on Linux or Windows with bash installed) 
  or `~/.zshrc` (MacOS or Linux with zsh) and open new terminal (to load the alias)

```bash
alias medatarun='f() { 
    /path/to/medatarun/gradlew -p /path/to/medatarun :app:installDist
    export MEDATARUN_APPLICATION_DATA=$(pwd);
    /path/to/medatarun/app/build/install/medatarun/bin/medatarun "$@"
}; f'
```

## AI Agents

Add to your Codex / ChatGPT / Claude configuration :

```toml
[mcp_servers.medatarun]
url = "http://localhost:8080/mcp"
```

