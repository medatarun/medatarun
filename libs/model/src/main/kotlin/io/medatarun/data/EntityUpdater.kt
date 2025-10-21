package io.medatarun.data

import io.medatarun.model.model.ModelAttributeId

interface EntityUpdater {
    val id: EntityInstanceId
    fun get(attributeId: ModelAttributeId): Instruction
    fun list(): List<Instruction>

    sealed interface Instruction {
        val attributeId: ModelAttributeId
        val present: Boolean

        data class InstructionNone(
            override val attributeId: ModelAttributeId,
            ) : Instruction {
            override val present: Boolean = false
        }

        data class InstructionUpdate(
            override val attributeId: ModelAttributeId,
            val value: Any
        ) : Instruction {
            override val present: Boolean = true
        }
    }
}