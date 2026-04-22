# Database baseline tooling

Generate SQL initialiation files for each extension by reading a 
live reference database.

This must be used before releasing a version of Medatarun to ensure
that all database migrations end-up with the correct SQL Baseline for
PostgreSQL and SQLite. 

## How to use

First make sure that all new tables added from the latest version of 
the application are added to `tools/database_baseline/module_specs.py`.

**SQLite**

Launch Medatarun on the latest commit containing database changes before
release.

```bash
cd tools/database-baseline
uv run database-baseline-sqlite --db-path $ENV_DEV_SQLITE/data/database.db
```

Check that there is no différence between expected baseline as committed and
the file generated based on GIT diff.

Commit if needed.

**PostgreSQL**

Launch medatarun on the latest commit containing database changes before release
on a PostgreSQL database.

Medatarun PostgreSQL should run on a Docker named `postgres` with database
`medatarun` and schema `medatarun_dev`, with user and password set. Default
values are in the script. So generate a baseline with:

```bash
cd tools/database-baseline
uv run database-baseline-postgresql
```

Validate the changes, commit, push, merge.

## Quick start with uv

Install `uv` first if it is not already available on your machine. See https://docs.astral.sh/uv/

```bash
cd tools/database-baseline
uv venv
uv sync
```

Generate module init SQL files from a reference database:

```bash
uv run database-baseline \
  --dialect sqlite \
  --db-path /path/to/medatarun/data/database.db
```

Generate module init SQL files from a PostgreSQL schema:

```bash
uv run database-baseline-postgresql
```
