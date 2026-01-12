package io.medatarun.actions

import io.medatarun.actions.actions.BatchActionProvider
import io.medatarun.actions.actions.SecurityRuleNames
import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.security.SecurityRuleCtx
import io.medatarun.security.SecurityRuleEvaluator
import io.medatarun.security.SecurityRuleEvaluatorResult
import io.medatarun.security.SecurityRulesProvider

class ActionsExtension : MedatarunExtension {
    override val id: String = "actions"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".providers", ActionProvider::class)
        ctx.registerContributionPoint(this.id + ".security_rules_providers", SecurityRulesProvider::class)
        ctx.register(ActionProvider::class, BatchActionProvider())
        ctx.register(SecurityRulesProvider::class, SecurityRulesProviderBase())
    }
}

class SecurityRulesProviderBase : SecurityRulesProvider {

    override fun getRules(): List<SecurityRuleEvaluator> {
        val rulePublic: SecurityRuleEvaluator = object : SecurityRuleEvaluator {
            override val key: String = SecurityRuleNames.PUBLIC
            override fun evaluate(ctx: SecurityRuleCtx) = SecurityRuleEvaluatorResult.Ok()
        }
        val ruleSignedIn: SecurityRuleEvaluator = object : SecurityRuleEvaluator {
            override val key: String = SecurityRuleNames.SIGNED_IN
            override fun evaluate(ctx: SecurityRuleCtx) =
                if (ctx.isSignedIn()) SecurityRuleEvaluatorResult.Ok() else SecurityRuleEvaluatorResult.Error("You must be signed in.")
        }
        val ruleAdmin: SecurityRuleEvaluator = object : SecurityRuleEvaluator {
            override val key: String = SecurityRuleNames.ADMIN
            override fun evaluate(ctx: SecurityRuleCtx) =
                if (ctx.isAdmin()) SecurityRuleEvaluatorResult.Ok() else SecurityRuleEvaluatorResult.Error("You must have an administrator role.")
        }
        return listOf(rulePublic, ruleAdmin, ruleSignedIn)
    }

}