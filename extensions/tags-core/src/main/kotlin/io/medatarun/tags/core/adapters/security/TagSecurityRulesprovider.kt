package io.medatarun.tags.core.adapters.security

import io.medatarun.security.AppPermission
import io.medatarun.security.AppPermissionKey
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
                        "Required permission: `${TagGlobalManagePermission.key}`."
                override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensurePermission(TagGlobalManagePermission)
                override fun associatedRequiredPermissions(): List<AppPermissionKey> {
                    return listOf(TagGlobalManagePermission.key)
                }
            },
            object : SecurityRuleEvaluator {
                override val key: String = TagSecurityRules.TAG_LOCAL_MANAGE
                override val name: String = "Manage local tags"
                override val description: String =
                    "Actors (users and tools) can manage local tags.\n\n" +
                        "Required permission: `${TagLocalManagePermission.key}`."
                override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensurePermission(TagLocalManagePermission)
                override fun associatedRequiredPermissions(): List<AppPermissionKey> {
                    return listOf(TagLocalManagePermission.key)
                }
            },
            object : SecurityRuleEvaluator {
                override val key: String = TagSecurityRules.TAG_GROUP_MANAGE
                override val name: String = "Manage Tag Groups"
                override val description: String =
                    "Actors (users and tools) can manage tag groups.\n\n" +
                        "Required permission: `${TagGroupManagePermission.key}`."
                override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensurePermission(TagGroupManagePermission)
                override fun associatedRequiredPermissions(): List<AppPermissionKey> {
                    return listOf(TagGroupManagePermission.key)
                }
            }
        )
    }

}
