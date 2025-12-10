package io.medatarun.data.ports.exposed

import io.medatarun.data.domain.EntityId
import io.medatarun.model.domain.AttributeKey

interface EntityUpdater {
    val id: EntityId
    fun get(attributeId: AttributeKey): Instruction
    fun list(): List<Instruction>

    sealed interface Instruction {
        val attributeId: AttributeKey
        val present: Boolean

        data class InstructionNone(
            override val attributeId: AttributeKey,
            ) : Instruction {
            override val present: Boolean = false
        }

        data class InstructionUpdate(
            override val attributeId: AttributeKey,
            val value: Any
        ) : Instruction {
            override val present: Boolean = true
        }
    }
}