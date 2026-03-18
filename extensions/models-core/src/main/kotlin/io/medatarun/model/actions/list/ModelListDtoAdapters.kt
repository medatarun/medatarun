package io.medatarun.model.actions.list

import io.medatarun.model.domain.ModelSummary
import kotlinx.serialization.Serializable

object ModelListDtoAdapters {
    fun toModelListDto(summaries: List<ModelSummary>): ModelListDto {
        val dtos = summaries.map {
            ModelListItemDto(
                id = it.id.value.toString(),
                key = it.key.value,
                name = it.name
            )
        }
        return ModelListDto(dtos)
    }
}


@Serializable
data class ModelListDto(
    val items: List<ModelListItemDto>
)

@Serializable
data class ModelListItemDto(
    val id: String,
    val key: String,
    val name: String?
)

