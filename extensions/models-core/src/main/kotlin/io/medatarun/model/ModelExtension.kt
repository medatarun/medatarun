package io.medatarun.model

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.model.actions.ModelActionProvider
import io.medatarun.model.adapters.descriptors.*
import io.medatarun.model.domain.ModelId
import io.medatarun.model.domain.ModelRef
import io.medatarun.model.infra.db.ModelStorageDb
import io.medatarun.model.infra.db.ModelStorageDbMigration
import io.medatarun.model.internal.ModelAuditor
import io.medatarun.model.internal.ModelCmdsImpl
import io.medatarun.model.internal.ModelQueriesImpl
import io.medatarun.model.internal.ModelValidationImpl
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.model.ports.exposed.ModelCmdEnveloppe
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.model.ports.needs.ModelClock
import io.medatarun.model.ports.needs.ModelExporter
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.model.ports.needs.ModelStorage
import io.medatarun.model.ports.needs.ModelTagResolver.Companion.modelTagScopeType
import io.medatarun.model.security.ModelSecurityPermissionsProvider
import io.medatarun.model.security.ModelSecurityRulesProvider
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.db.DbTransactionManager
import io.medatarun.platform.kernel.*
import io.medatarun.security.AppActorResolver
import io.medatarun.security.SecurityPermissionsProvider
import io.medatarun.security.SecurityRulesProvider
import io.medatarun.tags.core.domain.TagBeforeDeleteEvt
import io.medatarun.tags.core.domain.TagCmds
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.tags.core.domain.TagLocalScopeBeforeDeleteEvent
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.domain.TagScopeType
import io.medatarun.tags.core.ports.needs.TagScopeManager
import io.medatarun.types.TypeDescriptor
import org.slf4j.LoggerFactory
import java.time.Instant

/**
 * Extension to register the "model" base plugin to the kernel.
 */
