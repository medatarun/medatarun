# Changelog

## [0.10.0]

### Added

**Primary keys optional and business keys**

- Entities can have composite primary keys.
- Entities primary keys are no longer required, you can have entities without
  primary keys to better reflect real models.
- Entities can have business keys. A business key is composed of one or more
  entity attributes, have name and description as well as its own identiier key.
- Model JSON schema version 3.0.0 released for import/export

**Fine-grained permissions**

- Fine-grained permissions introduced on Models
- Screens have been upgraded with those new permissions. Mecanisms have been
  added to remove automatically inline edits and action buttons from the
  screens, where the user doesn't have permissions. It avoids usages of actions
  that would end up with a forbidden response anyway.

**Implied permissions**

- Because when you have a lot of permissions, you can have issues to fill a role
  with a consistent set of permissions. Added a concept of implied permissions.
- When you add a permission to a role, for example, `model_write`, automatically
  `model_read` is added, because otherwise it has no sense.
- Implied permissions are automatically removed when no other permission
  requires it.
- Role management screen now displays those permissions so that admin can have
  the full picture of what is authorized or not for the role.

**Onboarding**

- Better onboarding messages on the user interface to provide assistance for new
  installations.

### Changes

- Enforce delete constraints on entities so that you can't mistakely delete an
  entity if a relationship uses it.
- JDBC imports now create composite primary keys and don't fail with no primary
  key exists
- Frictionless-data now supports composite primary keys detection
- Fixed issues with session expiration
- Reorganized order and which action buttons are displayed on screens to make it
  more intuitive.
- Action forms are now prefilled with values when available. For example, for
  editing a key or a name, previous values are set on input fields.
- Upgraded dependencies in user interface and documentation subprojects.
- Improved user interface look and feel.

### Breaking changes

- action `entity_create` doesn't support `identifierAttribute` properties
  anymore. You must call `entity_attribute_create` after entity creation to add
  the previous identifier attribute separaterly and call primary key actions to
  set it as a primary key participant.
- `inspect_models_json` action removed `identifierAttribute`, added
  `businessKeys` in the model, `primaryKey` in each Entity.
- `inspect_actions` do not return `uiLocations` anymore

### Internal changes

- [ui] Action system reorganised and simplified for better control of the query
  caches and post-action navigation.
- [ui] Uniformized backend calls to use an instance of ActionPerformer
  everywhere
- [ui] Action registry is now statically compiled in UI for performance but also
  to provide to screens static typings for actions
- [ui] Replaced gradle-node-plugin that seems dead and has vulnerabilities with
  `org.siouan.frontend-jdk21` Frontend Gradle Plugin latest version

## [0.9.0]

### Added

- Tags and tag groups now have a full history of changes. In model history
  views, you can see old tag names even if they were removed or renamed.
- AI agents and API tools can now register themselves with OAuth 2.0 Dynamic
  Client Registration Protocol. For example, tools like OpenAI ChatGPT Codex
  show an "Authenticate" button and redirect users to a webpage
  where they can authorize the connection in their browser.
- Better RBAC: before this release, permissions were directly
  attached to actors. From now on, you can create roles and add permissions to
  roles. Actors can have multiple roles.
- The user interface now has an administration menu where you can:
    - list your installed database drivers and datasources
    - manage users from our internal identity storage
    - manage actors from our internal identity storage or external identity
      providers like Google, Azure AD, or Auth0
    - create roles and populate them with permissions
    - attach roles to actors
    - disable or re-enable actors
- PostgreSQL support has been added. PostgreSQL is optional; fresh
  installations still use SQLite out of the box.

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
  to reflect the reality.
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
- Unit tests fully use the database, can run all tests on PostgreSQL too (via
  TestContainers)
- E2E testing tools to replace manual testing (Python scripting with pytest)
- Largely improved database migration processes and tooling cross-modules

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
- SQLite tables have a stronger timestamp usage where timestamps are needed,
  now using `INTEGER` instead of `TEXT`.

**Permissions**

- `tag_free_manage` renamed to `tag_local_manage`
- `tag_managed_manage` renamed to `tag_global_manage`

**Dependencies**

- Typescript upgraded to 6.0
- Eslint upgraded to 10.0
- Kotlin upgrade to 2.3
- Most dependencies patches to latest for security

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
