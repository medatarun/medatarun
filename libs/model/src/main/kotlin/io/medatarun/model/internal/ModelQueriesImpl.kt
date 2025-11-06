package io.medatarun.model.internal

import io.medatarun.model.model.*
import io.medatarun.model.ports.ModelStorages
import java.text.Collator
import java.text.Normalizer
import java.util.*

class ModelQueriesImpl(private val storage: ModelStorages, private val locale: Locale) : ModelQueries {

    private val textComparator = TextComparator(locale)

    override fun findAllModelIds(): List<ModelId> {
        return storage.findAllModelIds()
    }

    override fun findAllModelSummaries(): List<ModelSummary> {
        return storage.findAllModelIds().map { id ->
            try {
                val model = storage.findModelById(id)
                ModelSummary(
                    id = id,
                    name = model.name?.get(locale.language),
                    description = model.description?.get(locale.language),
                    error = null
                )
            } catch (e: Exception) {
                ModelSummary(
                    id = id,
                    name = null,
                    description = null,
                    error = e.message
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