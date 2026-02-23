# tags-core

`tags-core` is the reusable core module that manages tags as semantic objects for Medatarun.

It is intentionally agnostic of concrete business objects (`model-core`, future `prompts-core`, etc.).
Other modules consume this core and define how tags can be applied to their own objects.

## Status

### Implemented today

- unified `Tag` identity and references (`TagId`, `TagRef`)
- explicit tag scope via `TagScopeRef` (global or local container scope)
- `TagGroup` support for managed vocabularies
- distinct business commands for `managed` vs `free` tags
- unified storage model for tags
- persisted tag scope in storage (`scope_type`, `scope_id`)
- key-based `TagRef` format includes scope information
- `TagFreeCreate` requires an explicit local `TagScopeRef`
- command-level compatibility checks between tag refs and business intent (`free` vs `managed`)
- transaction strategy across tag mutations and cross-module reactions (implemented via transaction manager and onBefore events that triggers changes in other modules, supposed to use the transaction manager too)

### Planned next

- assignment model (tagging arbitrary business objects with `TagId`)
- object-level rules defining which tag scopes are accepted


## Purpose

This module does not model "keywords" only.

A tag in Medatarun carries meaning:

- a stable key
- a name
- a description

Tags are used to express governance, intent, semantics, constraints and classification directly in the data model
(and later in other "intent objects" such as prompt templates / agent instructions).

## Core Principle

There is a single unified `Tag` concept with a single ID space (`TagId`), even if different governance modes exist.

This is important because:

- all taggable objects can reference tags with a single `TagId`
- API and references are simpler (`TagRef`)
- storage is simpler
- governance differences stay in business rules, not in duplicated technical models

## Current Business Modes of Tags

The current business model distinguishes two governance modes:

- `managed` tags
- `free` tags

The distinction is business-level, not storage-level.

### Managed tags

Managed tags are:

