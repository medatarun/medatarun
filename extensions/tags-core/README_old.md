# tags-core

`tags-core` is the reusable core module that manages tags as semantic objects for Medatarun.

It is intentionally agnostic of concrete business objects (`model-core`, future `prompts-core`, etc.).
Other modules consume this core and define how tags can be applied to their own objects.

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

This rule is now important in practice because `model-core` already stores `TagId` on objects.
Without the attach-time scope check, a local tag from another model can be attached by mistake.

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
