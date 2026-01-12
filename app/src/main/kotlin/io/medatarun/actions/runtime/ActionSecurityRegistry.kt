package io.medatarun.actions.runtime

import io.medatarun.actions.ports.needs.ActionCtx
import io.medatarun.kernel.ExtensionRegistry
import io.medatarun.security.*

class ActionSecurityRegistry(private val extensionRegistry: ExtensionRegistry) {
    private val ruleProviders = extensionRegistry.findContributionsFlat(SecurityRulesProvider::class)
    private val rules = ruleProviders.flatMap { it.getRules() }.associateBy { it.key }

    fun findEvaluatorOptional(ruleKey: String): SecurityRuleEvaluator? {
        return rules[ruleKey]
    }

    fun evaluateSecurity(ruleKey: String, actionCtx: ActionCtx): Boolean {
        val e = rules[ruleKey] ?: throw SecurityRuleEvaluatorNotFoundException(ruleKey)
        val ctx = object : SecurityRuleCtx {
            override fun isSignedIn(): Boolean {
                return actionCtx.principal != null
            }

            override fun isAdmin(): Boolean {
                val p = actionCtx.principal.principal
                return p != null && p.isAdmin
            }

            override fun getRoles(): List<AppPrincipalRole> {
                val roles = actionCtx.principal.principal?.roles ?: emptyList()
                return roles
            }
        }
        return e.evaluate(ctx)
    }
}