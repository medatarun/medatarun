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
                commands = group.commands.map { command -> toCommandDto(command) }
            )
        }
    }

    private fun toCommandDto(command: ActionCmdDescriptor): CliActionCommandDto {
        val params = command.parameters.map { param ->
            CliActionParamDto(
                name = param.name,
                title = param.title,
                description = param.description,
                multiplatformType = param.multiplatformType,
                optional = param.optional,
                order = param.order
            )
        }
        return CliActionCommandDto(
            name = command.name,
            title = command.title,
            description = command.description,
            parameters = params
        )
    }
}

@Serializable
data class CliActionGroupDto(
    val name: String,
    val commands: List<CliActionCommandDto>
)

@Serializable
data class CliActionCommandDto(
    val name: String,
    val title: String?,
    val description: String?,
    val parameters: List<CliActionParamDto>
)

@Serializable
data class CliActionParamDto(
    val name: String,
    val title: String?,
    val description: String?,
    val multiplatformType: String,
    val optional: Boolean,
    val order: Int
)
