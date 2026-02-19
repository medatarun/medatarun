package io.medatarun.tags.core

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx
import io.medatarun.security.AppPrincipalRole
import io.medatarun.security.SecurityRolesProvider
import io.medatarun.security.SecurityRuleCtx
import io.medatarun.security.SecurityRuleEvaluator
import io.medatarun.tags.core.actions.TagActionProvider
import io.medatarun.tags.core.actions.TagSecurityRuleNames
import io.medatarun.tags.core.domain.TagCmds
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.tags.core.internal.TagCmdsImpl
import io.medatarun.tags.core.internal.TagQueriesImpl

object TagFreeManageRole : AppPrincipalRole {
    override val key: String
        get() = "tag_free_manage"
}

object TagManagedManageRole : AppPrincipalRole {
    override val key: String
        get() = "tag_managed_manage"
}

object TagGroupManageRole : AppPrincipalRole {
    override val key: String
        get() = "tag_group_manage"
}

class TagsCoreExtension() : MedatarunExtension {
    override val id = "tags-core"
    override fun initServices(ctx: MedatarunServiceCtx) {
        ctx.register(TagCmds::class, TagCmdsImpl())
        ctx.register(TagQueries::class, TagQueriesImpl())
    }
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.register(ActionProvider::class, TagActionProvider())
        ctx.register(SecurityRolesProvider::class, object : SecurityRolesProvider {
            override fun getRoles(): List<AppPrincipalRole> {
                return listOf(TagFreeManageRole, TagManagedManageRole, TagGroupManageRole)
            }
        })
        ctx.register(SecurityRuleEvaluator::class, object : SecurityRuleEvaluator {
            override val key: String = TagSecurityRuleNames.TAG_MANAGED_MANAGE
            override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensureRole(TagManagedManageRole)
        })
        ctx.register(SecurityRuleEvaluator::class, object : SecurityRuleEvaluator {
            override val key: String = TagSecurityRuleNames.TAG_FREE_MANAGE
            override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensureRole(TagFreeManageRole)
        })
        ctx.register(SecurityRuleEvaluator::class, object : SecurityRuleEvaluator {
            override val key: String = TagSecurityRuleNames.TAG_GROUP_MANAGE
            override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensureRole(TagGroupManageRole)
        })
    }
}