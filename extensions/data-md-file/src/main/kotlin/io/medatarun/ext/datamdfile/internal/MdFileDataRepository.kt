package io.medatarun.ext.datamdfile.internal

import io.medatarun.data.DataRepository
import io.medatarun.data.Entity
import io.medatarun.data.EntityInitializer
import io.medatarun.data.EntityInstanceId
import io.medatarun.data.EntityUpdater
import io.medatarun.model.model.Model
import io.medatarun.model.model.ModelEntityId
import io.medatarun.model.model.ModelId
import java.nio.file.Path

/**
 * This repository stores each entity from the model as files.
 *
 * - Every entityDefId matches a subfolder from repositoryRoot.
 * - Each entity "id" attribute is used as a filename (<repositoryRoot>/<entityDefId>/<entityId>.md)
 * - Each entity is stored a Markdown file
 * - Entity attributes are stored in the frontmatter of the Markdown file
 *   except attributes of type Markdown who are stored in the texte Body, each attribute being in a section,
 *   each section starting with "##".
 * - Attributes being in the frontmatter are always present, and marked as "null" if data is null ( creationDate : null )
 * - Attributes being in the body (type Markdown) are not present if null (section shall not be written)
 *
 */
class MdFileDataRepository(private val repositoryRoot: Path): DataRepository {
    override fun managedEntityDefs(modelId: ModelId): Set<ModelEntityId> {
        TODO("Not yet implemented")
    }

    override fun findAllEntities(
        model: Model,
        entityDefId: ModelEntityId
    ): List<Entity> {
        TODO("Not yet implemented")
    }

    override fun createEntity(
        model: Model,
        entityDefId: ModelEntityId,
        entityInitializer: EntityInitializer
    ) {
        TODO("Not yet implemented")
    }

    override fun updateEntity(
        model: Model,
        entityDefId: ModelEntityId,
        entityUpdater: EntityUpdater
    ) {
        TODO("Not yet implemented")
    }

    override fun deleteEntity(
        model: Model,
        entityDefId: ModelEntityId,
        entityId: EntityInstanceId
    ) {
        TODO("Not yet implemented")
    }

}
