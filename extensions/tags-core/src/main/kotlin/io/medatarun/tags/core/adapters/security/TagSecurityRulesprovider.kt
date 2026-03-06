package io.medatarun.tags.core.adapters.security

import io.medatarun.security.SecurityRuleCtx
import io.medatarun.security.SecurityRuleEvaluator
import io.medatarun.security.SecurityRulesProvider

class TagSecurityRulesprovider : SecurityRulesProvider {
    override fun getRules(): List<SecurityRuleEvaluator> {
        return listOf(
            object : SecurityRuleEvaluator {
                override val key: String = TagSecurityRules.TAG_MANAGED_MANAGE
                override val description: String = "User must be allowed to manage managed tags."
                override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensureRole(TagManagedManageRole)
            },
            object : SecurityRuleEvaluator {
                override val key: String = TagSecurityRules.TAG_FREE_MANAGE
                override val description: String = "User must be allowed to manage free tags."
                override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensureRole(TagFreeManageRole)
            },
            object : SecurityRuleEvaluator {
                override val key: String = TagSecurityRules.TAG_GROUP_MANAGE
                override val description: String = "User must be allowed to manage tag groups."
                override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensureRole(TagGroupManageRole)
            }
        )
    }

}
