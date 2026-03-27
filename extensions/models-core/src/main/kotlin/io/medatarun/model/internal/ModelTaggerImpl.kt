package io.medatarun.model.internal

import io.medatarun.model.domain.AttributeOwnerId
import io.medatarun.model.domain.ModelAggregate
import io.medatarun.model.domain.ModelRef
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.tags.core.domain.TagId
import io.medatarun.tags.core.domain.TagRef

class ModelTaggerImpl {
    /**
     * Persists tag additions produced during copy+retag orchestration.
     */
    interface ModelTagWriter {
        fun addModelTag(cmd: ModelCmd.UpdateModelTagAdd)
        fun addEntityTag(cmd: ModelCmd.UpdateEntityTagAdd)
        fun addRelationshipTag(cmd: ModelCmd.UpdateRelationshipTagAdd)
        fun addEntityAttributeTag(cmd: ModelCmd.UpdateEntityAttributeTagAdd)
        fun addRelationshipAttributeTag(cmd: ModelCmd.UpdateRelationshipAttributeTagAdd)
    }

    fun applyAllTags(
        source: ModelAggregate,
        destModelRef: ModelRef,
        tagWriter: ModelTagWriter,
        idMaps: ModelSourceDestIdConv,
        resolveTag: (tagId: TagId) -> TagRef?
    ) {

        fun applyTags(tagIds: List<TagId>, block: (tagRef: TagRef) -> Unit) {
            for(tagId in tagIds) {
                val tagRef = resolveTag(tagId)
                if (tagRef != null) block(tagRef)
            }
        }

        applyTags(source.tags) { tagRef ->
            tagWriter.addModelTag(
                ModelCmd.UpdateModelTagAdd(
                    destModelRef,
                    tagRef
                )
            )
        }

        for (entity in source.entities) {
            val destEntityRef = idMaps.getDestEntityRef(entity.id)
            applyTags(entity.tags) { tagRef ->
                tagWriter.addEntityTag(
                    ModelCmd.UpdateEntityTagAdd(
                        destModelRef,
                        destEntityRef,
                        tagRef
                    )
                )
            }
        }

        for (relationship in source.relationships) {
            val destRelationshipRef = idMaps.getDestRelationshipRef(relationship.id)
            applyTags(relationship.tags) { tagRef ->
                tagWriter.addRelationshipTag(
                    ModelCmd.UpdateRelationshipTagAdd(
                        destModelRef,
                        destRelationshipRef,
                        tagRef
                    )
                )
            }
        }

        for (attribute in source.attributes) {
            applyTags(attribute.tags) { tagRef ->
                val owner = attribute.ownerId
                when (owner) {
                    is AttributeOwnerId.OwnerEntityId -> {
                        val destEntityRef = idMaps.getDestEntityRef(owner.id)
                        val destAttributeRef = idMaps.getDestEntityAttributeRef(attribute.id)
                        tagWriter.addEntityAttributeTag(
                            ModelCmd.UpdateEntityAttributeTagAdd(
                                destModelRef,
                                destEntityRef,
                                destAttributeRef,
                                tagRef
                            )
                        )
                    }

                    is AttributeOwnerId.OwnerRelationshipId -> {
                        val destRelationshipRef = idMaps.getDestRelationshipRef(owner.id)
                        val destAttributeRef = idMaps.getDestRelationshipAttributeRef(attribute.id)
                        tagWriter.addRelationshipAttributeTag(
                            ModelCmd.UpdateRelationshipAttributeTagAdd(
                                destModelRef,
                                destRelationshipRef,
                                destAttributeRef,
                                tagRef
                            )
                        )
                    }
                }

            }
        }
    }

}