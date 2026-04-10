package io.medatarun.model.infra.inmemory

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.EntityId
import io.medatarun.model.domain.EntityPBKeyId
import io.medatarun.model.domain.PBKey
import io.medatarun.model.domain.PBKeyKind
import io.medatarun.model.domain.PBKeyParticipant
import io.medatarun.type.commons.id.Id

data class PBKeyInMemory(
    override val id: EntityPBKeyId,
    override val entityId: EntityId,
    override val kind: PBKeyKind,
    override val name: String?,
    override val description: String?,
    override val participants: List<PBKeyParticipantInMemory>,
) : PBKey {
    companion object {
        fun of(other: PBKey): PBKeyInMemory {
            return PBKeyInMemory(
                id = other.id,
                entityId = other.entityId,
                kind = other.kind,
                name = other.name,
                description = other.description,
                participants = other.participants
                    .map { PBKeyParticipantInMemory.of(it) }
            )
        }
        fun ofSingleAttribute(entityId: EntityId, attributeId: AttributeId): PBKeyInMemory {
            return PBKeyInMemory(
                id = Id.generate(::EntityPBKeyId),
                entityId = entityId,
                kind = PBKeyKind.PRIMARY,
                name = null,
                description = null,
                participants = listOf(PBKeyParticipantInMemory(attributeId,0))
            )
        }
    }
}