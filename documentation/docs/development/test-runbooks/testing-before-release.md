# Testing before release

## Testing migrations

TODO()

## Testing database baseline

### Regen database baslines

First make sure that all new tables of modules are added to `generate_sqlite.py`.

**SQLite**

Launch medatarun on the latest commit containing database changes before release.

```bash
cd tools/database-baseline
uv run database-baseline-sqlite --db-path $ENV_DEV_SQLITE/data/database.db
```

Check that there is no différence between expected baseline as committed and 
the file generated based on GIT diff.

Commit if needed. 

**PostgreSQL**

Launch medatarun on the latest commit containing database changes before release on a PostgreSQL database.