package io.medatarun.model.infra.inmemory

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntityPrimaryKey
import io.medatarun.model.domain.EntityPrimaryKeyId
import io.medatarun.type.commons.id.Id

data class EntityPrimaryKeyInMemory(
    override val id: EntityPrimaryKeyId,
    override val entityId: EntityId,
    override val participants: List<PBKeyParticipantInMemory>,
) : EntityPrimaryKey {
    companion object {
        fun of(other: EntityPrimaryKey): EntityPrimaryKeyInMemory {
            return EntityPrimaryKeyInMemory(
                id = other.id,
                entityId = other.entityId,
                participants = other.participants
                    .map { PBKeyParticipantInMemory.of(it) }
            )
        }

        fun ofSingleAttribute(entityId: EntityId, attributeId: AttributeId): EntityPrimaryKeyInMemory {
            return EntityPrimaryKeyInMemory(
                Id.generate(::EntityPrimaryKeyId),
                entityId,
                listOf(PBKeyParticipantInMemory(attributeId, 0))
            )
        }
    }
}