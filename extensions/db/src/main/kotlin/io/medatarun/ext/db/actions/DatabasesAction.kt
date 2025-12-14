package io.medatarun.ext.db.actions

import io.medatarun.actions.ports.needs.ActionDoc

sealed interface DatabasesAction {

    @ActionDoc(
        title = "Database drivers",
        description = "Lists available database drivers",
        uiLocation = "global"
    )
    class DatabaseDrivers() : DatabasesAction

    @ActionDoc(
        title = "Database sources",
        description = "Lists available datasources",
        uiLocation = "global"
    )
    class Datasources() : DatabasesAction
}