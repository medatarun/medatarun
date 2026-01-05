---
sidebar_position: 30
---

# Run Medatarun

## Launch the server

Simply run 

`medatarun serve`

You will see the logs on the console. To stop the server, CTRL+C or Kill.

Now you can browse on http://localhost:8080

See [configuration page](./configuration.md) for options.

## CLI Users

Once the server is launched, you can run commands against it. Examples:

```
medatarun config InspectJson
medatarun model Import --from="http://my.schemas/schema.json"
medatarun model Import --from="datasource:myapp"
medatarun model Inspect_Json --modelKey=mymodel
medatarun model Model_AddTag --modelKey=mymodel --tag="my tag"
``` 

for example

To get help: `medatarun help` will give you all the commands and parameters, which are the same commands as the API or what an MCP server can use.

