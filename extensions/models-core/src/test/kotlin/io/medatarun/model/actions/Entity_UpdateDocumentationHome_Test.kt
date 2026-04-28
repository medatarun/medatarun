package io.medatarun.model.actions

import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.TextSingleLine
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNull

@EnableDatabaseTests
class Entity_UpdateDocumentationHome_Test {

    @Test
    fun `update entity documentation home not null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-update-doc-home")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("entity-primary")
        val url = URI("http://localhost").toURL()

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key, TextSingleLine("Entity primary"))

        env.dispatch(
            ModelAction.Entity_UpdateDocumentationHome(
                modelRef = modelRef,
                entityRef = entityRef,
                value = url.toString()
            )
        )

        env.replayWithRebuild {
            val reloaded = env.queries.findEntity(modelRef, entityRef)
            assertEquals(url, reloaded.documentationHome)
        }
    }

    @Test
    fun `update entity documentation home to null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-update-doc-home-null")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("entity-primary")
        val url = URI("http://localhost").toURL()

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key, TextSingleLine("Entity primary"))

        env.dispatch(
            ModelAction.Entity_UpdateDocumentationHome(
                modelRef = modelRef,
                entityRef = entityRef,
                value = url.toString()
            )
        )
        env.dispatch(
            ModelAction.Entity_UpdateDocumentationHome(
                modelRef = modelRef,
                entityRef = entityRef,
                value = null
            )
        )

        val reloaded = env.queries.findEntity(modelRef, entityRef)
        assertNull(reloaded.documentationHome)
    }

    @Test
    fun `update entity documentation home with same value does not create new event`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-update-doc-home-noop")
        val typeRef = typeRefKey("String")
        val entityRef = entityRefKey("entity-primary")
        val docHome = "http://localhost"

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key, TextSingleLine("Entity primary"))
        env.dispatch(
            ModelAction.Entity_UpdateDocumentationHome(
                modelRef = modelRef,
                entityRef = entityRef,
                value = docHome
            )
        )

        val beforeEventId = env.findLastStoredModelChangeEvent(modelRef).eventId
        env.dispatch(
            ModelAction.Entity_UpdateDocumentationHome(
                modelRef = modelRef,
                entityRef = entityRef,
                value = docHome
            )
        )
        val afterEventId = env.findLastStoredModelChangeEvent(modelRef).eventId

        assertEquals(beforeEventId, afterEventId)
    }
}
