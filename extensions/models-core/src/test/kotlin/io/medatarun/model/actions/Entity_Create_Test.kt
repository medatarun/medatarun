package io.medatarun.model.actions

import io.medatarun.model.domain.*
import io.medatarun.model.domain.EntityAttributeRef.Companion.entityAttributeRefKey
import io.medatarun.model.domain.EntityRef.Companion.entityRefKey
import io.medatarun.model.domain.ModelRef.Companion.modelRefKey
import io.medatarun.model.domain.TypeRef.Companion.typeRefKey
import io.medatarun.model.domain.fixtures.ModelTestEnv
import io.medatarun.platform.db.testkit.EnableDatabaseTests
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@EnableDatabaseTests
class Entity_Create_Test {

    @Test
    fun `create entity then id and name shall persist`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-create")
        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, TypeKey("String"))
        val entityRef = entityRefKey("entity")
        val name = LocalizedTextNotLocalized("Order")
        val description = LocalizedMarkdownNotLocalized("Order description")
        val docHome = "http://test.dev/local=123"
        val identityAttributeRef = entityAttributeRefKey("id")
        env.dispatch(
            ModelAction.Entity_Create2(
                modelRef = modelRef,
                entityKey = entityRef.key,
                name = name,
                description = description,
                documentationHome = docHome
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                name = LocalizedTextNotLocalized("Identifier"),
                attributeKey = identityAttributeRef.key,
                type = typeRefKey("String"),
                optional = false,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityPrimaryKey_Update(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = listOf(identityAttributeRef)
            )
        )

        val reloaded = env.queries.findEntity(modelRef, entityRef)
        assertEquals(entityRef.key, reloaded.key)
        assertEquals(name, reloaded.name)
        assertEquals(description, reloaded.description)
        assertEquals(EntityOrigin.Manual, reloaded.origin)
        assertEquals(URI.create(docHome).toURL(), reloaded.documentationHome)
        val identityAttribute = env.queries.findEntityAttribute(modelRef, entityRef, identityAttributeRef)
        assertEquals(identityAttributeRef.key, identityAttribute.key)
        assertEquals("Identifier", identityAttribute.name?.name)
        assertNull(identityAttribute.description?.name)
        assertEntityPrimaryKeyMatchesIdentityAttribute(env, modelRef, entityRef, identityAttribute.id)
    }

    @Test
    fun `create entity with null name then name shall be null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-create-null-name")
        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, TypeKey("String"))
        val entityRef = entityRefKey("entity-null-name")
        val description = LocalizedMarkdownNotLocalized("Entity without name")
        val identityAttributeRef = entityAttributeRefKey("id")

        env.dispatch(
            ModelAction.Entity_Create2(
                modelRef = modelRef,
                entityKey = entityRef.key,
                name = null,
                description = description,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                name = null,
                attributeKey = identityAttributeRef.key,
                type = typeRefKey("String"),
                optional = false,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityPrimaryKey_Update(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = listOf(identityAttributeRef)
            )
        )

        val reloaded = env.queries.findEntity(modelRef, entityRef)
        assertEquals(entityRef.key, reloaded.key)
        assertNull(reloaded.name)
        assertEquals(description, reloaded.description)
        assertEquals(EntityOrigin.Manual, reloaded.origin)
        val identityAttribute = env.queries.findEntityAttribute(modelRef, entityRef, identityAttributeRef)
        assertEntityPrimaryKeyMatchesIdentityAttribute(env, modelRef, entityRef, identityAttribute.id)
    }

    @Test
    fun `create entity with null description then description shall be null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-create-null-description")
        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, TypeKey("String"))
        val entityRef = entityRefKey("entity-null-description")
        val name = LocalizedTextNotLocalized("Entity without description")
        val identityAttributeRef = entityAttributeRefKey("String")

        env.dispatch(
            ModelAction.Entity_Create2(
                modelRef = modelRef,
                entityKey = entityRef.key,
                name = name,
                description = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                name = null,
                attributeKey = identityAttributeRef.key,
                type = typeRefKey("String"),
                optional = false,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityPrimaryKey_Update(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = listOf(identityAttributeRef)
            )
        )

        val reloaded = env.queries.findEntity(modelRef, entityRef)
        assertEquals(entityRef.key, reloaded.key)
        assertEquals(name, reloaded.name)
        assertNull(reloaded.description)
        assertEquals(EntityOrigin.Manual, reloaded.origin)
        val identityAttribute = env.queries.findEntityAttribute(modelRef, entityRef, identityAttributeRef)
        assertEntityPrimaryKeyMatchesIdentityAttribute(env, modelRef, entityRef, identityAttribute.id)
    }

    @Test
    fun `create entity with null attribute name and description then name and desc shall be null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-create-null-attr")
        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, TypeKey("String"))
        val entityRef = entityRefKey("entity-null-attr-name")
        val description = LocalizedMarkdownNotLocalized("Entity without name")
        val identityAttributeRef = entityAttributeRefKey("id")

        env.dispatch(
            ModelAction.Entity_Create2(
                modelRef = modelRef,
                entityKey = entityRef.key,
                name = null,
                description = description,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                name = null,
                attributeKey = identityAttributeRef.key,
                type = typeRefKey("String"),
                optional = false,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityPrimaryKey_Update(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = listOf(identityAttributeRef)
            )
        )

        val identityAttribute = env.queries.findEntityAttribute(modelRef, entityRef, identityAttributeRef)
        assertNull(identityAttribute.name)
        assertNull(identityAttribute.description)
        assertEntityPrimaryKeyMatchesIdentityAttribute(env, modelRef, entityRef, identityAttribute.id)
    }

    @Test
    fun `create entity with documentation home null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-create-doc-null")
        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, TypeKey("String"))
        val entityRef = entityRefKey("entity-null-attr-name")
        val identityAttributeRef = entityAttributeRefKey("id")
        env.dispatch(
            ModelAction.Entity_Create2(
                modelRef = modelRef,
                entityKey = entityRef.key,
                name = null,
                description = null,
                documentationHome = null
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                name = null,
                attributeKey = identityAttributeRef.key,
                type = typeRefKey("String"),
                optional = false,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityPrimaryKey_Update(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = listOf(identityAttributeRef)
            )
        )
        assertNull(env.queries.findEntity(modelRef, entityRef).documentationHome)
        val identityAttribute = env.queries.findEntityAttribute(modelRef, entityRef, identityAttributeRef)
        assertEntityPrimaryKeyMatchesIdentityAttribute(env, modelRef, entityRef, identityAttribute.id)
    }

    @Test
    fun `create entity with documentation home not null`() {
        val env = ModelTestEnv()
        val modelRef = modelRefKey("entity-create-doc-set")
        env.modelCreate(modelRef.key)
        env.typeCreate(modelRef, TypeKey("String"))
        val entityRef = entityRefKey("entity-null-attr-name")
        val url = URI("http://localhost:8080").toURL()
        val identityAttributeRef = entityAttributeRefKey("id")
        env.dispatch(
            ModelAction.Entity_Create2(
                modelRef = modelRef,
                entityKey = entityRef.key,
                name = null,
                description = null,
                documentationHome = url.toString()
            )
        )
        env.dispatch(
            ModelAction.EntityAttribute_Create(
                modelRef = modelRef,
                entityRef = entityRef,
                name = null,
                attributeKey = identityAttributeRef.key,
                type = typeRefKey("String"),
                optional = false,
                description = null
            )
        )
        env.dispatch(
            ModelAction.EntityPrimaryKey_Update(
                modelRef = modelRef,
                entityRef = entityRef,
                attributeRef = listOf(identityAttributeRef)
            )
        )
        assertEquals(url, env.queries.findEntity(modelRef, entityRef).documentationHome)
        val identityAttribute = env.queries.findEntityAttribute(modelRef, entityRef, identityAttributeRef)
        assertEntityPrimaryKeyMatchesIdentityAttribute(env, modelRef, entityRef, identityAttribute.id)
    }

    private fun assertEntityPrimaryKeyMatchesIdentityAttribute(
        env: ModelTestEnv,
        modelRef: ModelRef,
        entityRef: EntityRef,
        expectedIdentityAttributeId: AttributeId
    ) {
        val primaryKey = assertNotNull(env.queries.findEntityPrimaryKeyOptional(modelRef, entityRef))
        assertEquals(listOf(expectedIdentityAttributeId), primaryKey.participants.map { it.attributeId })
        assertEquals(listOf(0), primaryKey.participants.map { it.position })
    }

}
