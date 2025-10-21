package io.medatarun.model.infra

import io.medatarun.model.model.*


data class ModelInMemory(
    override val id: ModelId,
    override val name: LocalizedText?,
    override val description: LocalizedMarkdown?,
    override val version: ModelVersion,
    override val entityDefs: List<EntityDefInMemory>
) : Model {

}

data class EntityDefInMemory(
    override val id: EntityDefId,
    override val name: LocalizedText?,
    override val attributes: List<AttributeDefInMemory>,
    override val description: LocalizedMarkdown?,
) : EntityDef {

    private val map = attributes.associateBy { it.id }

    override fun countAttributes(): Int {
        return attributes.size
    }

    override fun getAttribute(id: AttributeDefId): AttributeDef {
        return map[id] ?: throw ModelEntityAttributeNotFoundException(this.id, id)
    }

    override fun hasAttributeDef(id: AttributeDefId): Boolean = map.containsKey(id)


    companion object {
        fun of(other: EntityDef): EntityDefInMemory {
            return EntityDefInMemory(
                id = other.id,
                name = other.name,
                description = other.description,
                attributes = other.attributes.map(AttributeDefInMemory::of),
            )
        }
    }
}

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
