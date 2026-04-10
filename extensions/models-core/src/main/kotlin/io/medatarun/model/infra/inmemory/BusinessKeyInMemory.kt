package io.medatarun.model.infra.inmemory

import io.medatarun.model.domain.BusinessKey
import io.medatarun.model.domain.BusinessKeyId
import io.medatarun.model.domain.BusinessKeyKey
import io.medatarun.model.domain.EntityId

data class BusinessKeyInMemory(
    override val id: BusinessKeyId,
    override val key: BusinessKeyKey,
    override val entityId: EntityId,
    override val name: String?,
    override val description: String?,
    override val participants: List<PBKeyParticipantInMemory>,
) : BusinessKey {
    companion object {
        fun of(other: BusinessKey): BusinessKeyInMemory {
            return BusinessKeyInMemory(
                id = other.id,
                entityId = other.entityId,
                key = other.key,
                name = other.name,
                description = other.description,
                participants = other.participants
                    .map { PBKeyParticipantInMemory.of(it) }
            )
        }
    }
}