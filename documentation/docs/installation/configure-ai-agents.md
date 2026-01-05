---
sidebar_position: 40
---

# Configure your AI Agent

You can tell your Codex / ChatGPT / Claude agent how to connect to Medatarun via the Model Context Protocol (MCP)

Depending on the agents, configuration will vary. Refer to your agent documentation to know how to configure it.
This is often referred as MCP or "tools" in their documentation.

The thing to remember is that the URL your agent needs is `http://localhost:8080/mcp`, it means
`http(s)://<yourhost>:<yourport>/mcp`, always `/mcp` at the end.

Note that we are only using the Streamable Http version.

## Example and test with ChatGPT's Codex

In your `~/.codex/config.toml` add

```toml
[mcp_servers.medatarun]
url = "http://localhost:8080/mcp"
```

Try to import something in Medatarun using UI's Import Button, or using CLI.

You can try this URL (it's from France public Open Data) : https://gitlab.com/opendatafrance/scdl/plats-menus-collectifs/-/raw/master/schema.json

```
./medatarun model Import --from="https://gitlab.com/opendatafrance/scdl/plats-menus-collectifs/-/raw/master/schema.json"
```

You don't know what it is? neither do I.

Open Codex and ask this question 

```text
I'm a business user from US, I don't understand french. Using Medatarun MCP, 
tell me what the model "plats-menus-collectifs" is and what it means, 
what's inside, what it is used for
```

Answer

```markdown
For a US business user: the plats-menus-collectifs model is a standardized data 
schema for “collective catering” dishes (meals served by public institutions and large
cafeterias). It defines a consistent way to publish what dishes are served, 
where they come from, and what they contain.

What it means

- A shared data dictionary for dish composition, sourcing, labels, allergens, 
  and publication metadata.
- Makes datasets comparable and auditable across organizations.

What’s inside

- One dataset/entity with fields covering:
  - Who publishes the data (organization name + official identifier).
  - Who produced the dish (establishment name + identifier).
  - Dish identity (dish code, dish name).
  - Ingredients/products and suppliers (names, supplier IDs, location codes).
  - Quality labels (e.g., organic, protected origin), additives, allergens.
  - GMO/irradiation flags.
  - Nutrition info (e.g., fat, carbs, salt per 100g).
  - Publication and last-updated timestamps.

What it’s used for

- Publishing and validating open data for collective catering menus.
- Compliance reporting on sourcing and quality.
- Transparency for stakeholders (citizens, auditors, regulators).
- Analytics across suppliers, regions, and time (sourcing, nutrition, sustainability).

```
