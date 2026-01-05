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

## Logging configuration

When running in server mode (`medatarun serve`) the default strategy is to log info, warnings and errors in the 
standard console (stdio).

When running in CLI mode (`medatarun command...`) strategy is to write warnings and errors on error console (stderr), 
and infos in standard console (stdio).

If this is ok for you, you can stop here. 

This can be changed to suit your integration needs.

Medatarun uses [SLF4J](https://www.slf4j.org/) and [LogBack](https://logback.qos.ch/) for logging.

In your `MEDATRUN_HOME` (install directory), file `config/logback.xml` will be read for Logback configuration.

Configuration file content is explained in [LogBack documentation here](https://logback.qos.ch/manual/index.html).
