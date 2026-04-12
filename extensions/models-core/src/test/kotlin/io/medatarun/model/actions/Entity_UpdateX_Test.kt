package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.*
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.model.ports.exposed.ModelCmd
import io.medatarun.model.ports.exposed.ModelCmdEnveloppe
import io.medatarun.model.ports.exposed.ModelCmds
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.platform.kernel.getService
import io.medatarun.security.AppTraceabilityRecord
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

@EnableDatabaseTests
class Entity_UpdateX_Test {

    @Test
    fun `update entity with wrong model id throws ModelNotFoundException`() {
        val env = TestEnvEntityUpdate()
        val wrongModelKey = modelRefKey("unknown-model")

        assertFailsWith<ModelNotFoundException> {
            env.runtime.dispatch(
                ModelAction.Entity_UpdateName(
                    wrongModelKey,
                    env.primaryEntityRef,
                    LocalizedTextNotLocalized("Updated name")
                )
            )
        }
    }

    @Test
    fun `update entity with wrong entity id throws EntityNotFoundException`() {
        val env = TestEnvEntityUpdate()
        val wrongEntityId = EntityKey("unknown-entity")
        val wrongEntityRef = EntityRef.ByKey(wrongEntityId)

        assertFailsWith<EntityNotFoundException> {
            env.dispatch(
                ModelAction.Entity_UpdateName(
                    env.modelRef,
                    wrongEntityRef,
                    LocalizedTextNotLocalized("Updated name")
                )
            )
        }
    }

    @Test
    fun `update entity identifier attribute updates compatibility primary key participant`() {
        val env = TestEnvEntityUpdate()
        val alternateIdentifierAttributeKey = AttributeKey("secondary_id")
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = env.modelRef,
                entityRef = env.primaryEntityRef,
                attributeKey = alternateIdentifierAttributeKey,
                type = typeRef("String"),
                optional = false,
                name = null,
                description = null
            )
        )
        val modelBefore = env.query.findModel(env.modelRef)
        val newIdentifierAttribute = modelBefore.findEntityAttribute(
            env.primaryEntityRef,
            EntityAttributeRef.ByKey(alternateIdentifierAttributeKey)
        )

        val modelCmds = env.runtime.platform.services.getService(ModelCmds::class)
        modelCmds.dispatch(
            ModelCmdEnveloppe(
                traceabilityRecord = AppTraceabilityRecord.fromRaw("test", ModelTestEnv.testPrincipal.id),
                cmd = ModelCmd.UpdateEntityIdentifierAttribute(
                    modelRef = env.modelRef,
                    entityRef = env.primaryEntityRef,
                    value = EntityAttributeRef.ById(newIdentifierAttribute.id)
                )
            )
        )

        val modelAfter = env.query.findModel(env.modelRef)
        val entityAfter = modelAfter.findEntity(env.primaryEntityRef)
        val primaryKeyAfter = assertNotNull(modelAfter.findEntityPrimaryKeyOptional(entityAfter.id))
        assertEquals(newIdentifierAttribute.id, entityAfter.identifierAttributeId)
        assertEquals(listOf(newIdentifierAttribute.id), primaryKeyAfter.participants.map { it.attributeId })
        assertEquals(listOf(0), primaryKeyAfter.participants.map { it.position })
    }

}
class TestEnvEntityUpdate {
    val runtime = createEnv()
    val dispatch = runtime::dispatch
    val query: ModelQueries = runtime.queries
    private val modelKey = ModelKey("model-entity-update")
    val modelRef = modelRefKey(ModelKey("model-entity-update"))
    val primaryEntityKey = EntityKey("entity-primary")
    val primaryEntityRef = EntityRef.ByKey(primaryEntityKey)
    val secondaryEntityKey = EntityKey("entity-secondary")
    val secondaryEntityRef = EntityRef.ByKey(secondaryEntityKey)

    init {
        runtime.dispatch(
            ModelAction.Model_Create(
                key = modelKey,
                name = LocalizedTextNotLocalized("Model entity update"),
                description = null,
                version = ModelVersion("1.0.0")
            )
        )
        runtime.dispatch(ModelAction.Type_Create(modelRef, TypeKey("String"), null, null))
        runtime.dispatch(
            ModelAction.Entity_Create(
                modelRef = modelRef,
                entityKey = primaryEntityKey,
                name = LocalizedTextNotLocalized("Entity primary"),
                description = LocalizedMarkdownNotLocalized("Entity primary description"),
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
        runtime.dispatch(
            ModelAction.Entity_Create(
                modelRef = modelRef,
                entityKey = secondaryEntityKey,
                name = LocalizedTextNotLocalized("Entity secondary"),
                description = LocalizedMarkdownNotLocalized("Entity secondary description"),
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
    }
}
