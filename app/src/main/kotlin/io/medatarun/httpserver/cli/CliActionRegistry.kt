package io.medatarun.httpserver.cli

import io.medatarun.actions.runtime.ActionCmdDescriptor
import io.medatarun.actions.runtime.ActionRegistry
import kotlinx.serialization.Serializable

class CliActionRegistry(private val actionRegistry: ActionRegistry) {

    fun actionRegistryDto(): List<CliActionGroupDto> {
        val groups = actionRegistry.findAllGroupDescriptors()
        return groups.map { group ->
            CliActionGroupDto(
                name = group.name,
                actions = group.commands.map { command -> toCommandDto(command) }
            )
        }
    }

    private fun toCommandDto(command: ActionCmdDescriptor): CliActionDto {
        val params = command.parameters.map { param ->
            CliActionParamDto(
                key = param.name,
                title = param.title,
                description = param.description,
                multiplatformType = param.multiplatformType,
                jsonType = param.jsonType.code,
                optional = param.optional,
                order = param.order
            )
        }
        return CliActionDto(
            key = command.name,
            title = command.title,
            description = command.description,
            parameters = params
        )
    }
}

@Serializable
data class CliActionGroupDto(
    val name: String,
    val actions: List<CliActionDto>
)

@Serializable
data class CliActionDto(
    val key: String,
    val title: String?,
    val description: String?,
    val parameters: List<CliActionParamDto>
)

@Serializable
data class CliActionParamDto(
    val key: String,
    val title: String?,
    val description: String?,
    val multiplatformType: String,
    val jsonType: String,
    val optional: Boolean,
    val order: Int
)
