package io.medatarun.ext.db.actions


import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionDocSemantics
import io.medatarun.actions.ports.needs.ActionDocSemanticsMode
import io.medatarun.security.SecurityRuleNames

sealed interface DatabasesAction {

    @ActionDoc(
        key="driver_list",
        title = "Database drivers",
        description = "Lists available database drivers",
        securityRule = SecurityRuleNames.ADMIN,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
    )
    class DatabaseDrivers : DatabasesAction

    @ActionDoc(
        key="datasource_list",
        title = "Database sources",
        description = "Lists available datasources",
        securityRule = SecurityRuleNames.ADMIN,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
    )
    class Datasources : DatabasesAction
}