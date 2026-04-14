package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.*
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.ports.exposed.ModelQueries
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
class Entity_Delete_Test {

    @Test
    fun `delete entity in model then entity removed`() {
        val env = TestEnvOneModel()
        val entityId = EntityKey("entity-to-delete")
        val entityRef = EntityRef.ByKey(entityId)

        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = env.modelRef,
                entityKey = entityId,
                name = LocalizedTextNotLocalized("To delete"),
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
        env.dispatch(ModelAction.Entity_Delete(env.modelRef, entityRef))

        val reloaded = env.query.findModelAggregate(env.modelRef)
        assertNull(reloaded.findEntityOptional(entityId))
    }

    @Test
    fun `delete entity with same key in two models then only entity in the specified model is removed`() {
        val runtime = createEnv()
        val query: ModelQueries = runtime.queries

        val modelKey1 = ModelKey("model-1")
        val modelRef1 = modelRefKey(modelKey1)
        val modelKey2 = ModelKey("model-2")
        val modelRef2 = modelRefKey(modelKey2)
        val entityKey = EntityKey("shared-entity")
        val entityRef = EntityRef.ByKey(entityKey)

        runtime.dispatch(
            ModelAction.Model_Create(
                modelKey1,
                LocalizedTextNotLocalized("Model 1"),
                null,
                ModelVersion("1.0.0")
            )
        )
        runtime.dispatch(ModelAction.Type_Create(modelRef1, TypeKey("String"), null, null))
        runtime.dispatch(
            ModelAction.Model_Create(
                modelKey2,
                LocalizedTextNotLocalized("Model 2"),
                null,
                ModelVersion("1.0.0")
            )
        )
        runtime.dispatch(ModelAction.Type_Create(modelRef2, TypeKey("String"), null, null))
        runtime.dispatch(
            ModelAction.Entity_Create(
                modelRef = modelRef1,
                entityKey = entityKey,
                name = LocalizedTextNotLocalized("Entity"),
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
        runtime.dispatch(
            ModelAction.Entity_Create(
                modelRef = modelRef2,
                entityKey = entityKey,
                name = LocalizedTextNotLocalized("Entity"),
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )

        runtime.dispatch(ModelAction.Entity_Delete(modelRef1, entityRef))

        val reloadedModel1 = query.findModelAggregate(modelRef1)
        val reloadedModel2 = query.findModelAggregate(modelRef2)

        assertNull(reloadedModel1.findEntityOptional(entityKey))
        assertNotNull(reloadedModel2.findEntityOptional(entityKey))
    }
}