- global in scope (company's IT-wide / organization-wide in Medatarun usage)
- governed
- created/updated/deleted only by users with strong permissions
- organized in a `TagGroup`

Typical examples:

- GDPR / privacy classifications
- security classifications
- localization / internationalization labels
- application governance labels

Managed tags are intended to provide a shared, institutional vocabulary.

### Free tags

Free tags are:

- local to a business context (not global by default)
- less governed than managed tags
- used for local collaboration, exploration, conventions or temporary semantics
- still semantic objects (key + name + description), not throwaway labels

Important consequence:

- free tags cannot be treated as globally unique semantic concepts if they remain ungoverned
- two teams may legitimately use the same key with different meanings

This is why free tags must be scoped (see below).

## Scope (Implemented foundation, evolving rules)

The scope foundation is now implemented in `tags-core` via `TagScopeRef`.
The next work is to use it consistently in all consuming modules and enforce assignment policies.

Current design:

- every tag has a scope
- objects determine which tag scopes they accept

### Scope categories

Conceptually:

- `Global` scope
- `Local` scope tied to a container (`containerType`, `containerId`)

This is the current reading of the model:

- managed tags -> global scope
- free tags -> local scope

### Governance implications by scope

Current / target rules:

- `Global` tags:
  - strong governance permissions
  - `groupId` mandatory
  - intended for transversal organizational meaning
- `Local(...)` tags:
  - permissions relative to the local container
  - intended for contextual/local meaning
  - can coexist with other local tags with the same key in other scopes

`TagScopeRef` is a domain concept (a reference to a scope owned by another module/context), not an input format.
JSON representations are adapter concerns and are handled by converters.

## Tag Groups

`TagGroup` represents a controlled vocabulary group (taxonomy-like grouping).

Examples:

- GDPR category
- sensitivity level
- retention policy class

A managed tag belongs to a group.

Business role of groups:

- structure managed tags
- make governance easier
- support rules such as group-based filtering and future assignment constraints

## Tag References

Tags are referenced using a unified `TagRef`:

- by ID (`id:<tagId>`)
- by key with explicit scope
  - managed/global: `key:global/<groupKey>/<tagKey>`
  - local/free: `key:<scopeType>/<scopeId>/<tagKey>`

This supports:

- robust references by ID
- readable references for APIs, CLI and human-driven operations

Interpretation rule for key refs:

- if scope type is `global`, the middle segment is a `groupKey`
- otherwise, the middle segment is a local `scopeId`

Business compatibility is checked in command handling:

- free-tag commands reject managed references
- managed-tag commands reject free references

This keeps the API unified while preserving business intent.

## Business Rules (Current)

The module intentionally preserves distinct business commands for free vs managed tags
because their rules are not identical.

Examples of current differences:

- free tag key uniqueness is checked within the local tag scope
- managed tag key uniqueness is checked within a tag group
- managed tags require group-related governance
- `Tag.isManaged` derives from the tag scope (global vs local)

Even with a unified `Tag`, the command layer keeps these distinctions explicit.

## Decisions (Current)

- Database uniqueness for tags is intentionally **not** enforced by SQL constraints (beyond primary keys and existing group FK).
- Tag uniqueness rules are enforced in business code (`TagCmdsImpl`), because free and managed tags do not share the same uniqueness logic.
- Current uniqueness rules are:
  - free tags: unique within a local scope
  - managed tags: unique within a tag group
- This is an explicit design decision, not a missing implementation.
- The consistency between `Tag.scope` and `Tag.groupId` is also intentionally enforced in business code (commands and domain rules), not by SQL constraints.
- Current business rule for that consistency is:
  - global / managed tag -> `groupId` is required
  - local / free tag -> `groupId` is absent

## Why tags-core is agnostic

`tags-core` is not "tags for models only".

It is a shared semantic tagging core for multiple Medatarun domains.

Today:

- `model-core` uses tags for models, entities, relationships, attributes

Later:

- `prompts-core` (or equivalent module) can use the same tagging core for prompts, templates, agent instructions, etc.

This matches Medatarun's broader goal:

- store intentions as data
- make them governable
- make them accessible consistently across UI / CLI / API / agents

## Taggable Objects and Accepted Scopes (Target Direction)

A key business rule for future modules:

- a taggable object should define which tag scopes it accepts

The scope infrastructure is already available in `tags-core`; what remains is module-specific policy enforcement.

Examples:

- `Model` / `Entity` / `Relation` / `Attribute` may accept:
  - global managed tags
  - local tags scoped to their owning `Model`
- future prompt/agent-related objects may accept:
  - global managed tags
  - local tags scoped to a prompt library / workspace / template collection

This avoids semantic leakage between unrelated contexts while preserving transversal governance.

## Example rule for model-core (planned business rule)

For `Model` objects (`Model` itself and the `Entity`(s), `Relationship`(s), `Attribute`(s) it holds):

- allowed tags:
  - managed tags (global)
  - free tags that belong to the enclosing `Model`
- forbidden:
  - free tags from another `Model`

This gives local flexibility without polluting the organization-wide semantic space.

## Future: assignments (tagging objects)

This module currently manages tag definitions and tag groups.

A future layer (in consuming modules or a shared assignment core) will manage assignments:

- linking a taggable object to a `TagId`

Important business questions to define when implementing assignments:

- can an object have multiple tags from the same managed group?
- should some groups be single-valued (e.g. one sensitivity level)?
- what happens when a tag is deleted and is still assigned?

Recommended direction:

- assignments should reference `TagId` (not keys)
- assignment rules should be expressed at business level per object type / group policy

## Events and cross-module reactions (planned architecture)

`tags-core` should expose events around tag lifecycle changes so consumer modules can react or block.

This is the intended integration model for modules like `model-core` and future `prompts-core`.

### Why events are needed

Other modules may need to:

- prevent deletion if a tag is still used
- cleanup assignments when tags/groups are deleted
- update indexes / search projections
- enforce local invariants related to accepted scopes

### Important design guideline

The event model should distinguish:

- `before` events (validation / veto / cleanup before final operation / blocking). Typically when an object needs to be deleted, it asks other modules via event to delete traces of usages before real deletion (or to block deletion)
- `after` events (reaction / projection / sync) - no use case for now

This avoids mixing business veto logic with post-change side effects.

## Practical governance strategy (recommended)

For Medatarun's "central of intentions" direction:

- managed tags = global governed vocabulary (company IT-wide / governance / security / data office)
- local tags = contextual vocabulary for a specific container scope
- promotion path can exist later:
  - local concept becomes stable and shared
  - then gets formalized as a managed tag

This allows Medatarun to support both:

- controlled enterprise semantics
- local iterative sense-making by teams

without conflating the two.

## Summary

`tags-core` is a semantic governance primitive, not just a label utility.

Its role is to provide:

- a unified tag identity model (`TagId`, `TagRef`)
- business distinctions for governance (`managed` vs local/free usage)
- a foundation for explicit scope-based semantics
- an agnostic core reusable by multiple Medatarun domains
- integration points (events) so consumer modules can enforce their own rules
