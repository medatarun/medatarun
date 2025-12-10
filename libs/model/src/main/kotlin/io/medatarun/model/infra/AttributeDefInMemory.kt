package io.medatarun.model.infra

import io.medatarun.model.domain.*

/**
 * Default implementation of AttributeDef
 */
data class AttributeDefInMemory(
    override val id: AttributeDefId,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
    override val type: ModelTypeId,
    override val optional: Boolean
) : AttributeDef {
    companion object {
        fun of(other: AttributeDef): AttributeDefInMemory {
            return AttributeDefInMemory(
                id = other.id,
                name = other.name,
                description = other.description,
                type = other.type,
                optional = other.optional
            )
        }
    }
}