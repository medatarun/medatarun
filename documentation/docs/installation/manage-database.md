---
sidebar_position: 55
---

# Manage application database

If you do not configure anything, Medatarun uses [SQLite](https://sqlite.org/) by default. Just
follow [installation steps](./install-from-distribution.mdx) and you are done.
That is all. No database settings are required.

This is the easiest way to start:

- no database server to install
- no user/password to create
- data is stored in a local file

## When to switch to PostgreSQL

SQLite stores data in one local file (`$MEDATARUN_HOME/data/database.db`).

It is OK for a small setup and low usage, but there are limits to this mode:

- the database is tied to one machine
- multiple Medatarun servers cannot safely share the same SQLite file
- limited write concurrency under higher load
- no centralized database backup/restore workflow

This happens often faster with AI agents: they make API calls continuously, at a
higher frequency than interactive human usage, and this generates more data and
history.

So, Medatarun supports [PostgreSQL](https://www.postgresql.org/).
PostgreSQL is a shared database server and is the recommended option 
for multi-server and production setups.

## Switch to PostgreSQL

### Installation

There are various tutorials on how to install PostgreSQL on the web, for example
[here for Windows](https://neon.com/postgresql/postgresql-getting-started/install-postgresql), or [macOS](https://neon.com/postgresql/postgresql-getting-started/install-postgresql-macos) or [Linux](https://neon.com/postgresql/postgresql-getting-started/install-postgresql-linux).

Once installed, you end up with:

- a database (example: `medatarun`) and a schema (often `public`, or `medatarun_prod`)
- a database user (example: `medatarun_user`)
- a password for this user

Depending on your installation, you may have other parameters.

### Connect to the database

Behind the scenes, Medatarun uses the [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/) to communicate with PostgreSQL.

The driver expects a database URL and a set of properties (for example `user` and `password`).

You can configure all this using Medatarun configuration mechanisms: with a properties file, environment variables, or
JVM `-D` options. See [the configuration reference](./configuration.md) on how
to pass configuration to Medatarun.

This is an example of a possible configuration:

```properties
# This tells Medatarun to use PostgreSQL instead of SQLite
medatarun.storage.datasource.jdbc.dbengine=postgresql
# This is the URL to your PostgreSQL installation
medatarun.storage.datasource.jdbc.url=jdbc:postgresql://localhost:5432/medatarun
# All properties after medatarun.storage.datasource.jdbc.properties. will be 
# passed down as-is to the driver.
# Here it means the driver will get user=medatarun_user and password=change-me
medatarun.storage.datasource.jdbc.properties.user=medatarun_user
medatarun.storage.datasource.jdbc.properties.password=change-me
```
This way you can configure most driver options.

### Restart Medatarun

Stop Medatarun and start it again with:

```bash
medatarun serve
```

### Check that startup is OK

If the server starts without database connection error, Medatarun is using
PostgreSQL.

## Same config with environment variables

If you prefer environment variables:

```bash
export MEDATARUN_STORAGE_DATASOURCE_JDBC_DBENGINE=postgresql
export MEDATARUN_STORAGE_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/medatarun
export MEDATARUN_STORAGE_DATASOURCE_JDBC_PROPERTIES_USER=medatarun_user
export MEDATARUN_STORAGE_DATASOURCE_JDBC_PROPERTIES_PASSWORD=change-me
medatarun serve
```

You can mix configuration sources: some values as environment variables, and others in properties (or other supported sources).
This is useful in real deployments, for example to keep secrets in environment variables while keeping stable settings in configuration files.

**Be careful**: environment variables cannot handle every configuration. 
For example, if you need to pass a `currentSchema` to PostgreSQL, you need
to do it in the JDBC URL. When converting environment variables to 
database config, we have to convert to lower case, then, `currentSchema` gets
converted to `currentschema` and ignored by PostgreSQL. In those cases do that instead:

```bash
export MEDATARUN_STORAGE_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/medatarun?currentSchema=medatarun_prod
```



## Property reference

- `medatarun.storage.datasource.jdbc.dbengine`
    - `sqlite` (default) or `postgresql`
- `medatarun.storage.datasource.jdbc.url`
    - database JDBC URL
- `medatarun.storage.datasource.jdbc.properties.*`
    - optional JDBC properties (example: `user`, `password`, SSL options)
    - passed as-is to the PostgreSQL JDBC driver (same mechanism also applies to SQLite)

## Important note before switching

If you already used Medatarun with SQLite, changing these settings makes
Medatarun use a different database.

Plan your data move before switching in production.
