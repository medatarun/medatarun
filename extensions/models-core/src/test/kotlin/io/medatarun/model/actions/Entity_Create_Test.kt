package io.medatarun.model.actions

import io.medatarun.model.actions.ModelAction
import io.medatarun.model.domain.AttributeKey
import io.medatarun.model.domain.EntityKey
import io.medatarun.model.domain.EntityOrigin
import io.medatarun.model.domain.EntityRef
import io.medatarun.model.domain.LocalizedMarkdownNotLocalized
import io.medatarun.model.domain.LocalizedTextNotLocalized
import io.medatarun.model.domain.typeRef
import org.junit.jupiter.api.Test
import java.net.URI
import kotlin.test.assertEquals
import kotlin.test.assertNull

class Entity_Create_Test {

    @Test
    fun `create entity then id and name shall persist`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvOneModel()
        val entityId = EntityKey("entity")
        val entityRef = EntityRef.ByKey(entityId)
        val name = LocalizedTextNotLocalized("Order")
        val description = LocalizedMarkdownNotLocalized("Order description")
        val docHome = "http://test.dev/local=123"
        env.dispatch(
            ModelAction.Entity_Create(
                env.modelRef,
                entityKey = entityId,
                name = name,
                description = description,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeName = LocalizedTextNotLocalized("Identifier"),
                documentationHome = docHome,
                identityAttributeType = typeRef("String")
            )
        )

        val reloaded = env.query.findEntity(env.modelRef, entityRef)
        assertEquals(entityId, reloaded.key)
        assertEquals(name, reloaded.name)
        assertEquals(description, reloaded.description)
        assertEquals(EntityOrigin.Manual, reloaded.origin)
        assertEquals(URI.create(docHome).toURL(), reloaded.documentationHome)
        val attributes = env.query.findModel(env.modelRef).findEntityAttributes(reloaded.ref)
        assertEquals(1, attributes.size)
        val attrId = attributes[0]
        assertEquals(AttributeKey("id"), attrId.key)
        assertEquals("Identifier", attrId.name?.name)
        assertNull(attrId.description?.name)
    }

    @Test
    fun `create entity with null name then name shall be null`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvOneModel()
        val entityKey = EntityKey("entity-null-name")
        val entityRef = EntityRef.ByKey(entityKey)
        val description = LocalizedMarkdownNotLocalized("Entity without name")

        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = env.modelRef,
                entityKey = entityKey,
                name = null,
                description = description,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )

        val reloaded = env.query.findEntity(env.modelRef, entityRef)
        assertEquals(entityKey, reloaded.key)
        assertNull(reloaded.name)
        assertEquals(description, reloaded.description)
        assertEquals(EntityOrigin.Manual, reloaded.origin)
        val attributes = env.query.findModel(env.modelRef).findEntityAttributes(entityRef)
        assertEquals(1, attributes.size)
    }

    @Test
    fun `create entity with null description then description shall be null`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvOneModel()
        val entityKey = EntityKey("entity-null-description")
        val entityRef = EntityRef.ByKey(entityKey)
        val name = LocalizedTextNotLocalized("Entity without description")

        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = env.modelRef,
                entityKey = entityKey,
                name = name,
                description = null,
                identityAttributeKey = AttributeKey("String"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )

        val reloaded = env.query.findEntity(env.modelRef, entityRef)
        assertEquals(entityKey, reloaded.key)
        assertEquals(name, reloaded.name)
        assertNull(reloaded.description)
        assertEquals(EntityOrigin.Manual, reloaded.origin)
    }

    @Test
    fun `create entity with null attribute name and description then name and desc shall be null`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvOneModel()
        val entityKey = EntityKey("entity-null-attr-name")
        val entityRef = EntityRef.ByKey(entityKey)
        val description = LocalizedMarkdownNotLocalized("Entity without name")

        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = env.modelRef,
                entityKey = entityKey,
                name = null,
                description = description,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )


        val attributes = env.query.findModel(env.modelRef).findEntityAttributes(entityRef)
        assertNull(attributes[0].name)
        assertNull(attributes[0].description)
    }

    @Test
    fun `create entity with documentation home null`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvOneModel()
        val entityKey = EntityKey("entity-null-attr-name")
        val entityRef = EntityRef.ByKey(entityKey)
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = env.modelRef,
                entityKey = entityKey,
                name = null,
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = null
            )
        )
        assertNull(env.query.findEntity(env.modelRef, entityRef).documentationHome)
    }

    @Test
    fun `create entity with documentation home not null`() {
        val env = _root_ide_package_.io.medatarun.model.actions.TestEnvOneModel()
        val entityKey = EntityKey("entity-null-attr-name")
        val entityRef = EntityRef.ByKey(entityKey)
        val url = URI("http://localhost:8080").toURL()
        env.dispatch(
            ModelAction.Entity_Create(
                modelRef = env.modelRef,
                entityKey = entityKey,
                name = null,
                description = null,
                identityAttributeKey = AttributeKey("id"),
                identityAttributeType = typeRef("String"),
                identityAttributeName = null,
                documentationHome = url.toString()
            )
        )
        assertEquals(url, env.query.findEntity(env.modelRef, entityRef).documentationHome)
    }

}
