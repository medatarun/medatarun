# Quickstart

You’ll need the `medatarun` command installed. See the [installation guide](./install.md) for setup.

Create a directory that will hold your [project data](./project_data.md), then start the server ▶️ `medatarun serve`

## Resources and commands

Medatarun exposes a set of _resources_. Each resource provides _commands_, sometimes with parameters.

UI, CLI, REST API or an AI agent using the MCP server all rely on the same model.

## 🌐 Use with UI: human understanding and manual management

UI provide business and data users a clear view of your models. Use it in your meetings, reviews as a based
and shared understanding. 

Open http://localhost:8080 to view and manage your models.

The command palette lets you run resource commands and provide parameters when needed.

## ⭐ Use with your AI agent: the fun and productive way 

### Optional: update your AI AGENTS.md

If your AI Agent doesn't support MCP Instructions ([see column Instructions on this table](https://modelcontextprotocol.io/clients))
you'll need to create or add usage instructions in an `AGENTS.md` file. Otherwise, you AI may act strange.

```
echo "# AGENTS.md" > AGENTS.md
curl -s http://localhost:8080/api/config/aiAgentsInstructions >> AGENTS.md
```

### Use with your agent

Run your AI Agent; it will call Medatarun through MCP on its own. For example:

Imagine a business user starting something:

```prompt
Can you set up a ‘contacts’ thing? I just want people with their email, name, job, phone if we have it, LinkedIn, 
and which company they work at. For each company, keep its name, website, domain, and a short description of what it does.
```

Note how "not very precise" the request is, at least, no as precise as a developer or data analyst would. 

```prompt
Extend the model with company employee counts and Glassdoor-like information.
```

```prompt
I am a business user, explain "contacts";
```

```prompt
I am a developer, explain "contacts";
```

Another user, a developer, can continue work to adjust the model, entities, types

```prompt
Make linkedin_url required. Replace the ShortDescription type with a new Markdown type
```

And then

```prompt
Enrich company description. Explain that we need information of main activities and how the company relates to us (customer/partner/vendor/prospect/competitor).
```

Remember you view everything using AI.

## 🌐 Use with RestAPI

- List available resources, commands and arguments `curl http://localhost:8080/api | jq`
- Execute a command on a resource by POST on `http://localhost:8080/api/<resource>/<command>` and pass parameters in Json format in the body.
- Rest API : `curl http://localhost:8080/api/model/createModel?id=mymode`, it's the same commands as CLI, you can use GET and send query parameters or POST with a form body.
- Rest API : `curl http://localhost:8080/api/model/inspect`

## 🖥️ Use with CLI : CI/CD or manual mode, 

- `medatarun help`: get basic help and available resources 
- `medatarun help <resource>`: list available commands on a resource, for example: `medatarun help model`
- `medatarun help <resource> <command>`: get description of a command, for example: `medatarun help model inspect`
- `medatarun <resource> <command>`: runs a command, parameters are passed by `--key=value`. Complex values shall be in Json format.

