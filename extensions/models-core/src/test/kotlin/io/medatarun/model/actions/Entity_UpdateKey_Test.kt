package io.medatarun.model.actions

import io.medatarun.model.domain.*
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
class Entity_UpdateKey_Test {

    @Test
    fun `update entity key with duplicate key throws exception`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-update-key-duplicate")
        val typeRef = typeRefKey("String")
        val primaryEntityRef = entityRefKey("entity-primary")
        val secondaryEntityRef = entityRefKey("entity-secondary")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate2(modelRef, primaryEntityRef.key, LocalizedTextNotLocalized("Entity primary"))
        env.entityCreate2(modelRef, secondaryEntityRef.key, LocalizedTextNotLocalized("Entity secondary"))

        assertFailsWith<EntityUpdateKeyDuplicateKeyException> {
            env.dispatch(
                ModelAction.Entity_UpdateKey(
                    modelRef = modelRef,
                    entityRef = primaryEntityRef,
                    value = secondaryEntityRef.key
                )
            )
        }
    }

    @Test
    fun `update entity key with correct key ok`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-update-key")
        val typeRef = typeRefKey("String")
        val primaryEntityRef = entityRefKey("entity-primary")
        val newId = EntityKey("entity-renamed")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate2(modelRef, primaryEntityRef.key, LocalizedTextNotLocalized("Entity primary"))

        env.dispatch(
            ModelAction.Entity_UpdateKey(
                modelRef = modelRef,
                entityRef = primaryEntityRef,
                value = newId
            )
        )

        env.replayWithRebuild {
            assertNull(env.queries.findEntityOptional(modelRef, primaryEntityRef))
            assertNotNull(env.queries.findEntityOptional(modelRef, entityRefKey(newId.value)))
        }
    }

    @Test
    fun `update entity key with same value does not create new event`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-update-key-noop")
        val typeRef = typeRefKey("String")
        val primaryEntityRef = entityRefKey("entity-primary")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate2(modelRef, primaryEntityRef.key, LocalizedTextNotLocalized("Entity primary"))

        val beforeEventId = env.findLastStoredModelChangeEvent(modelRef).eventId
        env.dispatch(
            ModelAction.Entity_UpdateKey(
                modelRef = modelRef,
                entityRef = primaryEntityRef,
                value = primaryEntityRef.key
            )
        )
        val afterEventId = env.findLastStoredModelChangeEvent(modelRef).eventId

        assertEquals(beforeEventId, afterEventId)
    }
}
