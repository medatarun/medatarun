package io.medatarun.model.internal

import io.medatarun.model.ports.exposed.ModelCmd

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

}