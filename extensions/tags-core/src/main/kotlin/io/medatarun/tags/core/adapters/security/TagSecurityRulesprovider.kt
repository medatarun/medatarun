package io.medatarun.tags.core.adapters.security

import io.medatarun.security.SecurityRuleCtx
import io.medatarun.security.SecurityRuleEvaluator
import io.medatarun.security.SecurityRulesProvider

class TagSecurityRulesprovider : SecurityRulesProvider {
    override fun getRules(): List<SecurityRuleEvaluator> {
        return listOf(
            object : SecurityRuleEvaluator {
                override val key: String = TagSecurityRules.TAG_GLOBAL_MANAGE
                override val name: String = "Manage global tags"
                override val description: String =
                    "Actors (users and tools) can manage global tags.\n\n" +
                        "Required permission: `${TagGlobalManageRole.key}`."
                override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensureRole(TagGlobalManageRole)
            },
            object : SecurityRuleEvaluator {
                override val key: String = TagSecurityRules.TAG_LOCAL_MANAGE
                override val name: String = "Manage local tags"
                override val description: String =
                    "Actors (users and tools) can manage local tags.\n\n" +
                        "Required permission: `${TagLocalManageRole.key}`."
                override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensureRole(TagLocalManageRole)
            },
            object : SecurityRuleEvaluator {
                override val key: String = TagSecurityRules.TAG_GROUP_MANAGE
                override val name: String = "Manage Tag Groups"
                override val description: String =
                    "Actors (users and tools) can manage tag groups.\n\n" +
                        "Required permission: `${TagGroupManageRole.key}`."
                override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensureRole(TagGroupManageRole)
            }
        )
    }

}
