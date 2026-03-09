package io.medatarun.model.domain.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.EntityNotFoundException
import io.medatarun.model.domain.EntityRef
import io.medatarun.model.domain.LocalizedMarkdownNotLocalized
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.ModelKey
import io.medatarun.model.domain.ModelNotFoundException
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.ModelVersion
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.typeRef
import io.medatarun.model.ports.exposed.ModelQueries
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

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
                modelKey = modelKey,
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
