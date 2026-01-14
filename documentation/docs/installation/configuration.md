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

- `MEDATARUN_HOME` is the technical runtime directory used by Medatarun. It contains configuration files, logs, secrets
  and runtime-generated data.
- `MEDATARUN_APPLICATION_DATA` is the directory where Medatarun stores **your data** and projects.

It can be the same directory or not. Your choice. In most cases, you don’t need to set these variables. Defaults work
out of the box.

If you don't define them (the default options) they will be where you unzipped Medatarun.

## Server

You can configure server host and port. This acts when the server start, and also tell the CLI what server to connect
to.

<!-- See enums to copy/paste description -->

| Key                                                | Type    | Default value | Description                                                                                                                                                                                                                                                                                                                                                                                                                             |
|----------------------------------------------------|---------|---------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Server**                                         |         |               |                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `medatarun.public.base.url`                        | String  | `<generated>` | Public base URL of the Medatarun instance.<br />This is the externally visible URL used for generated links and redirects.<br />Override it when Medatarun is deployed behind a reverse proxy or accessed via a different hostname.<br/>If not set, it is derived from the server host and port (`http://<host>:<port>`).                                                                                                               |
| `medatarun.server.host`                            | String  | `0.0.0.0`     | Hostname or IP address the server binds to.                                                                                                                                                                                                                                                                                                                                                                                             |
| `medatarun.server.port`                            | Integer | `8080`        | TCP port the server listens on.                                                                                                                                                                                                                                                                                                                                                                                                         |
| **Auth**                                           |         |               |                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| `medatarun.auth.bootstrap.secret`                  | String  | `<generated>` | Bootstrap secret used to obtain the initial administrator access.<br />In most environments, this value does not need to be set.<br />If not provided, Medatarun generates a random secret at startup and prints it in the logs.<br /><br />In environments where startup logs are not accessible (for example when running in containers or managed platforms), you should explicitly set this value. Minimum length is 20 characters. |
| `medatarun.auth.jwt.trusted.issuers`               | CSV     |               | Comma separated list of accepted JWT issuers. Each name will be used to get further configuration.                                                                                                                                                                                                                                                                                                                                      |
| `medatarun.auth.jwt.trusted.issuer.xxx.issuer`     | String  |               | Required for every issuer. In the property name, replace `xxx` with one your issuer names defined in `medatarun.auth.jwt.trusted.issuers`. The value must be the exact name of `iss` claim as it appears in the JWT.                                                                                                                                                                                                                    |
| `medatarun.auth.jwt.trusted.issuer.xxx.jwks`       | URL     |               | Required for every issuer. Absolute URL of your issuer's JWKS. You can often get it from its `.well-known/openid-configuration` (`jwks_uri` in this document's JSON). Otherwise ask your provider. It is the URL where we'll download the public keys of your issuer to validate their tokens.                                                                                                                                          |
| `medatarun.auth.jwt.trusted.issuer.xxx.algorithms` | CSV     |               | Comma separated list of algorithms that this issuer will sign its JWT with. We only support `RS256` and `ES256`. If you let this empty (or don't provide it), it is assumed `RS256` only.                                                                                                                                                                                                                                               |
| `medatarun.auth.jwt.trusted.issuer.xxx.audiences`  | CSV     |               | Comma separated list of audiences that we need to check. If you let this empty (or not defined), no audience checks will be done. If you set some audiences, it is required that your JWT contains an `aud` with at least one of the audiences in this configuration.                                                                                                                                                                   |
| `medatarun.auth.jwt.jwks.cache.duration`           | seconds | 600           | Duration (in seconds) we keep public keys in our local cache. Each JWT needs to be validated against your issuer public key. This is the time we keep the keys until we ask for a fresh one.                                                                                                                                                                                                                                            |
| `medatarun.auth.ui.oidc.authority`                 | URL     |               | If you let this empty, it is assumed Medatarun's UI will use our built-in OIDC provider. Then this value will depend on your `medatarun.public.base.url` and you don't have to take care of it. If specified, our UI will call your IdP to sign your users (OpenId Connect). In this case you need to specify your OIDC provider's authority URL (from which we will auto-discover its OIDC configuration).                             |
| `medatarun.auth.ui.oidc.clientid`                  | String  |               | If you have configured an OIDC authority and registered Medatarun as a client application, this is the name of the _client id_ you used to register Medatarun in your OIDC Identity Provider.                                                                                                                                                                                                                                           |

Note: frontend OIDC configuration is independent of JWT validation configuration. The client-id is never used as a JWT
audience.

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
- **in `config/medatarun.properties`** file in `MEDATARUN_HOME` folder (this file doesn't exist in your download, you
  have to create it).

:::note

These configuration patterns are the ones we test.

Medatarun relies on the Java MicroProfile Config specification,
with [Smallrye config as implementation choice](https://smallrye.io/smallrye-config/Latest/config/getting-started/).
This is mentioned here so you know exactly what is underneath and
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

In your `MEDATARUN_HOME` (install directory), file `config/logback.xml` will be read for Logback configuration.

Configuration file content is explained in [LogBack documentation here](https://logback.qos.ch/manual/index.html).
