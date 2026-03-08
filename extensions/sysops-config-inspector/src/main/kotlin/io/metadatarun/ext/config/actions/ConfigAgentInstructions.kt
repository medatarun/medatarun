package io.metadatarun.ext.config.actions

import org.intellij.lang.annotations.Language

class ConfigAgentInstructions {
    fun process(): String {

        return instructions
    }

    companion object {
        @Language("Markdown")
        val instructions = """
This server is used to build and maintain conceptual domain models.

A model can contain:
- types,
- entities and their attributes,
- relationships and their attributes,
- tags.

## Core rules

- Interact with models only through MCP actions from this server.
- Always define a clear identifier attribute for each entity.
- Prefer domain-specific types over generic primitives when possible.

## Reference format (mandatory)

When an action expects a reference (`modelRef`, `entityRef`, `attributeRef`, etc.), always pass:
- `key:...` (preferred when stable and readable), or
- `id:...`.
Never pass raw keys or ids without the prefix except when a key is required for creation or update (`modelKey`, `entityKey`, `key`, etc. parameters).

## Tag model semantics (must be respected)

- One unified `Tag` model.
- `managed` tag:
    - global scope
    - shared controlled vocabularies
    - belongs to one `TagGroup`
    - uniqueness by `(group, key)`
    - Do not add managed tags without explicit user consent.
- `free` tag:
    - local scope (`scopeType`, `scopeId`)
    - local/contextual needs
    - no group
    - uniqueness by `(local scope, key)`

## Recommended workflow

1. Use `model/model_list` to find the target model.
2. Use `model/model_export` to read the full current state before making changes.
3. Apply changes with targeted actions (`*_create`, `*_update_*`, `*_delete`, `*_add_tag`, etc.).
4. Re-run `model/model_export` after changes to verify consistency.
5. Before finalizing, perform one final full export review.

## Search usage

- Use `model/search` only for tags or text content search.
- Do not use `model/search` as a substitute for full model review.
- Use `model/model_export` for authoritative validation.

## Modeling quality expectations

- Keep names and descriptions clear and unambiguous.
- For important fields, document meaning, allowed values, and rules.
- Keep consistency across model, type, entity, attribute, and relationship descriptions.
- If tags are used, ensure each tag has a clear purpose and usage description.
- Write for business users who do not know data modeling terms.
- Every description must mention concrete business objects (customer, order, payment, address, etc.), not technical abstractions.

## Completion criteria

A change is complete only if:

- Requested changes are present in `model/model_export`.
- Text descriptions are clear for target users, understandable and actionable.
- updates are consistent across affected objects.

""".trimIndent()
    }
}