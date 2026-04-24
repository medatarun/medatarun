# Checklist before release

# [ ] upgrade dependencies Java side

```bash
./gradlew dependencyUpdates
```

# [ ] upgrade dependencies UI

```bash
cd ui;
pnpm outdated --long
```

# [ ] upgrade dependencies Documentation

```bash
cd documentation;
pnpm outdated --long
```

## [ ] upgrade dependencies on tools

```bash
uv --project tools/database-baseline tree --outdated
uv --project tools/database-baseline lock --upgrade
uv --project tools/testing-e2e tree --outdated
uv --project tools/testing-e2e lock --upgrade
uv --project tools/ui-update-action-registry tree --outdated
uv --project tools/ui-update-action-registry lock --upgrade
```

### Other things 

- [ ] test migrations from the previous version (documentation to write about this).
- [ ] ensure that the database baseline had been generated. See `tools/database-baseline` project README.
- [ ] launch all end-to-end tests and all unit tests (SQLite and PostgreSQL included) with `tools/testing-e2e`. 
- [ ] Make sure that the `CHANGELOG.md` file contains everything needed.
