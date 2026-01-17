package io.metadatarun.ext.config.actions

class ConfigAgentInstructions {
    fun process(): String {
        return """
This server provides capabilities to create and enrich domain models. Models contain entities and attributes.
Use "model/inspectJson" command to know available models, types and entities.
Do not read or write .medatarun directly; interact with models only through the MCP server.
Always set a clear primary key/identifier for every entity.
Avoid primitive obsession: create and use rich domain types (Email, Phone, URL, etc.) instead of generic primitives; define any missing rich types before using them.
            """.trimIndent()
    }
}