# Changelog

## [Unreleased 0.9.0]

### Added

**General**

- Changelog file (this file)

**Tags and models**

- Tags and tag groups event history

**MCP and API authentication**

- Support for OAuth2.0 in MCP (and APIs) with Dynamic Client Registration Protocol

**Administration tools**

- New admin menu
- Admin page for database drivers
- Admin page for configured datasources
- Admin page to list roles, manage roles, add permissions to roles
- Admin page to list actors, manage actors, add roles, enable or disable them

**Storage**

- PostgreSQL support. Default installations are still using SQLite, so they still run out-of-the box.
- Tools to generale initial SQL for fresh installations

### Breaking changes

- In actions (CLI, UI, API) `managed` tags vocabulary had been consistently
  replaced by `global` (`tag_managed_create` becomes `tag_global_create` for
  example). `free` tags vocabulary had been replaced by `local`. For example
  `tag_free_create` replaced by `tag_local_create`

Roles and permissions had been separated. From now a role is a named set of
permissions you can create yourself and affect to actors.

- In actions (CLI, UI, API), `actor_set_roles` had been removed.
- You should now create roles with `role_create` and add appropriate
  permissions with `role_add_permission` and `role_delete_permission`.
- Then you can use `actor_add_role` and `actor_delete_role` to affect roles to
  actors.

### Changes

**Features**

- The `free tag` and `managed tag` wording had been replaced by `local tag` and
  `global tag` everywhere. UI, API, CLI, MCP reflect changes in tag vocabulary.
  The idea is that the fact that a tag is free to create or managed are linked
  permissions, and it is not intrinsic of where the tag belongs. So the feature
  is the same, with the same organization possibilities, but the wording changes
  to
  reflect the reality.
- Tags history implemented with event sourcing
- A `System maintenance` actor is created automatically
  to identify data changes made by the system itself.
- Roles and permissions are now separate concepts. You can create, update,
  delete your own roles with permissions inside. Those roles can be affected to
  actors.

**Architecture**

- Improved traceability by grouping together `actorId` + `actionInstanceId` into
  `traceabilityRecord`. `traceabilityRecord` keeps `actorId` and extends the
  concept of `actionInstanceId` to a more flexible `origin`.
  `origin=action:<actionInstanceId>`
- Old roles had been renamed to permissions. A new role concept had been
  introduced as a set of permissions you can affect to actors.
- New feature in security permissions declaration to rename permissions

**Database**

`models-core` module:

- Column `model_events.action_id` renamed to `model_event.traceability_origin`
  with new storage format.
- Unecessary complications in how was built `model_search_item_snapshot.id`
  removed. Using now real `BINARY(16)` UUIDs.

`tags-core` module:

- Added `tag_event` table for tag event sourcing
- Renamed table `tag` to `tag_view_current_tag` and `tag_group` to
  `tag_view_current_tag_group` and their index  (denormalized tables). Now those
  tables can be rebuilt from event source `tag_event`.
- New event-sourced tables `tag_view_history_tag` and
  `tag_view_history_tag_group` allow finding old tag and group names and
  descriptions in history (denormalized tables)

`auth` module:

- `actors` table renamed to `auth_actor`
- A new set of tables had been introduced: `auth_actor_role`, `auth_role`,
  `auth_role_permission`, `auth_actor_role`

General:

- SQLite tables moved ids from `TEXT` to `BINARY(16)`.
- SQLite tables have a stronger TIMESTAMP usage where timestamps are needed,
  still as text but in TIMESTAMP format.

**Permissions**

- `tag_free_manage` renamed to `tag_local_manage`
- `tag_managed_manage` renamed to `tag_global_manage`

### Fixed

- Deleting local tags, global tags and groups of tags now removes included tags
  from everywhere on models and their elements.
- Copying models now creates a release event
- Tag scope deletion on model delete

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
