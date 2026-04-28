package io.medatarun.model.actions

import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.LocalizedMarkdown
import io.medatarun.model.domain.LocalizedText
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@EnableDatabaseTests
class Entity_UpdateDescription_Test {

    @Test
    fun `update entity description not null persisted`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-update-description")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("entity-primary")
        val initialDescription = LocalizedMarkdown("Entity primary description")
        val newDescription = LocalizedMarkdown("Primary entity updated description")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(
            modelRef = modelRef,
            entityKey = entityRef.key,
            name = LocalizedText("Entity primary"),
            description = initialDescription
        )

        env.dispatch(
            ModelAction.Entity_UpdateDescription(
                modelRef = modelRef,
                entityRef = entityRef,
                value = newDescription
            )
        )

        env.replayWithRebuild {
            val reloaded = env.queries.findEntity(modelRef, entityRef)
            assertEquals(newDescription, reloaded.description)
        }
    }

    @Test
    fun `update entity description with null then description is null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-update-description-null")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("entity-primary")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(
            modelRef = modelRef,
            entityKey = entityRef.key,
            name = LocalizedText("Entity primary"),
            description = LocalizedMarkdown("Entity primary description")
        )

        env.dispatch(
            ModelAction.Entity_UpdateDescription(
                modelRef = modelRef,
                entityRef = entityRef,
                value = null
            )
        )

        val reloaded = env.queries.findEntity(modelRef, entityRef)
        assertNull(reloaded.description)
    }

    @Test
    fun `update entity description with same value does not create new event`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-update-description-noop")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("entity-primary")
        val currentDescription = LocalizedMarkdown("Entity primary description")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(
            modelRef = modelRef,
            entityKey = entityRef.key,
            name = LocalizedText("Entity primary"),
            description = currentDescription
        )

        val beforeEventId = env.findLastStoredModelChangeEvent(modelRef).eventId
        env.dispatch(ModelAction.Entity_UpdateDescription(
            modelRef = modelRef,
            entityRef = entityRef,
            value = currentDescription
        ))
        val afterEventId = env.findLastStoredModelChangeEvent(modelRef).eventId

        assertEquals(beforeEventId, afterEventId)
    }
}
