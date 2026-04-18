package io.metadatarun.ext.config.actions


import io.medatarun.actions.ports.needs.ActionDoc
import io.medatarun.actions.ports.needs.ActionDocSemantics
import io.medatarun.actions.ports.needs.ActionDocSemanticsMode
import io.medatarun.actions.ports.needs.ActionParamDoc
import io.medatarun.security.SecurityRuleNames

sealed interface ConfigAction {
    @ActionDoc(
        key="ai_agents_instructions",
        title = "AI Agents Instructions",
        description = "Each AI Agent should read that first. Returns a usage guide for AI Agents. Use it for your AGENTS.md files if your agent doesn't support instructions in MCP.",
        securityRule = SecurityRuleNames.PUBLIC,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
    )
    class AIAgentsInstructions : ConfigAction

    @ActionDoc(
        key="inspect_config_text",
        title = "Inspect config as text file",
        description = "Returns a human-readable list of the configuration, including extension contributions and contribution points, what provides what to whom.",
        securityRule = SecurityRuleNames.ADMIN,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
    )
    class Inspect : ConfigAction

    @ActionDoc(
        key="inspect_config",
        title = "Inspect config",
        description = "Returns a Json representation of the configuration, including extension contributions and contribution points, what provides what to whom.",
        securityRule = SecurityRuleNames.ADMIN,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
    )
    class InspectJson : ConfigAction

    @ActionDoc(
        key="inspect_actions",
        title = "Inspect actions",
        description = "Returns all known actions with their parameter descriptions.",
        securityRule = SecurityRuleNames.PUBLIC,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
    )
    class InspectActions : ConfigAction


    @ActionDoc(
        key="inspect_actions_all",
        title = "Inspect all actions",
        description = "Returns all known from the system.",
        securityRule = SecurityRuleNames.PUBLIC,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
    )
    class InspectActionsAll : ConfigAction

    @ActionDoc(
        key="inspect_security_rules",
        title = "Inspect security rules",
        description = "Returns all known security rules registered in application with their descriptions.",
        securityRule = SecurityRuleNames.PUBLIC,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
    )
    class InspectSecurityRules : ConfigAction

    @ActionDoc(
        key="inspect_permissions",
        title = "Inspect permissions",
        description = "Returns all known permissions registered in application with their descriptions.",
        securityRule = SecurityRuleNames.PUBLIC,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
    )
    class InspectPermissions : ConfigAction

    @ActionDoc(
        key="inspect_type_system",
        title = "Inspect type system",
        description = "Returns all known types declared in application with their description.",
        securityRule = SecurityRuleNames.PUBLIC,
        semantics = ActionDocSemantics(ActionDocSemanticsMode.NONE)
    )
    class InspectTypeSystem : ConfigAction

}

