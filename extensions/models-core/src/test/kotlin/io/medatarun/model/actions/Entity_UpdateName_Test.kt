package io.medatarun.model.actions

import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.TextSingleLine
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@EnableDatabaseTests
class Entity_UpdateName_Test {

    @Test
    fun `update entity name not null persisted`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-update-name")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("entity-primary")
        val newName = TextSingleLine("Entity primary updated")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key, TextSingleLine("Entity primary"))

        env.dispatch(
            ModelAction.Entity_UpdateName(
                modelRef = modelRef,
                entityRef = entityRef,
                value = newName
            )
        )

        env.replayWithRebuild {
            val reloaded = env.queries.findEntity(modelRef, entityRef)
            assertEquals(newName, reloaded.name)
        }
    }

    @Test
    fun `update entity name null then name is null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-update-name-null")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("entity-primary")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key, TextSingleLine("Entity primary"))

        env.dispatch(
            ModelAction.Entity_UpdateName(
                modelRef = modelRef,
                entityRef = entityRef,
                value = null
            )
        )

        val reloaded = env.queries.findEntity(modelRef, entityRef)
        assertNull(reloaded.name)
    }

    @Test
    fun `update entity name after release persists on current model`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-update-name-after-release")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("entity-primary")
        val newName = TextSingleLine("Entity primary after release")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key, TextSingleLine("Entity primary"))

        env.dispatch(ModelAction.Model_Release(modelRef, ModelVersion("1.1.0")))
        env.dispatch(
            ModelAction.Entity_UpdateName(
                modelRef = modelRef,
                entityRef = entityRef,
                value = newName
            )
        )

        val reloaded = env.queries.findEntity(modelRef, entityRef)
        assertEquals(newName, reloaded.name)
    }

    @Test
    fun `update entity name with same value does not create new event`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-update-name-noop")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("entity-primary")
        val currentName = TextSingleLine("Entity primary")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key, currentName)

        val beforeEventId = env.findLastStoredModelChangeEvent(modelRef).eventId
        env.dispatch(
            ModelAction.Entity_UpdateName(
                modelRef = modelRef,
                entityRef = entityRef,
                value = currentName
            )
        )
        val afterEventId = env.findLastStoredModelChangeEvent(modelRef).eventId

        assertEquals(beforeEventId, afterEventId)
    }
}
