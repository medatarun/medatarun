package io.medatarun.model.model

@JvmInline value class ModelId(val value: String)

@JvmInline value class ModelLocation(val value: String)
@JvmInline value class ModelVersion(val value: String)


interface Model {
    val id: ModelId
    val name: LocalizedText?
    val description: LocalizedMarkdown?
    val version: ModelVersion
    val entityDefs: List<EntityDef>
    fun findEntityDefOptional(entityDefId: EntityDefId): EntityDef? = entityDefs.firstOrNull { it.id == entityDefId }
    fun findEntityDef(entityDefId: EntityDefId): EntityDef = findEntityDefOptional(entityDefId) ?: throw EntityDefNotInModelException(id, entityDefId)
}


