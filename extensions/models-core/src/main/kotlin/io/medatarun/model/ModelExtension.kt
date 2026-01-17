package io.medatarun.model

import io.medatarun.actions.ports.needs.ActionProvider
import io.medatarun.model.actions.ModelActionProvider
import io.medatarun.model.domain.*
import io.medatarun.model.internal.KeyValidation
import io.medatarun.model.ports.needs.ModelImporter
import io.medatarun.model.ports.needs.ModelRepository
import io.medatarun.platform.kernel.MedatarunExtension
import io.medatarun.platform.kernel.MedatarunExtensionCtx
import io.medatarun.types.*
import kotlinx.serialization.json.*
import kotlin.reflect.KClass

/**
 * Extension to register the "model" base plugin to the kernel.
 */
class ModelExtension : MedatarunExtension {
    override val id: String = "models-core"
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
        ctx.register(TypeDescriptor::class, LocalizedTextDescriptor())
        ctx.register(TypeDescriptor::class, LocalizedMarkdownDescriptor())
        ctx.register(TypeDescriptor::class, RelationshipRoleKeyDescriptor())
        ctx.register(TypeDescriptor::class, RelationshipCardinalityDescriptor())
    }

}

class AttributeKeyDescriptor : TypeDescriptor<AttributeKey> {
    override val target: KClass<AttributeKey> = AttributeKey::class
    override val equivMultiplatorm: String = "AttributeKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: AttributeKey): AttributeKey {
        return value.validated()
    }

    override val description = KeyValidation.DESCRIPTION

}

class EntityKeyDescriptor : TypeDescriptor<EntityKey> {
    override val target: KClass<EntityKey> = EntityKey::class
    override val equivMultiplatorm: String = "EntityKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: EntityKey): EntityKey {
        return value.validated()
    }

    override val description = KeyValidation.DESCRIPTION

}

class RelationshipKeyDescriptor : TypeDescriptor<RelationshipKey> {
    override val target: KClass<RelationshipKey> = RelationshipKey::class
    override val equivMultiplatorm: String = "RelationshipKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: RelationshipKey): RelationshipKey {
        return value.validated()
    }

    override val description = KeyValidation.DESCRIPTION
}

class TypeKeyDescriptor : TypeDescriptor<TypeKey> {
    override val target: KClass<TypeKey> = TypeKey::class
    override val equivMultiplatorm: String = "TypeKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: TypeKey): TypeKey {
        return value.validated()
    }

    override val description = KeyValidation.DESCRIPTION
}

class ModelKeyDescriptor : TypeDescriptor<ModelKey> {
    override val target: KClass<ModelKey> = ModelKey::class
    override val equivMultiplatorm: String = "ModelKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: ModelKey): ModelKey {
        return value.validated()
    }

    override val description = KeyValidation.DESCRIPTION
}

class RelationshipRoleKeyDescriptor : TypeDescriptor<RelationshipRoleKey> {
    override val target: KClass<RelationshipRoleKey> = RelationshipRoleKey::class
    override val equivMultiplatorm: String = "RelationshipRoleKey"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: RelationshipRoleKey): RelationshipRoleKey {
        return value.validated()
    }

    override val description = KeyValidation.DESCRIPTION
}

class HashtagDescriptor : TypeDescriptor<Hashtag> {
    override val target: KClass<Hashtag> = Hashtag::class
    override val equivMultiplatorm: String = "Hashtag"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: Hashtag): Hashtag {
        return value.validated()
    }
}

class ModelVersionDescriptor : TypeDescriptor<ModelVersion> {
    override val target: KClass<ModelVersion> = ModelVersion::class
    override val equivMultiplatorm: String = "ModelVersion"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: ModelVersion): ModelVersion {
        return value.validate()
    }

    override val description: String = ModelVersion.DESCRIPTION
}

class LocalizedTextDescriptor : TypeDescriptor<LocalizedText> {
    override val target: KClass<LocalizedText> = LocalizedText::class
    override val equivMultiplatorm: String = "LocalizedText"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: LocalizedText): LocalizedText {
        return value.validate()
    }

    override val description: String = LOCALIZED_TEXT_DESCRIPTION
    override val jsonConverter: TypeJsonConverter<LocalizedText> = LocalizedTextTypeJsonConverter()
}

class LocalizedMarkdownDescriptor : TypeDescriptor<LocalizedMarkdown> {
    override val target: KClass<LocalizedMarkdown> = LocalizedMarkdown::class
    override val equivMultiplatorm: String = "LocalizedMarkdown"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: LocalizedMarkdown): LocalizedMarkdown {
        return value.validate()
    }
    override val description: String = LOCALIZED_MARKDOWN_DESCRIPTION
    override val jsonConverter: TypeJsonConverter<LocalizedMarkdown> = LocalizedMarkdownTypeJsonConverter()
}

class LocalizedMarkdownTypeJsonConverter : TypeJsonConverter<LocalizedMarkdown> {
    override fun deserialize(json: JsonElement): LocalizedMarkdown {
        return when (json) {
            is JsonNull -> throw TypeJsonConverterIllegalNullException()
            is JsonArray -> throw TypeJsonConverterBadFormatException("expected a JsonObject or a JsonString")
            is JsonObject -> {
                try {
                    LocalizedMarkdownMap(json.entries.associate { it.key to it.value.jsonPrimitive.content })
                } catch(e: Exception) {
                    throw TypeJsonConverterBadFormatException("Could not parse JSON object. ${e.message}: $json")
                }
            }
            is JsonPrimitive -> {
                if (json.isString) {
                    val content = json.contentOrNull ?: throw TypeJsonConverterIllegalNullException()
                    return LocalizedMarkdownNotLocalized(content)
                } else {
                    throw TypeJsonConverterBadFormatException("expected a JsonObject or a JsonString")
                }
            }
        }
    }
}

class LocalizedTextTypeJsonConverter : TypeJsonConverter<LocalizedText> {
    override fun deserialize(json: JsonElement): LocalizedText {
        return when (json) {
            is JsonNull -> throw TypeJsonConverterIllegalNullException()
            is JsonArray -> throw TypeJsonConverterBadFormatException("expected a JsonObject or a JsonString")
            is JsonObject -> {
                try {
                    LocalizedTextMap(json.entries.associate { it.key to it.value.jsonPrimitive.content })
                } catch(e: Exception) {
                    throw TypeJsonConverterBadFormatException("Could not parse JSON object. ${e.message}: $json")
                }
            }
            is JsonPrimitive -> {
                if (json.isString) {
                    val content = json.contentOrNull ?: throw TypeJsonConverterIllegalNullException()
                    return LocalizedTextNotLocalized(content)
                } else {
                    throw TypeJsonConverterBadFormatException("expected a JsonObject or a JsonString")
                }
            }
        }
    }
}

class RelationshipCardinalityDescriptor : TypeDescriptor<RelationshipCardinality> {
    override val target: KClass<RelationshipCardinality> = RelationshipCardinality::class
    override val equivMultiplatorm: String = "RelationshipCardinality"
    override val equivJson: TypeJsonEquiv = TypeJsonEquiv.STRING
    override fun validate(value: RelationshipCardinality): RelationshipCardinality {
        return value
    }

    override val description: String = ""
}