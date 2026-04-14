package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.domain.*
import io.medatarun.model.domain.TypeKey
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@EnableDatabaseTests
class EntityAttribute_Create_Test {

    @Test
    fun `create attribute then id name and description shall persist`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val reloaded = env.createAttribute(
            attributeKey = AttributeKey("businesskey"),
            type = TypeRef.typeRefKey(TypeKey("String")),
            optional = false,
            name = LocalizedTextNotLocalized("Business Key"),
            description = LocalizedMarkdownNotLocalized("Unique business key"),
        )
        val type = env.query.findType(env.sampleModelRef, TypeRef.typeRefKey(TypeKey("String")))
        assertEquals(AttributeKey("businesskey"), reloaded.key)
        assertEquals(LocalizedTextNotLocalized("Business Key"), reloaded.name)
        assertEquals(LocalizedMarkdownNotLocalized("Unique business key"), reloaded.description)
        assertEquals(type.id, reloaded.typeId)
        assertEquals(false, reloaded.optional)
    }

    @Test
    fun `create attribute with null name then name shall be null`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val reloaded = env.createAttribute(name = null)
        assertNull(reloaded.name)
    }

    @Test
    fun `create attribute with null description then description shall be null`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val reloaded = env.createAttribute(description = null)
        assertNull(reloaded.description)
    }

    @Test
    fun `create attribute with optional true description then optional is true`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val reloaded = env.createAttribute(optional = true)
        assertTrue(reloaded.optional)
    }

    @Test
    fun `create attribute with type boolean then type found`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val typeKey = TypeKey("Boolean")
        env.dispatch(ModelAction.Type_Create(env.sampleModelRef, typeKey, null, null))
        val type = env.query.findType(env.sampleModelRef, TypeRef.ByKey(typeKey))
        val reloaded = env.createAttribute(type = TypeRef.typeRefKey(TypeKey("Boolean")))
        assertEquals(type.id, reloaded.typeId)
    }

    @Test
    fun `create attribute with duplicate key then error`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        env.createAttribute(attributeKey = AttributeKey("lastname"))
        assertFailsWith<CreateAttributeDuplicateKeyException> {
            env.createAttribute(attributeKey = AttributeKey("lastname"))
        }
    }

    @Test
    fun `create attribute unknown type then error`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        assertFailsWith<TypeNotFoundException> {
            env.createAttribute(attributeKey = AttributeKey("lastname"),
                type = TypeRef.typeRefKey(TypeKey("UnknownType"))
            )
        }
    }
}