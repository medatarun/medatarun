package io.medatarun.model

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.model.actions.ModelActionProvider
import io.medatarun.model.adapters.*
import io.medatarun.model.ports.needs.ModelExporter
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.model.ports.needs.ModelRepository
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.types.TypeDescriptor

/**
 * Extension to register the "model" base plugin to the kernel.
 */
class ModelExtension : MedatarunExtension {
    override val id: String = "models-core"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".repositories", ModelRepository::class)
        ctx.registerContributionPoint(this.id + ".importer", ModelImporter::class)
        ctx.registerContributionPoint(this.id + ".exporter", ModelExporter::class)
        ctx.register(ActionProvider::class, ModelActionProvider(ctx.createResourceLocator()))
        ctx.register(TypeDescriptor::class, AttributeKeyDescriptor())
        ctx.register(TypeDescriptor::class, EntityKeyDescriptor())
        ctx.register(TypeDescriptor::class, HashtagDescriptor())
        ctx.register(TypeDescriptor::class, LocalizedMarkdownDescriptor())
        ctx.register(TypeDescriptor::class, LocalizedTextDescriptor())
        ctx.register(TypeDescriptor::class, ModelKeyDescriptor())
        ctx.register(TypeDescriptor::class, ModelVersionDescriptor())
        ctx.register(TypeDescriptor::class, RelationshipCardinalityDescriptor())
        ctx.register(TypeDescriptor::class, RelationshipKeyDescriptor())
        ctx.register(TypeDescriptor::class, RelationshipRoleKeyDescriptor())
        ctx.register(TypeDescriptor::class, TypeKeyDescriptor())
    }

}

