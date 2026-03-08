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
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.model.ports.needs.ModelExporter
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.model.ports.needs.ModelStorage
import io.medatarun.model.ports.needs.ModelTagResolver.Companion.modelTagScopeType
import io.medatarun.platform.db.DbConnectionFactory
import io.medatarun.platform.db.DbMigration
import io.medatarun.platform.db.DbTransactionManager
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx
import io.medatarun.tags.core.domain.TagCmds
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.tags.core.domain.TagScopeRef
import io.medatarun.tags.core.domain.TagScopeType
import io.medatarun.tags.core.ports.needs.TagScopeManager
import io.medatarun.types.TypeDescriptor
import org.slf4j.LoggerFactory

/**
 * Extension to register the "model" base plugin to the kernel.
 */
open class ModelExtension : MedatarunExtension {
    override val id: String = "models-core"
    override fun initServices(ctx: MedatarunServiceCtx) {
        val tagQueries = ctx.getService(TagQueries::class)
        val tagCmds = ctx.getService(TagCmds::class)
        val dbConnectionFactory = ctx.getService(DbConnectionFactory::class)
        val dbTransactionManager = ctx.getService(DbTransactionManager::class)

        val auditor: ModelAuditor = object : ModelAuditor {
            override fun onCmdProcessed(cmd: ModelCmd) {
                logger.info("onCmdProcessed: $cmd")
            }
        }

        val validation = ModelValidationImpl()
        val tagResolver = ModelTagResolverWithQueries(tagQueries, tagCmds)
        val storage: ModelStorage = ModelStorageDb(dbConnectionFactory)
        val modelQueriesImpl = ModelQueriesImpl(storage, tagResolver)
        val modelCmdsImpl = ModelCmdsImpl(storage, validation, auditor, tagResolver, dbTransactionManager)



        ctx.register(ModelCmds::class, modelCmdsImpl)
        ctx.register(ModelQueries::class, modelQueriesImpl)


    }

    override fun initContributions(ctx: MedatarunExtensionCtx) {
        val modelQueries = ctx.getService(ModelQueries::class)
        val modelTagScopeManager = object : TagScopeManager {
            override val type: TagScopeType = modelTagScopeType

            override fun localScopeExists(scopeRef: TagScopeRef.Local): Boolean {
                val modelId = ModelId(scopeRef.localScopeId.value)
                return modelQueries.findModelOptional(ModelRef.ById(modelId)) != null
            }
        }

        ctx.registerContributionPoint(this.id + ".importer", ModelImporter::class)
        ctx.registerContributionPoint(this.id + ".exporter", ModelExporter::class)
        ctx.registerContribution(TagScopeManager::class, modelTagScopeManager)
        ctx.registerContribution(ActionProvider::class, ModelActionProvider(ctx.createResourceLocator()))
        ctx.registerContribution(DbMigration::class, ModelStorageDbMigration(id))
        ctx.registerContribution(TypeDescriptor::class, AttributeKeyDescriptor())
        ctx.registerContribution(TypeDescriptor::class, EntityKeyDescriptor())
        ctx.registerContribution(TypeDescriptor::class, EntityRefDescriptor())
        ctx.registerContribution(TypeDescriptor::class, EntityAttributeRefDescriptor())
        ctx.registerContribution(TypeDescriptor::class, LocalizedMarkdownDescriptor())
        ctx.registerContribution(TypeDescriptor::class, LocalizedTextDescriptor())
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
    }

    companion object {
        private val logger = LoggerFactory.getLogger("audit")
    }
}
