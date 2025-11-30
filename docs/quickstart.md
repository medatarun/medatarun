# Quickstart

Prerequisites:

- Have `medatarun` command installed, see [installation guide](install.md).

Create a new directory that will act as your project root.

🖥️ Use with CLI

- CLI: list available commands, for example, `medatarun help`, `medatarun help model`
- CLI: get description of each command, for example, `medatarun help model inspect`

▶️ Launch server : `metadarun serve`

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

