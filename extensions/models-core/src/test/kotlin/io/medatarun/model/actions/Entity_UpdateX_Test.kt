package io.medatarun.model.actions

import io.medatarun.model.domain.*
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.model.ports.exposed.ModelQueries
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

@EnableDatabaseTests
class Entity_UpdateX_Test {

    @Test
    fun `update entity with wrong model id throws ModelNotFoundException`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-update-x-model-not-found")
        val typeRef = typeRefKey("String")
        val primaryEntityRef = entityRefKey("entity-primary")
        val wrongModelRef = modelRefKey("unknown-model")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, primaryEntityRef.key, LocalizedTextNotLocalized("Entity primary"))

        assertFailsWith<ModelNotFoundException> {
            env.dispatch(
                ModelAction.Entity_UpdateName(
                    modelRef = wrongModelRef,
                    entityRef = primaryEntityRef,
                    value = LocalizedTextNotLocalized("Updated name")
                )
            )
        }
    }

    @Test
    fun `update entity with wrong entity id throws EntityNotFoundException`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-update-x-entity-not-found")
        val typeRef = typeRefKey("String")
        val wrongEntityRef = entityRefKey("unknown-entity")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)

        assertFailsWith<EntityNotFoundException> {
            env.dispatch(
                ModelAction.Entity_UpdateName(
                    modelRef = modelRef,
                    entityRef = wrongEntityRef,
                    value = LocalizedTextNotLocalized("Updated name")
                )
            )
        }
    }
}

class TestEnvEntityUpdate {
    val runtime = ModelTestEnv()
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
                documentationHome = null
            )
        )
        runtime.dispatch(
            ModelAction.Entity_Create(
                modelRef = modelRef,
                entityKey = secondaryEntityKey,
                name = LocalizedTextNotLocalized("Entity secondary"),
                description = LocalizedMarkdownNotLocalized("Entity secondary description"),
                documentationHome = null
            )
        )
    }
}
