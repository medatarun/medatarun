package io.medatarun.ext.db.actions

import io.medatarun.actions.ports.needs.ActionDoc

sealed interface DatabasesAction {

    @ActionDoc(
        key="driver_list",
        title = "Database drivers",
        description = "Lists available database drivers",
        uiLocation = "global"
    )
    class DatabaseDrivers() : DatabasesAction

    @ActionDoc(
        key="datasource_list",
        title = "Database sources",
        description = "Lists available datasources",
        uiLocation = "global"
    )
    class Datasources() : DatabasesAction
}