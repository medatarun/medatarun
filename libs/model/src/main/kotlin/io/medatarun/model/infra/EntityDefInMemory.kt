package io.medatarun.model.infra

import io.medatarun.model.model.AttributeDef
import io.medatarun.model.model.AttributeDefId
import io.medatarun.model.model.EntityDef
import io.medatarun.model.model.EntityDefId
import io.medatarun.model.model.LocalizedMarkdown
import io.medatarun.model.model.LocalizedText
import io.medatarun.model.model.AttributeDefNotFoundException

/**
 * Default implementation of EntityDef
 */
data class EntityDefInMemory(
    override val id: EntityDefId,
    override val name: LocalizedText?,
    override val attributes: List<AttributeDefInMemory>,
    override val description: LocalizedMarkdown?,
    override val identifierAttributeDefId: AttributeDefId
) : EntityDef {

    private val map = attributes.associateBy { it.id }

    override fun countAttributeDefs(): Int {
        return attributes.size
    }

    override fun getAttributeDef(id: AttributeDefId): AttributeDef {
        return map[id] ?: throw AttributeDefNotFoundException(this.id, id)
    }

    override fun hasAttributeDef(id: AttributeDefId): Boolean = map.containsKey(id)


    companion object {
        fun of(other: EntityDef): EntityDefInMemory {
            return EntityDefInMemory(
                id = other.id,
                name = other.name,
                description = other.description,
                attributes = other.attributes.map(AttributeDefInMemory::of),
                identifierAttributeDefId = other.identifierAttributeDefId
            )
        }
    }
}