open class ModelExtension(
    private val config: ModelExtensionConfig = ModelExtensionConfigProd()
) : MedatarunExtension {
    override val id: String = "models-core"
    override fun initServices(ctx: MedatarunServiceCtx) {
        val tagQueries = ctx.getService(TagQueries::class)
        val tagCmds = ctx.getService(TagCmds::class)
        val eventSystem = ctx.getService<EventSystem>()
        val tagScopeBeforeDeleteNotifier = eventSystem.createNotifier(TagLocalScopeBeforeDeleteEvent::class)
        val dbConnectionFactory = ctx.getService(DbConnectionFactory::class)
        val dbTransactionManager = ctx.getService(DbTransactionManager::class)
        val actorResolver = ctx.getService<AppActorResolver>()
        val auditor: ModelAuditor = object : ModelAuditor {
            override fun onCmdProcessed(cmd: ModelCmdEnveloppe) {
                val traceabilityRecord = cmd.traceabilityRecord
                val actorId = traceabilityRecord.actorId
                val actorDisplayName = actorResolver.resolve(traceabilityRecord.actorId)?.displayName
                val origin = traceabilityRecord.origin
                logger.info(
                    "onCmdProcessed: actor=$actorDisplayName actorId=$actorId origin=$origin $cmd"
                )
            }
        }

        val validation = ModelValidationImpl()
        val tagResolver = ModelTagResolverWithQueries(tagQueries, tagCmds, tagScopeBeforeDeleteNotifier)
        val storage: ModelStorage = ModelStorageDb(dbConnectionFactory, config.modelClock)
        val modelQueriesImpl = ModelQueriesImpl(storage, tagResolver)
        val modelCmdsImpl = ModelCmdsImpl(storage, validation, auditor, tagResolver, dbTransactionManager)

        eventSystem.registerObserver(TagBeforeDeleteEvt::class) { evt ->
            modelCmdsImpl.dispatch(
                ModelCmdEnveloppe(
                    traceabilityRecord = evt.traceabilityRecord,
                    cmd = ModelCmd.RemoveTagReferences(evt.id)
                )
            )
        }

        ctx.register(ModelCmds::class, modelCmdsImpl)
        ctx.register(ModelQueries::class, modelQueriesImpl)



    }

    override fun initContributions(ctx: MedatarunExtensionCtx) {
        val extensionRegistry = ctx.getService<ExtensionRegistry>()
        val modelQueries = ctx.getService<ModelQueries>()
        val modelCmds = ctx.getService<ModelCmds>()
        val actorResolver = ctx.getService<AppActorResolver>()
        val tagQueries = ctx.getService<TagQueries>()
        val modelTagScopeManager = object : TagScopeManager {
            override val type: TagScopeType = modelTagScopeType

            override fun localScopeExists(scopeRef: TagScopeRef.Local): Boolean {
                val modelId = ModelId(scopeRef.localScopeId.value)
                return modelQueries.existsModel(ModelRef.ById(modelId))
            }
        }

        val actionProvider = ModelActionProvider(ctx.createResourceLocator(), extensionRegistry, modelCmds, modelQueries, actorResolver, tagQueries)

        ctx.registerContributionPoint(this.id + ".importer", ModelImporter::class)
        ctx.registerContributionPoint(this.id + ".exporter", ModelExporter::class)
        ctx.registerContribution(TagScopeManager::class, modelTagScopeManager)
        ctx.registerContribution(ActionProvider::class, actionProvider)
        ctx.registerContribution(DbMigration::class, ModelStorageDbMigration(id))
        ctx.registerContribution(TypeDescriptor::class, AttributeKeyDescriptor())
        ctx.registerContribution(TypeDescriptor::class, BusinessKeyKeyDescriptor())
        ctx.registerContribution(TypeDescriptor::class, BusinessKeyRefDescriptor())
        ctx.registerContribution(TypeDescriptor::class, EntityKeyDescriptor())
        ctx.registerContribution(TypeDescriptor::class, EntityRefDescriptor())
        ctx.registerContribution(TypeDescriptor::class, EntityAttributeRefDescriptor())
        ctx.registerContribution(TypeDescriptor::class, TextMarkdownDescriptor())
        ctx.registerContribution(TypeDescriptor::class, TextSingleLineDescriptor())
        ctx.registerContribution(TypeDescriptor::class, ModelAuthorityDescriptor())
        ctx.registerContribution(TypeDescriptor::class, ModelDiffScopeDescriptor())
        ctx.registerContribution(TypeDescriptor::class, ModelKeyDescriptor())
        ctx.registerContribution(TypeDescriptor::class, ModelRefDescriptor())
        ctx.registerContribution(TypeDescriptor::class, ModelVersionDescriptor())
        ctx.registerContribution(TypeDescriptor::class, RelationshipAttributeRefDescriptor())
        ctx.registerContribution(TypeDescriptor::class, RelationshipCardinalityDescriptor())
        ctx.registerContribution(TypeDescriptor::class, RelationshipKeyDescriptor())
        ctx.registerContribution(TypeDescriptor::class, RelationshipRefDescriptor())
        ctx.registerContribution(TypeDescriptor::class, RelationshipRoleKeyDescriptor())
        ctx.registerContribution(TypeDescriptor::class, RelationshipRoleRefDescriptor())
        ctx.registerContribution(TypeDescriptor::class, SearchFieldsDescriptor())
        ctx.registerContribution(TypeDescriptor::class, SearchFiltersDescriptor())
        ctx.registerContribution(TypeDescriptor::class, TypeKeyDescriptor())
        ctx.registerContribution(TypeDescriptor::class, TypeRefDescriptor())
        ctx.registerContribution(SecurityPermissionsProvider::class, ModelSecurityPermissionsProvider())
        ctx.registerContribution(SecurityRulesProvider::class, ModelSecurityRulesProvider())

    }

    companion object {
        private val logger = LoggerFactory.getLogger("audit")
    }
}

interface ModelExtensionConfig {
    val modelClock: ModelClock
}

class ModelExtensionConfigProd : ModelExtensionConfig {
    override val modelClock = object : ModelClock {
        override fun now(): Instant = Instant.now()
    }
}
