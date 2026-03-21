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
import io.medatarun.tags.core.domain.TagCmdEnveloppe
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
            tagCmds.dispatch(
                TagCmdEnveloppe(
                    traceabilityRecord = evt.traceabilityRecord,
                    cmd = TagCmd.TagScopeDelete(evt.tagScopeRef)
                )
            )
        }

        ctx.register(TagCmds::class, tagCmds)
        ctx.register(TagQueries::class, tagQueries)
    }

    override fun initContributions(ctx: MedatarunExtensionCtx) {
        val tagCmds= ctx.getService<TagCmds>()
        val tagQueries= ctx.getService<TagQueries>()
        ctx.registerContributionPoint("$id.tag-scope-manager", TagScopeManager::class)
        ctx.registerContribution(TypeDescriptor::class, TagIdTypeDescriptor())
        ctx.registerContribution(TypeDescriptor::class, TagKeyTypeDescriptor())
        ctx.registerContribution(TypeDescriptor::class, TagRefTypeDescriptor())
        ctx.registerContribution(TypeDescriptor::class, TagScopeRefTypeDescriptor())
        ctx.registerContribution(TypeDescriptor::class, TagGroupKeyTypeDescriptor())
        ctx.registerContribution(TypeDescriptor::class, TagGroupRefTypeDescriptor())
        ctx.registerContribution(TypeDescriptor::class, TagSearchFiltersDescriptor())
        ctx.registerContribution(ActionProvider::class, TagActionProvider(tagCmds, tagQueries))
        ctx.registerContribution(SecurityRolesProvider::class, TagSecurityRolesProvider())
        ctx.registerContribution(SecurityRulesProvider::class, TagSecurityRulesprovider())
        ctx.registerContribution(DbMigration::class, TagsCoreDbMigration(id))
    }
}
