package io.medatarun.ext.db.actions


import io.medatarun.actions.actions.ActionUILocation
import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.security.SecurityRuleNames

sealed interface DatabasesAction {

    @ActionDoc(
        key="driver_list",
        title = "Database drivers",
        description = "Lists available database drivers",
        uiLocations = [ActionUILocation.global],
        securityRule = SecurityRuleNames.ADMIN
    )
    class DatabaseDrivers : DatabasesAction

    @ActionDoc(
        key="datasource_list",
        title = "Database sources",
        description = "Lists available datasources",
        uiLocations = [ActionUILocation.global],
        securityRule = SecurityRuleNames.ADMIN
    )
    class Datasources : DatabasesAction
}