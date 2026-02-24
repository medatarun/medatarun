package io.medatarun.model

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.model.actions.ModelActionProvider
import io.medatarun.model.adapters.descriptors.*
import io.medatarun.model.infra.ModelHumanPrinterEmoji
import io.medatarun.model.infra.ModelStoragesComposite
import io.medatarun.model.internal.ModelAuditor
import io.medatarun.model.internal.ModelCmdsImpl
import io.medatarun.model.internal.ModelQueriesImpl
import io.medatarun.model.internal.ModelValidationImpl
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.exposed.ModelHumanPrinter
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.model.ports.needs.ModelExporter
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.model.ports.needs.ModelRepository
import io.medatarun.model.ports.needs.ModelTagResolver
import io.medatarun.platform.kernel.ExtensionRegistry
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.platform.kernel.MedatarunServiceCtx
import io.medatarun.security.SecurityRolesRegistry
import io.medatarun.security.SecurityRolesRegistryImpl
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagQueries
import io.medatarun.tags.core.domain.TagRef
import io.medatarun.types.TypeDescriptor
import org.slf4j.LoggerFactory

/**
 * Extension to register the "model" base plugin to the kernel.
 */
open class ModelExtension : MedatarunExtension {
    override val id: String = "models-core"
    override fun initServices(ctx: MedatarunServiceCtx) {
        val extensionRegistry = ctx.getService(ExtensionRegistry::class)
        val tagQueries = ctx.getService(TagQueries::class)

        val auditor: ModelAuditor = object : ModelAuditor {
            override fun onCmdProcessed(cmd: ModelCmd) {
                logger.info("onCmdProcessed: $cmd")
            }
        }

        val validation = ModelValidationImpl()
        val storage = ModelStoragesComposite({
            extensionRegistry.findContributionsFlat(ModelRepository::class)
        }, validation)

        val tagResolver = object : ModelTagResolver {
            override fun resolveTagId(tagRef: TagRef): TagId {
                return tagQueries.findTagByRef(tagRef).id
            }
        }
        val modelQueriesImpl = ModelQueriesImpl(storage, tagResolver)
        val modelCmdsImpl = ModelCmdsImpl(storage, auditor, tagResolver)
        val modelHumanPrinterEmoji = ModelHumanPrinterEmoji()
        val securityRolesRegistry = SecurityRolesRegistryImpl(extensionRegistry)

        ctx.register(ModelCmds::class, modelCmdsImpl)
        ctx.register(ModelQueries::class, modelQueriesImpl)
        ctx.register(ModelHumanPrinter::class, modelHumanPrinterEmoji)
        ctx.register(SecurityRolesRegistry::class, securityRolesRegistry)
    }
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".repositories", ModelRepository::class)
        ctx.registerContributionPoint(this.id + ".importer", ModelImporter::class)
        ctx.registerContributionPoint(this.id + ".exporter", ModelExporter::class)
        ctx.register(ActionProvider::class, ModelActionProvider(ctx.createResourceLocator()))
        ctx.register(TypeDescriptor::class, AttributeKeyDescriptor())
        ctx.register(TypeDescriptor::class, EntityKeyDescriptor())
        ctx.register(TypeDescriptor::class, EntityRefDescriptor())
        ctx.register(TypeDescriptor::class, EntityAttributeRefDescriptor())
        ctx.register(TypeDescriptor::class, LocalizedMarkdownDescriptor())
        ctx.register(TypeDescriptor::class, LocalizedTextDescriptor())
        ctx.register(TypeDescriptor::class, ModelKeyDescriptor())
        ctx.register(TypeDescriptor::class, ModelRefDescriptor())
        ctx.register(TypeDescriptor::class, ModelVersionDescriptor())
        ctx.register(TypeDescriptor::class, RelationshipAttributeRefDescriptor())
        ctx.register(TypeDescriptor::class, RelationshipCardinalityDescriptor())
        ctx.register(TypeDescriptor::class, RelationshipKeyDescriptor())
        ctx.register(TypeDescriptor::class, RelationshipRefDescriptor())
        ctx.register(TypeDescriptor::class, RelationshipRoleKeyDescriptor())
        ctx.register(TypeDescriptor::class, RelationshipRoleRefDescriptor())
        ctx.register(TypeDescriptor::class, SearchFieldsDescriptor())
        ctx.register(TypeDescriptor::class, SearchFiltersDescriptor())
        ctx.register(TypeDescriptor::class, TypeKeyDescriptor())
        ctx.register(TypeDescriptor::class, TypeRefDescriptor())
    }

    companion object {
        private val logger = LoggerFactory.getLogger("audit")
    }
}
