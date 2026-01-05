package io.medatarun.cli

import io.medatarun.httpserver.cli.CliActionGroupDto


class AppCLIActionRegistry(
    private val actionGroupDtoList: List<CliActionGroupDto>
) {
    fun findActionGroup(resourceId: String): CliActionGroupDto? {
        return actionGroupDtoList.find { it.name == resourceId }
    }
    fun findActionGroupNamesSorted(): List<String> {
        return actionGroupDtoList.sortedBy { it.name.lowercase() }.map { it.name }
    }
}