package io.medatarun.tags.core

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbTransactionManager
import io.medatarun.platform.kernel.*
import io.medatarun.security.*
import io.medatarun.tags.core.actions.TagActionProvider
import io.medatarun.tags.core.actions.TagSecurityRuleNames
import io.medatarun.tags.core.adapters.*
import io.medatarun.tags.core.domain.TagCmds
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.tags.core.internal.*
import io.medatarun.tags.core.ports.needs.TagScopeManager
import io.medatarun.types.TypeDescriptor

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

class TagsCoreExtension : MedatarunExtension {
    override val id = "tags-core"
    override fun initServices(ctx: MedatarunServiceCtx) {
        val dbConnectionFactory = ctx.getService(DbConnectionFactory::class)
        val dbTransactionManager = ctx.getService(DbTransactionManager::class)
        val extensionRegistry = ctx.getService(ExtensionRegistry::class)
        val storage = TagStorageSQLite(dbConnectionFactory)
        val tagScopeRegistry = TagScopeRegistryImpl(TagScopeManagerResolverImpl(extensionRegistry))
        val tagCmdEvents = TagCmdsEventsHandler(tagScopeRegistry)
        val tagScopes = TagScopesImpl(tagScopeRegistry)
        val tagCmds = TagCmdsImpl(storage, tagScopes, tagCmdEvents, dbTransactionManager)
        val tagQueries = TagQueriesImpl(storage)

        ctx.register(TagCmds::class, tagCmds)
        ctx.register(TagQueries::class, tagQueries)
    }

    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint("$id.tag-scope-manager", TagScopeManager::class)
        ctx.register(TypeDescriptor::class, TagIdTypeDescriptor())
        ctx.register(TypeDescriptor::class, TagKeyTypeDescriptor())
        ctx.register(TypeDescriptor::class, TagRefTypeDescriptor())
        ctx.register(TypeDescriptor::class, TagScopeRefTypeDescriptor())
        ctx.register(TypeDescriptor::class, TagGroupKeyTypeDescriptor())
        ctx.register(TypeDescriptor::class, TagGroupRefTypeDescriptor())
        ctx.register(ActionProvider::class, TagActionProvider())
        ctx.register(SecurityRolesProvider::class, object : SecurityRolesProvider {
            override fun getRoles(): List<AppPrincipalRole> {
                return listOf(TagFreeManageRole, TagManagedManageRole, TagGroupManageRole)
            }
        })
        ctx.register(SecurityRulesProvider::class, object : SecurityRulesProvider {
            override fun getRules(): List<SecurityRuleEvaluator> {
                return listOf(
                    object : SecurityRuleEvaluator {
                        override val key: String = TagSecurityRuleNames.TAG_MANAGED_MANAGE
                        override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensureRole(TagManagedManageRole)
                    },
                    object : SecurityRuleEvaluator {
                        override val key: String = TagSecurityRuleNames.TAG_FREE_MANAGE
                        override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensureRole(TagFreeManageRole)
                    },
                    object : SecurityRuleEvaluator {
                        override val key: String = TagSecurityRuleNames.TAG_GROUP_MANAGE
                        override fun evaluate(ctx: SecurityRuleCtx) = ctx.ensureRole(TagGroupManageRole)
                    }
                )
            }

        })

        ctx.register(PlatformStartedListener::class, object : PlatformStartedListener {
            override fun onPlatformStarted(ctx: PlatformStartedCtx) {
                val db = ctx.services.getService<DbConnectionFactory>()
                TagStorageSQLite(db).initSchema()
            }
        })
    }
}
