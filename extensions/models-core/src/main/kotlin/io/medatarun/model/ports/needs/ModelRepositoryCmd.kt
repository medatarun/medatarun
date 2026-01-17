package io.medatarun.model.ports.needs

import io.medatarun.model.domain.*
import io.medatarun.model.infra.EntityDefInMemory
import io.medatarun.model.ports.exposed.*
import java.net.URL

sealed interface ModelRepositoryCmdOnModel : ModelRepositoryCmd {
    val modelKey: ModelKey
}

sealed interface ModelRepositoryCmd {


    // ------------------------------------------------------------------------
    // Models
    // ------------------------------------------------------------------------

    data class CreateModel(
        val model: Model
    ) : ModelRepositoryCmd

    data class UpdateModelName(
        override val modelKey: ModelKey,
        val name: LocalizedTextNotLocalized
    ) : ModelRepositoryCmdOnModel

    data class UpdateModelDescription(
        override val modelKey: ModelKey,
        val description: LocalizedTextNotLocalized?
    ) : ModelRepositoryCmdOnModel

    data class UpdateModelVersion(
        override val modelKey: ModelKey,
        val version: ModelVersion
    ) : ModelRepositoryCmdOnModel

    data class UpdateModelDocumentationHome(
        override val modelKey: ModelKey,
        val url: URL?
    ) : ModelRepositoryCmdOnModel

    data class UpdateModelHashtagAdd(
        override val modelKey: ModelKey,
        val hashtag: Hashtag
    ) : ModelRepositoryCmdOnModel

    data class UpdateModelHashtagDelete(
        override val modelKey: ModelKey,
        val hashtag: Hashtag
    ) : ModelRepositoryCmdOnModel

    data class DeleteModel(
        override val modelKey: ModelKey
    ) : ModelRepositoryCmdOnModel

    // ------------------------------------------------------------------------
    // Types
    // ------------------------------------------------------------------------

    data class CreateType(
        override val modelKey: ModelKey,
        val initializer: ModelTypeInitializer
    ) : ModelRepositoryCmdOnModel

    data class UpdateType(
        override val modelKey: ModelKey,
        val typeId: TypeKey,
        val cmd: ModelTypeUpdateCmd
    ) : ModelRepositoryCmdOnModel

    data class DeleteType(
        override val modelKey: ModelKey,
        val typeId: TypeKey
    ) : ModelRepositoryCmdOnModel

    // ------------------------------------------------------------------------
    // Entity
    // ------------------------------------------------------------------------

    data class CreateEntityDef(
        override val modelKey: ModelKey,
        val entityDef: EntityDefInMemory
    ) : ModelRepositoryCmdOnModel

    data class UpdateEntityDef(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val cmd: EntityDefUpdateCmd
    ) : ModelRepositoryCmdOnModel

    data class UpdateEntityDefHashtagAdd(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val hashtag: Hashtag
    ) : ModelRepositoryCmdOnModel

    data class UpdateEntityDefHashtagDelete(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val hashtag: Hashtag
    ) : ModelRepositoryCmdOnModel

    data class DeleteEntityDef(
        override val modelKey: ModelKey,
        val entityKey: EntityKey
    ) : ModelRepositoryCmdOnModel

    // ------------------------------------------------------------------------
    // Entity attributes
    // ------------------------------------------------------------------------

    class CreateEntityDefAttributeDef(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeDef: AttributeDef
    ) : ModelRepositoryCmdOnModel

    class DeleteEntityDefAttributeDef(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey
    ) : ModelRepositoryCmdOnModel

    class UpdateEntityDefAttributeDef(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val cmd: AttributeDefUpdateCmd
    ) : ModelRepositoryCmdOnModel

    data class UpdateEntityDefAttributeDefHashtagAdd(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val hashtag: Hashtag
    ) : ModelRepositoryCmdOnModel

    data class UpdateEntityDefAttributeDefHashtagDelete(
        override val modelKey: ModelKey,
        val entityKey: EntityKey,
        val attributeKey: AttributeKey,
        val hashtag: Hashtag
    ) : ModelRepositoryCmdOnModel


    // ------------------------------------------------------------------------
    // Relationships
    // ------------------------------------------------------------------------

    class CreateRelationshipDef(
        override val modelKey: ModelKey,
        val initializer: RelationshipDef
    ) : ModelRepositoryCmdOnModel

    class UpdateRelationshipDef(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val cmd: RelationshipDefUpdateCmd
    ) : ModelRepositoryCmdOnModel

    data class UpdateRelationshipDefHashtagAdd(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val hashtag: Hashtag
    ) : ModelRepositoryCmdOnModel

    data class UpdateRelationshipDefHashtagDelete(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val hashtag: Hashtag
    ) : ModelRepositoryCmdOnModel

    class DeleteRelationshipDef(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey
    ) : ModelRepositoryCmdOnModel

    class CreateRelationshipAttributeDef(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attr: AttributeDef
    ) : ModelRepositoryCmdOnModel

    class UpdateRelationshipAttributeDef(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val cmd: AttributeDefUpdateCmd
    ) : ModelRepositoryCmdOnModel

    data class UpdateRelationshipAttributeDefHashtagAdd(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val hashtag: Hashtag
    ) : ModelRepositoryCmdOnModel

    data class UpdateRelationshipAttributeDefHashtagDelete(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey,
        val hashtag: Hashtag
    ) : ModelRepositoryCmdOnModel

    class DeleteRelationshipAttributeDef(
        override val modelKey: ModelKey,
        val relationshipKey: RelationshipKey,
        val attributeKey: AttributeKey
    ) : ModelRepositoryCmdOnModel


}