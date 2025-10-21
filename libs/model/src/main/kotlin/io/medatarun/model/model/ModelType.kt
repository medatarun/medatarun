package io.medatarun.model.model

@JvmInline
value class ModelTypeId(val value : String )

interface  ModelType {
    val id: EntityDefId
    val name: LocalizedText
    val description: LocalizedText
}