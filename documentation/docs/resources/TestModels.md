---
sidebar_position: 999
---

# Test models

Here is a collection of models from various sources you can import to test medatarun.

## Schemas catalog

File [catalog.csv](catalog.csv) contains schemas to import into medatarun using the _import button_ in UI, or CLI
commands, Rest API or ask your connected IA agent. 

You can use Codex or another AI agent to import them all at once: 

```
Read the docs/resources/catalog.csv file yourself. Then mandatorily use the Medatarun MCP server to import all models listed in the url_raw column. Do not import them by any other means. Any import not performed through Medatarun is invalid.
```

The `url_raw` column from the file gives you URL to import.

- French schema taken from https://github.com/datagouv/schema.data.gouv.fr and especially the file `repertoires.yml`
  then converted to CSV with a direct link to the raw file. 



