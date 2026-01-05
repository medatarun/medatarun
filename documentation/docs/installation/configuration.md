---
sidebar_position: 50
---

# Configure Medatarun

## Directories

When you launch Medatarun, it looks for two environment variables

- `MEDATARUN_HOME` is where application **runs** (where you store database drivers, manage your configurations, see your logs, etc.)
- `MEDATARUN_APPLICATION_DATA` is where **your** data (or projects) lives. It is where application **stores**.

It can be the same directory or not. Your choice.

If you don't define them (the default options) they will be where you unzipped Medatarun.

## Server

You can configure server host and port. This acts when the server start, and also tell the CLI what server to connect to.

- `medatarun.server.port`: server port (defaults to `8080`)
- `medatarun.server.host`: server listening host (defaults to `0.0.0.0`)

## How to configure

Medatarun reads configuration properties from multiple configuration sources in this order:

- **as Java properties on the command line**: For example `medatarun -Dmedatarun.server.port=8081 serve`
- **as environment variables**: Here, you must define them in uppercase, replacing the doc `.` with underscores `_` (for example `MEDATARUN_SERVER_PORT=8080`)
- **in `.env` files** in `MEDATARUN_HOME`
- **in `medatarun.properties`** file in `MEDATARUN_HOME` folder

:::note

Those are guaranteed usages. Other places for configuration exist but we can not guarantee everything.

Good to know, we use standard Java's Microprofile configuration with [Smallrye config as implementation choice](https://smallrye.io/smallrye-config/Latest/config/getting-started/).
If you experience any issue, please look at Smallrye documentation.

:::