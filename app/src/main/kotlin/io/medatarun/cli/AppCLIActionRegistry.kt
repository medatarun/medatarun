package io.medatarun.cli

import io.metadatarun.ext.config.actions.dto.ActionDescriptorDto


class AppCLIActionRegistry(
    actionDescriptorDtoList: List<ActionDescriptorDto>
) {
    private val actionGroupDtoList: List<AppCLIActionGroup> = actionDescriptorDtoList
        .groupBy { it.groupKey }
        .map { groupEntry ->
            AppCLIActionGroup(
                name = groupEntry.key,
                actions = groupEntry.value.map { action ->
                    AppCLIAction(
                        actionGroupKey = action.groupKey,
                        actionKey = action.actionKey,
                        title = action.title,
                        description = action.description,
                        parameters = action.parameters.map { parameter ->
                            AppCLIActionParam(
                                key = parameter.name,
                                title = parameter.title,
                                description = parameter.description,
                                multiplatformType = parameter.type,
                                jsonType = parameter.jsonType,
                                optional = parameter.optional,
                                order = parameter.order
                            )
                        }
                    )
                }
            )
        }

    fun findActionGroup(resourceId: String): AppCLIActionGroup? {
        return actionGroupDtoList.find { it.name == resourceId }
    }

    fun findActionGroupNamesSorted(): List<String> {
        return actionGroupDtoList.sortedBy { it.name.lowercase() }.map { it.name }
    }
}

data class AppCLIActionGroup(
    val name: String,
    val actions: List<AppCLIAction>
)

data class AppCLIAction(
    val actionGroupKey: String,
    val actionKey: String,
    val title: String?,
    val description: String?,
    val parameters: List<AppCLIActionParam>
)

data class AppCLIActionParam(
    val key: String,
    val title: String?,
    val description: String?,
    val multiplatformType: String,
    val jsonType: String,
    val optional: Boolean,
    val order: Int
)
