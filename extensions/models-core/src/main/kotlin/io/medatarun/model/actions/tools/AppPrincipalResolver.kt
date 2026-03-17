package io.medatarun.model.actions.tools

import io.medatarun.security.AppPrincipalId

class AppPrincipalResolver {
    val resolved: MutableMap<AppPrincipalId,  String> = mutableMapOf()
    fun displayName(id: AppPrincipalId): String {
        return id.value
    }

}