---
sidebar_position: 10
---

# Install from distribution

Install and run from our .zip file 

## Requirements 

- Java 21+ : [download Java here](https://adoptium.net/fr) or if you need to manage multiple versions of Java on your computer, use https://sdkman.io/ then, `sdk install java 21-tem`
- If you intend to use CLI or follow command-line tutorials, 
  - on Windows you can use WSDL2
  - on Linux, Bash or Zsh shell are ok and already installed on Mac/Linux. 

## Download

- [Download Medatarun](https://github.com/medatarun/medatarun/releases/download/v0.0.1-alpha1/medatarun-0.0.1-alpha1.zip) or pick another release on [GitHub](https://github.com/medatarun/medatarun)
- Unzip where you want.
- Optional (for CLI users): add medatarun to your PATH

```bash
alias medatarun='f() { 
    /path/to/medatarun/gradlew -p /path/to/medatarun :app:installDist
    export MEDATARUN_APPLICATION_DATA=$(pwd);
    /path/to/medatarun/app/build/install/app/bin/medatarun "$@"
}; f'
```

## AI Agents

Add to your Codex / ChatGPT / Claude configuration :

```toml
[mcp_servers.medatarun]
url = "http://localhost:8080/mcp"
```

