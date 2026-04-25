package io.medatarun.model.actions.list

import io.medatarun.model.domain.ModelSummary
import kotlinx.serialization.Serializable

object ModelListDtoAdapters {
    fun toModelListDto(summaries: List<ModelSummary>): ModelListDto {
        val dtos = summaries.map {
            ModelListItemDto(
                id = it.id.value.toString(),
                key = it.key.value,
                name = it.name,
                description = it.description,
                authority = it.authority.code
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
    val name: String?,
    val description: String?,
    val authority: String
)

