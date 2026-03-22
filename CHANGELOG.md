# Changelog

## [Unreleased 0.9.0]

### Added

- Changelog file

### Changed - architecture

- Improved traceability by grouping together `actorId` + `actionInstanceId` into
  `traceabilityRecord`. `traceabilityRecord` keeps `actorId` and extends the
  concept of `actionInstanceId` to a more flexible `origin`.
  `origin=action:<actionInstanceId>`

### Changed - database

Database migration scripts from 0.8.0 (migrations will be manual until 1.0.0 is
released.)

- Column `model_events.action_id` renamed to `model_event.traceability_origin`
  with new storage format.

```
alter table model_event rename column traceability_origin2 to traceability_origin;
update model_event set traceability_origin = concat('action:', traceability_origin) where substring(traceability_origin, 0, 8) <> 'action:';
```

### Fixed

- Deleting local tags, global tags and groups of tags now removes included tags
  from everywhere on models and their elements.

## [0.8.0]

### Added

- Models can be marked canonical or system to separate the canonical view from "
  technical" models. Visual indicators on the user interface had been added.
- The tag system was rewamped and implemented with two différent governance
  modes: global tags (managed, strict permissions) and local tags for models.
  Added inline editor and user interface tools to manage global tags, local tags
  and tag groups.
- Search by tags and text is available on all channels. Filters can be combined.
  A user interface had been added for search.
- Comparison between models and their versions. Comparison can be structural
  only or full (with texts). Availble on CLI, API, MCP and UI. A specific UI
  screen had been made for visual comparisons.
- Finished relationship management and their roles.
- Model history implemented with event-sourcing. Models are now stored in the
  database only. This replaces the need for Git to do versionning. In the user
  interface a history page had been added to better show who did what between
  versions.
- A model export format had been added that matches the previous storage system.
  Exports are available on user interface and all other channels (API, CLI,
  MCP).
- Large improvements on the user interface in general, and more specifically on
  actions (forms, combox)
- Better session expiration UX when the user session expires
- French translations
- On the user interface, added a rich text editor for description fields (
  Markdown compatible)
- The action runner user interface had been re-built for better usage,
  navigation and features discovery with inline documentation

### Changes

- MCP instructions had been improved for agents
- Action system got semantics for UI auto-adaptation
- Actions are now all documented and better aligned together to ease
  integrations for API and CLI
- Action system improvement on all channels (better error reports, improved
  usage on CLI, API, MCP)
- Audit record of all actions by everybody (humans and actors)
 

### Security

- Security upgrades on backend, documentation and UI. Kept the project up to
  date with its dependencies.

### Other

- Largely improved automated tests for models and tags
- Cleanup repo and major code / architecture improvements
- Automated e2e infrastructure to be able to test releases