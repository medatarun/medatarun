package io.medatarun.ext.db.actions

import io.medatarun.actions.actions.SecurityRuleNames
import io.medatarun.actions.ports.needs.ActionDoc

sealed interface DatabasesAction {

    @ActionDoc(
        key="driver_list",
        title = "Database drivers",
        description = "Lists available database drivers",
        uiLocation = "global",
        securityRule = SecurityRuleNames.ADMIN
    )
    class DatabaseDrivers : DatabasesAction

    @ActionDoc(
        key="datasource_list",
        title = "Database sources",
        description = "Lists available datasources",
        uiLocation = "global",
        securityRule = SecurityRuleNames.ADMIN
    )
    class Datasources : DatabasesAction
}