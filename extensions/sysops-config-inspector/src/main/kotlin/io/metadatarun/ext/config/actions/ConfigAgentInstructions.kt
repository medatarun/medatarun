package io.metadatarun.ext.config.actions

class ConfigAgentInstructions {
    fun process(): String {
        return """
This server provides capabilities to create and enrich conceptual domain models. 
Models contain entities and their attributes, relationships and their attributes, types.

Use "model/model_list" command to list available models.
Use "model/model_export" command to get the full content of a model.

When you need to address an object (modelRef, entityRef, etc.) the value must be:
- either "id:xxx" where xxx is the id of the object
- or "key:xxx" where xxx is the key of the object.

Interact with models only through the MCP server.
Always set a clear primary key/identifier for every entity.
Avoid primitive obsession: create and use rich domain types (Email, Phone, URL, etc.) instead of generic primitives; define any missing rich types before using them.
            """.trimIndent()
    }
}