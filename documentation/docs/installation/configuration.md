---
sidebar_position: 50
---

# Configuration options


:::tip
Medatarun starts with sensible defaults and requires no configuration for a basic setup.
This page documents the available options if you need to go further.
:::

## Directories

When you launch Medatarun, it looks for two environment variables

- `MEDATARUN_HOME` is the technical runtime directory used by Medatarun. It contains configuration files, logs, secrets and runtime-generated data.
- `MEDATARUN_APPLICATION_DATA` is the directory where Medatarun stores **your data** and projects.

It can be the same directory or not. Your choice. In most cases, you don’t need to set these variables. Defaults work out of the box.

If you don't define them (the default options) they will be where you unzipped Medatarun.

## Server

You can configure server host and port. This acts when the server start, and also tell the CLI what server to connect
to.

- `medatarun.server.port`: server port (defaults to `8080`)
- `medatarun.server.host`: server listening host (defaults to `0.0.0.0`)

<!-- See enums to copy/paste description -->

| Key                               | Type    | Default value | Description                                                                                                                                                                                                                                                                                                                                                                                                                             |
|-----------------------------------|---------|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Server**                        |         |               |                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `medatarun.public.base.url`       | String  | `<generated>` | Public base URL of the Medatarun instance.<br />This is the externally visible URL used for generated links and redirects.<br />Override it when Medatarun is deployed behind a reverse proxy or accessed via a different hostname.<br/>If not set, it is derived from the server host and port (`http://<host>:<port>`).                                                                                                               |
| `medatarun.server.host`           | String  | `0.0.0.0`     | Hostname or IP address the server binds to.                                                                                                                                                                                                                                                                                                                                                                                             |
| `medatarun.server.port`           | Integer | `8080`        | TCP port the server listens on.                                                                                                                                                                                                                                                                                                                                                                                                         |
| **Auth**                          |         |               |                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `medatarun.auth.bootstrap.secret` | String  | `<generated>` | Bootstrap secret used to obtain the initial administrator access.<br />In most environments, this value does not need to be set.<br />If not provided, Medatarun generates a random secret at startup and prints it in the logs.<br /><br />In environments where startup logs are not accessible (for example when running in containers or managed platforms), you should explicitly set this value. Minimum length is 20 characters. |

## CLI

The CLI uses the same configuration properties to determine which Medatarun instance to connect to.
The server host, port, and public base URL follow the same rules and defaults as the server configuration.

| Key                         | Type    | Default value | Description                                                                                                                       |
|-----------------------------|---------|---------------|-----------------------------------------------------------------------------------------------------------------------------------|
| **CLI**                     |         |               |                                                                                                                                   |
| `medatarun.public.base.url` | String  | `<generated>` | Base URL of the Medatarun server to connect to. If not set, it is derived from the server host and port (`http://<host>:<port>`). |
| `medatarun.server.host`     | String  | `0.0.0.0`     | Hostname or IP address of the Medatarun server to connect to. Ignored if     `medatarun.public.base.url` is present.              |
| `medatarun.server.port`     | Integer | `8080`        | TCP port of the Medatarun server to connect to. Ignored if `medatarun.public.base.url` is present.                                |
| `medatarun.auth.token`      | String  | `<none>`      | Authentication token used by the CLI when connecting to a Medatarun instance.                                                     |

The CLI authenticates to the Medatarun API using a pre-configured authentication token.
To learn more about authentication tokens: [Managing users](./manage-users.md)

## How to configure

Medatarun reads configuration properties from multiple configuration sources in this order:

- **as Java properties on the command line**: For example `medatarun -Dmedatarun.server.port=8081 serve`
- **as environment variables**: Here, you must define them in uppercase, replacing the doc `.` with underscores `_` (for
  example `MEDATARUN_SERVER_PORT=8080`)
- **in `.env` files** in `MEDATARUN_HOME`
- **in `medatarun.properties`** file in `MEDATARUN_HOME` folder

:::note

These configuration patterns are the ones we test.

Medatarun relies on the Java MicroProfile Config specification, with [Smallrye config as implementation choice](https://smallrye.io/smallrye-config/Latest/config/getting-started/). This is mentioned here so you know exactly what is underneath and 
where to look if you run into configuration-related issues.

As an open source project, we cannot test every possible setup or environment. If your configuration behaves 
unexpectedly, the SmallRye Config documentation is the right place to dig into the details.

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
