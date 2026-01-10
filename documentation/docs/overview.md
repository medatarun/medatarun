---
sidebar_position: 2
---
# Overview

You’ll need the `medatarun` command installed. See the [installation guide](./installation/install-from-distribution) for setup.

## Basic understanding

Medatarun manipulates Models.

Models are [_conceptual data models_](https://en.wikipedia.org/wiki/Conceptual_schema). They describe meaning, structure and constraints of data. They are not database schemas or implementation details.

A Model contains entities and relationships. Entities and relationships can have attributes.

The goal here is to understand the meaning, constraints, subtleties of the data (independently of storage or runtime concerns).

Medatarun exposes a set of _actions_, grouped by domain. Actions are the single primitive of the system.

UI, CLI, REST API or an AI agent using the MCP server all rely on the same _actions_. What you do with UI can be
done with CLI, API or an agent. 

_Note: if you are familiar with DDD concepts, you can think of a Model as a _bounded context_._

## 🌐 Use with UI: human understanding and manual management

UI provides business and data users a clear view of your models. Use it in your meetings, reviews as a base
and shared understanding. 

Open http://localhost:8080 to view and manage your models.

The command palette lets you run resource commands and provide parameters when needed.

For example, we can create a model by importing an existing schema. Open the Commands palette, 
choose model > import and in the "from" field add [this URL](https://raw.githubusercontent.com/etalab/schema-irve/master/datapackage.json).
This is a TableSchema-style model definition (in French, everything in French is complicated). 

On the Models page, you'll see a new available model, click and browse entities, types and documentation.

Now you can explain what the data in [this CSV file](https://raw.githubusercontent.com/etalab/schema-irve/v2.3.0/statique/exemple-valide-statique.csv) means.

Everything is French and you don't understand anything? See below.


## ⭐ Use with your AI agent 

See [how to connect your AI Agent to Medatarun](./usages/mcp-usage) if needed.

### Use with your agent

Run your AI Agent; it will call Medatarun through MCP on its own. For example:

Imagine a business user starting something:

```prompt
Can you set up a ‘contacts’ thing? I just want people with their email, name, job, phone if we have it, LinkedIn, 
and which company they work at. For each company, keep its name, website, domain, and a short description of what it does.
```

Note how "not very precise" this request is, at least, not as precise as a developer or data analyst would. 

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

Remember you can view and browse everything using user interface.

Now, do you remember the French data example? what if you just ask:

```prompt
Translate model "datapackage-irve", entities and attribute name and descriptions (do not change ids)?
```

We asked an AI to modify our models. Sometimes it produces incorrect or unwanted changes.

When model definitions are on GIT (when `MEDATARUN_APPLICATION_DATA` points to a Git repo), changes can be tracked and rolled back.

Medatarun also provides internal versioning: model snapshots, version history, and comparison tools.

## 🌐 Use with RestAPI

- List available action groups, actions and arguments `curl http://localhost:8080/api | jq`
- Execute a command on a resource by POST on `http://localhost:8080/api/<actionGroup>/<action>` and pass parameters in Json format in the body.
- Rest API : `curl http://localhost:8080/api/model/createModel?id=mymode`, it's the same commands as CLI, you can use GET and send query parameters or POST with a form body.
- Rest API : `curl http://localhost:8080/api/model/inspect`

## 🖥️ Use with CLI : CI/CD or manual mode, 

- `medatarun help`: get basic help and available resources 
- `medatarun help <actiongroup>`: list available commands on a group, for example: `medatarun help model`
- `medatarun help <actiongroup> <action>`: get description of a command, for example: `medatarun help model inspect`
- `medatarun <actiongroup> <action>`: runs an action, parameters are passed by `--key=value`. Complex values shall be in Json format.

