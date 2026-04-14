package io.medatarun.model.actions

import io.medatarun.platform.db.testkit.EnableDatabaseTests
import io.medatarun.model.actions.ModelAction.EntityAttribute_UpdateType
import io.medatarun.model.domain.EntityAttributeRef
import io.medatarun.model.domain.TypeKey
import io.medatarun.model.domain.TypeNotFoundException
import io.medatarun.model.domain.TypeRef
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

@EnableDatabaseTests
class EntityAttribute_UpdateType_Test {

    @Test
    fun `update attribute type is persisted`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val typeMarkdownKey = TypeKey("Markdown")
        env.dispatch(ModelAction.Type_Create(env.sampleModelRef, typeMarkdownKey, null, null))
        val typeMarkdownId = env.query.findType(env.sampleModelRef, TypeRef.typeRefKey(typeMarkdownKey)).id
        val type = env.query.findType(env.sampleModelRef, TypeRef.typeRefKey(TypeKey("String")))
        val attr = env.createAttribute(type = TypeRef.typeRefId(type.id))

        val attributeRef = EntityAttributeRef.entityAttributeRefKey(attr.key)
        env.runtime.dispatch(
            EntityAttribute_UpdateType(
                env.sampleModelRef,
                env.sampleEntityRef,
                attributeRef,
                TypeRef.typeRefKey(typeMarkdownKey)
            )
        )
        val reloaded = env.reloadAttribute(attributeRef)
        assertEquals(typeMarkdownId, reloaded.typeId)
    }

    @Test
    fun `update attribute unknown type then error`() {
        val env = TestEnvEntityAttribute()
        env.addSampleEntity()
        val type = env.query.findType(env.sampleModelRef, TypeRef.typeRefKey(TypeKey("String")))
        val attr = env.createAttribute(type = TypeRef.typeRefId(type.id))
        assertThrows<TypeNotFoundException> {
            env.dispatch(
                EntityAttribute_UpdateType(
                    env.sampleModelRef,
                    env.sampleEntityRef,
                    EntityAttributeRef.entityAttributeRefKey(attr.key),
                    TypeRef.typeRefKey(TypeKey("String2"))
                )
            )
        }
    }
}