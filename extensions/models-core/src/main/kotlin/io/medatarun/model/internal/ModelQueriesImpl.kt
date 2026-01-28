package io.medatarun.model.internal

import io.medatarun.model.domain.*
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.model.ports.needs.ModelStorages
import java.text.Collator
import java.text.Normalizer
import java.util.*

class ModelQueriesImpl(private val storage: ModelStorages) : ModelQueries {

    override fun findAllModelIds(): List<ModelId> {
        return storage.findAllModelIds()
    }

    override fun findAllModelSummaries(locale: Locale): List<ModelSummary> {
        val textComparator = TextComparator(locale)
        return storage.findAllModelIds().map { id ->
            try {
                val model = storage.findModelById(id)
                ModelSummary(
                    id = model.id,
                    key = model.key,
                    name = model.name?.get(locale),
                    description = model.description?.get(locale),
                    error = null,
                    countTypes = model.types.size,
                    countEntities = model.entities.size,
                    countRelationships = model.relationships.size
                )
            } catch (e: Exception) {
                ModelSummary(
                    id = id,
                    key = ModelKey.generateRandom(),
                    name = null,
                    description = null,
                    error = e.message,
                    countTypes = 0, countEntities = 0, countRelationships = 0
                )
            }
        }.sortedWith(
            Comparator.comparing(
                { it.name ?: it.key.value },
                Comparator.nullsLast(textComparator)
            )
        )
    }

    override fun findEntity(
        modelRef: ModelRef,
        entityRef: EntityRef
    ): Entity {
        val model = findModel(modelRef)
        return model.findEntityOptional(entityRef) ?: throw EntityNotFoundException(modelRef, entityRef)
    }

    override fun findEntityAttributeOptional(
        modelRef: ModelRef,
        entityRef: EntityRef,
        attributeRef: EntityAttributeRef
    ): Attribute? {
        val model = findModel(modelRef)
        return model.findEntityAttributeOptional(entityRef, attributeRef)
    }

    override fun findEntityAttribute(
        modelRef: ModelRef,
        entityRef: EntityRef,
        attributeRef: EntityAttributeRef
    ): Attribute {
        return findEntityAttributeOptional(modelRef, entityRef, attributeRef)
            ?: throw EntityAttributeNotFoundException(modelRef, entityRef, attributeRef)
    }

    override fun findType(
        modelRef: ModelRef,
        typeRef: TypeRef
    ): ModelType {
        return findModel(modelRef).findTypeOptional(typeRef) ?: throw TypeNotFoundException(modelRef, typeRef)
    }

    override fun findModelByKey(modelKey: ModelKey): Model {
        return storage.findModelByKeyOptional(modelKey) ?: throw ModelNotFoundByKeyException(modelKey)
    }

    override fun findModelById(modelId: ModelId): Model {
        return storage.findModelByIdOptional(modelId) ?: throw ModelNotFoundByIdException(modelId)
    }

    override fun findModel(modelRef: ModelRef): Model {
        return when (modelRef) {
            is ModelRef.ById -> findModelById(modelRef.id)
            is ModelRef.ByKey -> findModelByKey(modelRef.key)
        }
    }

    override fun findModelOptional(modelRef: ModelRef): Model? {
        return when (modelRef) {
            is ModelRef.ById -> storage.findModelByIdOptional(modelRef.id)
            is ModelRef.ByKey -> storage.findModelByKeyOptional(modelRef.key)
        }
    }

    private class TextComparator(val locale: Locale) : Comparator<String> {
        private val collator = Collator.getInstance(locale)
        private val comparator = Comparator.nullsLast(
            Comparator<String> { x, y ->
                val xnfd = Normalizer.normalize(x, Normalizer.Form.NFD)
                val ynfd = Normalizer.normalize(y, Normalizer.Form.NFD)
                norm(xnfd, ynfd)
            }
        )

        fun norm(x: String, y: String): Int {
            return collator.compare(x, y)
        }

        override fun compare(o1: String?, o2: String?): Int {
            return comparator.compare(o1, o2)

        }
    }

}