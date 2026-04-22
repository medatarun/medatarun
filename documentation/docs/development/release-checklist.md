# Checklist before release

- [ ] test migrations from the previous version (documentation to write about this).
- [ ] ensure that the database baseline had been generated. See `tools/database-baseline` project README.
- [ ] launch all end-to-end tests and all unit tests (SQLite and PostgreSQL included) with `tools/testing-e2e`. 
- [ ] Make sure that the `CHANGELOG.md` file contains everything needed.
