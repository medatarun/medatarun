package io.medatarun.tags.core

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx
import io.medatarun.platform.kernel.PlatformStartedCtx
import io.medatarun.platform.kernel.PlatformStartedListener
import io.medatarun.platform.kernel.getService
import io.medatarun.security.AppPrincipalRole
import io.medatarun.security.SecurityRolesProvider
import io.medatarun.security.SecurityRuleCtx
import io.medatarun.security.SecurityRuleEvaluator
import io.medatarun.tags.core.actions.TagActionProvider
import io.medatarun.tags.core.actions.TagSecurityRuleNames
import io.medatarun.tags.core.adapters.TagStorageSQLite
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
        val dbConnectionFactory = ctx.getService(DbConnectionFactory::class)
        val storage = TagStorageSQLite(dbConnectionFactory)
        ctx.register(TagCmds::class, TagCmdsImpl(storage))
        ctx.register(TagQueries::class, TagQueriesImpl(storage))
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
        ctx.register(PlatformStartedListener::class, object: PlatformStartedListener {
            override fun onPlatformStarted(ctx: PlatformStartedCtx) {
                val db = ctx.services.getService<DbConnectionFactory>()
                TagStorageSQLite(db).initSchema()
            }
        })
    }
}