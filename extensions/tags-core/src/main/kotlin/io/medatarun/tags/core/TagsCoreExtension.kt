package io.medatarun.tags.core

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.db.DbTransactionManager
import io.medatarun.platform.kernel.*
import io.medatarun.security.SecurityRolesProvider
import io.medatarun.security.SecurityRulesProvider
import io.medatarun.tags.core.actions.TagActionProvider
import io.medatarun.tags.core.adapters.security.TagSecurityRolesProvider
import io.medatarun.tags.core.adapters.security.TagSecurityRulesprovider
import io.medatarun.tags.core.adapters.types.*
import io.medatarun.tags.core.domain.TagCmd
import io.medatarun.tags.core.domain.TagCmds
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.tags.core.domain.TagScopeBeforeDeleteEvent
import io.medatarun.tags.core.infra.db.TagStorageSQLite
import io.medatarun.tags.core.infra.db.TagsCoreDbMigration
import io.medatarun.tags.core.internal.*
import io.medatarun.tags.core.ports.needs.TagScopeManager
import io.medatarun.types.TypeDescriptor

class TagsCoreExtension : MedatarunExtension {
    override val id = "tags-core"
    override fun initServices(ctx: MedatarunServiceCtx) {
        val dbConnectionFactory = ctx.getService(DbConnectionFactory::class)
        val dbTransactionManager = ctx.getService(DbTransactionManager::class)
        val extensionRegistry = ctx.getService(ExtensionRegistry::class)
        val storage = TagStorageSQLite(dbConnectionFactory)
        val tagScopeRegistry = TagScopeRegistryImpl(TagScopeManagerResolverImpl(extensionRegistry))
        val eventSystem = ctx.getService(EventSystem::class)
        val tagCmdEvents = TagCmdsEventsHandler(eventSystem)
        val tagScopes = TagScopesImpl(tagScopeRegistry)
        val tagCmds = TagCmdsImpl(storage, tagScopes, tagCmdEvents, dbTransactionManager)
        val tagQueries = TagQueriesImpl(storage, tagScopes)
        eventSystem.registerObserver(TagScopeBeforeDeleteEvent::class) { evt ->
            tagCmds.dispatch(TagCmd.TagScopeDelete(evt.tagScopeRef))
        }

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
        ctx.register(TypeDescriptor::class, TagSearchFiltersDescriptor())
        ctx.register(ActionProvider::class, TagActionProvider())
        ctx.register(SecurityRolesProvider::class, TagSecurityRolesProvider())
        ctx.register(SecurityRulesProvider::class, TagSecurityRulesprovider())
        ctx.register(DbMigration::class, TagsCoreDbMigration(id))
    }
}
