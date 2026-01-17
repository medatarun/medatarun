package io.medatarun.security

import io.medatarun.platform.kernel.ExtensionId
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx

class SecurityExtension : MedatarunExtension {
    override val id: ExtensionId = "platform-security"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".security_rules_providers", SecurityRulesProvider::class)
        ctx.registerContributionPoint(this.id + ".security_roles_providers", SecurityRolesProvider::class)
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