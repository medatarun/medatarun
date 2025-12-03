# DB extension

This extension helps you start a new model from an existing database. Building a model from scratch can take time, and
many structures you need already exist in the database you use every day. Importing a database schema gives you an
initial version of the model. You can then refine and adapt it inside Medatarun. The goal is simply to avoid starting
from an empty model and to make use of what already exists.

## What is JDBC

To connect to databases (and run an import), Medatarun uses JDBC.

JDBC is a standard way for Java programs to connect to databases. Each database needs its own “driver”, which is a small
file (a JAR) that explains to the application how to talk to that database. Using JDBC lets the application support many
database systems without changes, because each driver follows the same rules and exposes the same connection method.

A "JDBC URL" is like an Internet address, but for database connections. It identifies where your database is and how to
reach it. A typical example looks like:

`jdbc:postgresql://localhost:5432/myschema`

The exact format depends on the database engine, but all JDBC URLs follow this general pattern.

## Install your driver

In your Medatarun installation directory, create a `datasources` folder and a subfolder `jdbc-drivers`.

Download the JDBC driver for your database from its official website. PostgreSQL, MySQL, DuckDB, Snowflake and many
others distribute these drivers publicly. Make sure to download the JAR file version (not `.zip`, not `.exe`). Place the
JAR in the `jdbc-drivers` directory.

Note that some vendors distribute their driver as a `.zip` archive. In that case, extract the archive and locate the
actual `.jar` file — this is the file you must place in `jdbc-drivers`, not the `.zip` itself.

Then create a `drivers.json` file inside `datasources` that lists the drivers you want Medatarun to load. This file
tells the application which driver corresponds to which database and where its JAR is located. Medatarun does not detect
drivers automatically; this file is required.

```json
{
  "drivers": [
    {
      "id": "mysql",
      "name": "MySQL JDBC Driver",
      "jar": "mysql-connector-j-9.5.0.jar",
      "className": "com.mysql.cj.jdbc.Driver"
    },
    {
      "id": "postgresql",
      "name": "PostgreSQL JDBC Driver",
      "jar": "postgresql-42.7.8.jar",
      "className": "org.postgresql.Driver"
    },
    {
      "id": "duckdb",
      "name": "DuckDB JDBC Driver",
      "jar": "duckdb_jdbc-1.4.2.0.jar",
      "className": "org.duckdb.DuckDBDriver"
    },
    {
      "id": "snowflake",
      "name": "Snowflake JDBC Driver",
      "jar": "snowflake-jdbc-3.27.1.jar",
      "className": "net.snowflake.client.jdbc.SnowflakeDriver"
    }
  ]
}
```

- `id` is the name of the driver and must match the `jdbc:<id>:...` part of a JDBC connection.
- `name` is a human-readable name shown in the interface.
- `jar` is the exact name of the JAR file placed in `jdbc-drivers`.
- `className` is the main class of the driver, as published in the driver’s documentation.

Medatarun will load the drivers listed in this file and use them to establish database connections.

## Create database connections

In the `datasources` folder, create a `connections.json` file that will hold your database connections.

We do not store connections in your project directory. The `connections.json` file lives only in the Medatarun
installation directory, so credentials stay on your machine. In your projects, you share only the logical connection
names. This lets a team use the same names across environments while each member keeps their own local settings and
passwords.

The `connections.json` file has this format:

```json
{
  "connections": [
    {
      "name": "mydatabase",
      "driver": "postgresql",
      "url": "jdbc:postgresql://localhost:5432/myschema",
      "username": "your_username",
      "secret": {
        "storage": "RAW",
        "value": "your_secret"
      },
      "properties": {
      }
    }
  ]
}
```

- `name` is a logical name for your connection. This is the name you will use to do imports and will be shared in your
  project. This way, all your team members can rely on the same names.
- `driver` is the `id` of the driver
- `url` is the JDBC URL used to connect to the database from your environment. For example, with PostgreSQL, a URL often
  looks like: `jdbc:postgresql://localhost:5432/myschema`. Other databases use similar URLs, mostly differing by the
  prefix. Check your database documentation for the exact format.
- `username`: connexion username
- `secret` defines how the password is stored 
  - `storage`: for now the only option is RAW, meaning the password is stored in plain text
  - `value` is the stored password
- `properties` is a set of values you can pass to the driver on each connection. You must refer to your database vendor
  documentation to know the list of possible values.

Note about `secret`: the password is stored locally in the Medatarun installation directory and never in your project, 
so shared projects only expose logical connection names. In this first version, "storage": "RAW" is the only mode 
available. Additional storage modes will be introduced later.

## Checkup

Just to avoid misunderstandings, the expected directory and file organization is typically this one:

```text
<medatarun_install_dir>/
    datasources/
        connections.json
        drivers.json
        jdbc-drivers/
            mysql-connector-j-9.5.0.jar
            postgresql-42.7.8.jar
```

## Launch an import

Run the Import command from the UI, API or CLI, and provide the name of the connection in this format:
`datasource:<connection_name>`. Using the previous example, you should import `datasource:mydatabase`.

## Behaviour and limitations

A database schema is a physical structure, while Medatarun works with conceptual models. Databases store only what is
needed to run queries, and many modelling details do not appear in the schema. Because of this, some information
required for a complete model cannot be inferred automatically.

For example, a phone number may appear in a database as a simple `VARCHAR`, even though it would be a dedicated type (
like `PhoneNumber`) in a
model. Optional attributes may be stored as empty or `NULL` values, but the schema does not indicate whether they are
conceptually optional. Identifiers also differ: Medatarun expects each entity to have a single identifier, while a
database may define a composite primary key or none at all.

To ensure that the import always produces a usable starting point, we apply a set of rules:

Principles 

- each database table becomes an entity, and each column becomes an attribute
- each database column type (including differences in size or precision) becomes a separate model type
- nullable database columns are imported as optional attributes
- database tables with no columns are ignored
- foreign keys are imported as relationships as best-effort

Naming and origin

- because we cannot guess the model name to import, all models created from import will be named
  `<connection_name> (import <date>)` with an id of `<connection_name>-<uuid>` to distinguish multiple imports.
- When a model is created, its origin will be `datasource:<connection name>` so you can track back where the model
  comes from, even after you renamed it.

Primary keys

- if no primary key exists on a database table, the first column becomes the entity’s identifier
- if a composite primary key exists in the database table, only its first column becomes the entity's identifier

Foreign keys

- When foreign keys are imported, only one side of the relationship can be inferred. A foreign key tells us whether 
  the referencing column must contain a value, but it does not specify how many rows in the referenced table may 
  point to the same value. Database schemas do not store this information. 
  Because of this, the import sets the cardinality on the referencing side to 0 or 1 and marks the opposite side cardinality as undefined.

These rules provide a consistent starting point. You can then adjust the resulting model as needed inside Medatarun.