package io.medatarun.model

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.kernel.MedatarunExtension
import io.medatarun.kernel.MedatarunExtensionCtx
import io.medatarun.model.actions.ModelActionProvider
import io.medatarun.model.domain.*
import io.medatarun.model.internal.KeyValidation
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.model.ports.needs.ModelRepository
import io.medatarun.types.JsonTypeEquiv
import io.medatarun.types.TypeDescriptor
import kotlin.reflect.KClass

/**
 * Extension to register the "model" base plugin to the kernel.
 */
class ModelExtension: MedatarunExtension {
    override val id: String = "model"
    override fun init(ctx: MedatarunExtensionCtx) {
        ctx.registerContributionPoint(this.id + ".repositories", ModelRepository::class)
        ctx.registerContributionPoint(this.id + ".importer", ModelImporter::class)
        ctx.register(ActionProvider::class, ModelActionProvider(ctx.createResourceLocator()))
        ctx.register(TypeDescriptor::class, AttributeKeyDescriptor())
        ctx.register(TypeDescriptor::class, EntityKeyDescriptor())
        ctx.register(TypeDescriptor::class, RelationshipKeyDescriptor())
        ctx.register(TypeDescriptor::class, TypeKeyDescriptor())
        ctx.register(TypeDescriptor::class, ModelKeyDescriptor())
        ctx.register(TypeDescriptor::class, HashtagDescriptor())
        ctx.register(TypeDescriptor::class, ModelVersionDescriptor())
    }

}

class AttributeKeyDescriptor : TypeDescriptor<AttributeKey> {
    override val target: KClass<AttributeKey> = AttributeKey::class
    override val equivMultiplatorm: String = "AttributeKey"
    override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING
    override fun validate(value: AttributeKey): AttributeKey {
        return value.validated()
    }
    override val description = KeyValidation.DESCRIPTION

}
class EntityKeyDescriptor : TypeDescriptor<EntityKey> {
    override val target: KClass<EntityKey> = EntityKey::class
    override val equivMultiplatorm: String = "EntityKey"
    override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING
    override fun validate(value: EntityKey): EntityKey {
        return value.validated()
    }
    override val description = KeyValidation.DESCRIPTION

}
class RelationshipKeyDescriptor : TypeDescriptor<RelationshipKey> {
    override val target: KClass<RelationshipKey> = RelationshipKey::class
    override val equivMultiplatorm: String = "RelationshipKey"
    override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING
    override fun validate(value: RelationshipKey): RelationshipKey {
        return value.validated()
    }
    override val description = KeyValidation.DESCRIPTION
}
class TypeKeyDescriptor : TypeDescriptor<TypeKey> {
    override val target: KClass<TypeKey> = TypeKey::class
    override val equivMultiplatorm: String = "TypeKey"
    override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING
    override fun validate(value: TypeKey): TypeKey {
        return value.validated()
    }
    override val description = KeyValidation.DESCRIPTION
}
class ModelKeyDescriptor : TypeDescriptor<ModelKey> {
    override val target: KClass<ModelKey> = ModelKey::class
    override val equivMultiplatorm: String = "ModelKey"
    override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING
    override fun validate(value: ModelKey): ModelKey {
        return value.validated()
    }
    override val description = KeyValidation.DESCRIPTION
}
class HashtagDescriptor : TypeDescriptor<Hashtag> {
    override val target: KClass<Hashtag> = Hashtag::class
    override val equivMultiplatorm: String = "Hashtag"
    override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING
    override fun validate(value: Hashtag): Hashtag {
        return value.validated()
    }
}
class ModelVersionDescriptor : TypeDescriptor<ModelVersion> {
    override val target: KClass<ModelVersion> = ModelVersion::class
    override val equivMultiplatorm: String = "ModelVersion"
    override val equivJson: JsonTypeEquiv = JsonTypeEquiv.STRING
    override fun validate(value: ModelVersion): ModelVersion {
        return value.validate()
    }

    override val description: String = """
ModelVersion follows Semantic Versioning (MAJOR.MINOR.PATCH).

Each part is a number, for example 1.2.3. The version must not be empty.

An optional pre-release can be added after -, using dot-separated identifiers, for example 1.2.3-alpha or 1.2.3-alpha.1.
Optional build metadata can be added after +, for example 1.2.3+build.1. Pre-release and build metadata can be combined.

Numeric identifiers (major, minor, patch, and numeric pre-release parts) must not contain leading zeros.
Pre-release and build identifiers may only contain letters, digits, and hyphens.

This format allows versions to be compared and ordered consistently over time.     
""".trimIndent()
}
