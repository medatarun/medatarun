package io.medatarun.model.model

@JvmInline
value class EntityDefId(val value: String)

interface EntityDef {

    val id: EntityDefId
    val name: LocalizedText?
    val description: LocalizedMarkdown?
    val attributes: List<AttributeDef>

    fun countAttributes(): Int
    fun getAttribute(id: AttributeDefId): AttributeDef
    fun hasAttributeDef(id: AttributeDefId): Boolean

    fun ensureAttributeDefExists(id: AttributeDefId) =
        // Ensures attribute definition exist, syntax sugar
        getAttribute(id)


}