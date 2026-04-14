package io.medatarun.model.actions

import io.medatarun.model.domain.*
import io.medatarun.model.domain.EntityAttributeRef.Companion.entityAttributeRefKey
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@EnableDatabaseTests
class EntityAttribute_Create_Test {

    @Test
    fun `create attribute then id name and description shall persist`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-create")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("businesskey")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)

        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = attributeRef.key,
                type = typeRef,
                optional = false,
                name = LocalizedTextNotLocalized("Business Key"),
                description = LocalizedMarkdownNotLocalized("Unique business key")
            )
        )

        env.replayWithRebuild {
            val reloaded = env.queries.findEntityAttribute(modelRef, entityRef, attributeRef)
            val type = env.queries.findType(modelRef, typeRef)
            assertEquals(attributeRef.key, reloaded.key)
            assertEquals(LocalizedTextNotLocalized("Business Key"), reloaded.name)
            assertEquals(LocalizedMarkdownNotLocalized("Unique business key"), reloaded.description)
            assertEquals(type.id, reloaded.typeId)
            assertEquals(false, reloaded.optional)
        }
    }

    @Test
    fun `create attribute with null name then name shall be null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-create-null-name")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("myattribute")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)

        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = attributeRef.key,
                type = typeRef,
                optional = false,
                name = null,
                description = null
            )
        )

        val reloaded = env.queries.findEntityAttribute(modelRef, entityRef, attributeRef)
        assertNull(reloaded.name)
    }

    @Test
    fun `create attribute with null description then description shall be null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-create-null-description")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("myattribute")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)

        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = attributeRef.key,
                type = typeRef,
                optional = false,
                name = LocalizedTextNotLocalized("Name"),
                description = null
            )
        )

        val reloaded = env.queries.findEntityAttribute(modelRef, entityRef, attributeRef)
        assertNull(reloaded.description)
    }

    @Test
    fun `create attribute with optional true description then optional is true`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-create-optional")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("myattribute")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)

        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = attributeRef.key,
                type = typeRef,
                optional = true,
                name = null,
                description = null
            )
        )

        val reloaded = env.queries.findEntityAttribute(modelRef, entityRef, attributeRef)
        assertTrue(reloaded.optional)
    }

    @Test
    fun `create attribute with type boolean then type found`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-create-boolean")
        val entityRef = entityRefKey("entity-a")
        val typeStringRef = typeRefKey("String")
        val typeBooleanRef = typeRefKey("Boolean")
        val attributeRef = entityAttributeRefKey("myattribute")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeStringRef.key)
        env.typeCreate(modelRef, typeBooleanRef.key)
        env.entityCreate(modelRef, entityRef.key)

        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = attributeRef.key,
                type = typeBooleanRef,
                optional = false,
                name = null,
                description = null
            )
        )

        val type = env.queries.findType(modelRef, typeBooleanRef)
        val reloaded = env.queries.findEntityAttribute(modelRef, entityRef, attributeRef)
        assertEquals(type.id, reloaded.typeId)
    }

    @Test
    fun `create attribute with duplicate key then error`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-create-duplicate")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val attributeRef = entityAttributeRefKey("lastname")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeKey = attributeRef.key,
                type = typeRef,
                optional = false,
                name = null,
                description = null
            )
        )

        assertFailsWith<CreateAttributeDuplicateKeyException> {
            env.dispatch(
                ModelAction.EntityAttribute_Create(
                    modelRef = modelRef,
                    entityRef = entityRef,
                    attributeKey = attributeRef.key,
                    type = typeRef,
                    optional = false,
                    name = null,
                    description = null
                )
            )
        }
    }

    @Test
    fun `create attribute unknown type then error`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-attribute-create-unknown-type")
        val entityRef = entityRefKey("entity-a")
        val typeRef = typeRefKey("String")
        val unknownTypeRef = typeRefKey("UnknownType")
        val attributeRef = entityAttributeRefKey("lastname")

        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, typeRef.key)
        env.entityCreate(modelRef, entityRef.key)

        assertFailsWith<TypeNotFoundException> {
            env.dispatch(
                ModelAction.EntityAttribute_Create(
                    modelRef = modelRef,
                    entityRef = entityRef,
                    attributeKey = attributeRef.key,
                    type = unknownTypeRef,
                    optional = false,
                    name = null,
                    description = null
                )
            )
        }
    }
}
