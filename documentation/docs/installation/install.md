---
sidebar_position: 1
---
# Install

There is no installer yet. You will be running with the source code directly.

## Requirements 

- Git
- Java 21+: many ways to do that, one simple way is to install https://sdkman.io/ then, `sdk install java 21-tem`
- Bash or Zsh shell installed: already installed on Mac/Linux. On Windows you can use WSDL2

## Process

- Clone this repository
- Create an alias `medatarun` in your `~/.bash_aliases` (bash on Linux or Windows with bash installed) 
  or `~/.zshrc` (MacOS or Linux with zsh) and open new terminal (to load the alias)

```bash
alias medatarun='f() { 
    /path/to/medatarun/gradlew -p /path/to/medatarun :app:installDist
    export MEDATARUN_APPLICATION_DATA=$(pwd);
    /path/to/medatarun/app/build/install/app/bin/app "$@"
}; f'
```

## AI Agents

Add to your Codex / ChatGPT / Claude configuration :

```toml
[mcp_servers.medatarun]
url = "http://localhost:8080/mcp"
```

