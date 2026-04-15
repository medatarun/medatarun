package io.medatarun.model.infra.inmemory

import io.medatarun.model.domain.AttributeId
import io.medatarun.model.domain.PBKeyParticipant

data class PBKeyParticipantInMemory(
    override val attributeId: AttributeId,
    override val position: Int
) : PBKeyParticipant {
    companion object {
        fun of(other: PBKeyParticipant): PBKeyParticipantInMemory {
            return PBKeyParticipantInMemory(
                attributeId = other.attributeId,
                position = other.position
            )
        }
    }
}