package io.medatarun.model.internal

import io.medatarun.model.domain.Model
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelNotFoundException
import io.medatarun.model.domain.ModelSummary
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
                    id = id,
                    name = model.name?.get(locale),
                    description = model.description?.get(locale),
                    error = null,
                    countTypes = model.types.size,
                    countEntities = model.entityDefs.size,
                    countRelationships = model.relationshipDefs.size
                )
            } catch (e: Exception) {
                ModelSummary(
                    id = id,
                    name = null,
                    description = null,
                    error = e.message,
                    countTypes = 0, countEntities = 0, countRelationships = 0
                )
            }
        }.sortedWith(
            Comparator.comparing(
                { it.name ?: it.id.value },
                Comparator.nullsLast(textComparator)
            )
        )
    }

    override fun findModelById(modelId: ModelId): Model {
        return storage.findModelByIdOptional(modelId) ?: throw ModelNotFoundException(modelId)
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