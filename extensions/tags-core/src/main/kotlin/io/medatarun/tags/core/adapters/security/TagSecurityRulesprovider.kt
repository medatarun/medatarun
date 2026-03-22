package io.medatarun.tags.core.adapters.security

import io.medatarun.security.SecurityRuleCtx
import io.medatarun.security.SecurityRuleEvaluator
import io.medatarun.security.SecurityRulesProvider

class TagSecurityRulesprovider : SecurityRulesProvider {
    override fun getRules(): List<SecurityRuleEvaluator> {
        return listOf(
            object : SecurityRuleEvaluator {
                override val key: String = TagSecurityRules.TAG_GLOBAL_MANAGE
                override val name: String = "Manage Managed Tags"
                override val description: String =
                    "Actors (users and tools) can manage managed tags.\n\n" +
                        "Required role: `tag_managed_manage`."
                override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensureRole(TagManagedManageRole)
            },
            object : SecurityRuleEvaluator {
                override val key: String = TagSecurityRules.TAG_LOCAL_MANAGE
                override val name: String = "Manage Free Tags"
                override val description: String =
                    "Actors (users and tools) can manage free tags.\n\n" +
                        "Required role: `tag_free_manage`."
                override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensureRole(TagFreeManageRole)
            },
            object : SecurityRuleEvaluator {
                override val key: String = TagSecurityRules.TAG_GROUP_MANAGE
                override val name: String = "Manage Tag Groups"
                override val description: String =
                    "Actors (users and tools) can manage tag groups.\n\n" +
                        "Required role: `tag_group_manage`."
                override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensureRole(TagGroupManageRole)
            }
        )
    }

}
