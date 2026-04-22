# Database baseline tooling

Generate modules init SQL files from a reference database

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
