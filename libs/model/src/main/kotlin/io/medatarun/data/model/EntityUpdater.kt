package io.medatarun.data.model

import io.medatarun.model.model.AttributeDefId

interface EntityUpdater {
    val id: EntityId
    fun get(attributeId: AttributeDefId): Instruction
    fun list(): List<Instruction>

    sealed interface Instruction {
        val attributeId: AttributeDefId
        val present: Boolean

        data class InstructionNone(
            override val attributeId: AttributeDefId,
            ) : Instruction {
            override val present: Boolean = false
        }

        data class InstructionUpdate(
            override val attributeId: AttributeDefId,
            val value: Any
        ) : Instruction {
            override val present: Boolean = true
        }
    }
}